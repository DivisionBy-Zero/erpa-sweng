from bunch import Bunch
from contextlib import contextmanager
from datetime import datetime
import json
import os
import unittest

from api import app
from contexts import with_context_using_instance
import models


user_prefix = 'avogadro'
test_game_obj = {'title': "My super game",
                 'description': "A super game description",
                 'difficulty': 10,
                 'isCampaign': 0,
                 'locationLat': 0.0,
                 'locationLon': 0.0,
                 'maxPlayers': 0,
                 'minPlayers': 0,
                 'sessionLengthInMinutes': 0,
                 'universe': "Marvel"}


class MyCurl():
    def __init__(self, app):
        self.app = app

    def __getattr__(self, x):
        def mk_wrapped_call(*args, **kwargs):
            res = getattr(self.app, x)(*args, **kwargs)
            res.asDict = lambda: json.loads(res.data)
            res.asStr = lambda: str(res.data)
            res.asObject = lambda: Bunch(res.asDict())
            return res
        return mk_wrapped_call


def create_and_register_username(curl, username):
    user_uuid = curl.post('/users/newuser/{}'.format(username)).asStr()
    curl.post('/users/register_auth', json={'public_key': "pk!"+username,
                                            'user_uuid': user_uuid})
    curl.post('/users', json={'is_gm': True, 'is_player': True,
                              'uuid': user_uuid}).asObject()
    return user_uuid


def with_user(f):
    @contextmanager
    def mk_user(test):
        def_usernamme = "Lavoisier"
        # username = user_prefix + str(datetime.now())
        # user_uuid = test.app.post('/users/newuser/{}'.format(username))
        # TODO(@Roos): Remove when the auth system is complete
        user_uuid = (test.curl.get('/users/user/{}'.format(def_usernamme))
                     .asStr())
        yield type('user', (object,), {'uuid': user_uuid})
    return with_context_using_instance(mk_user)(f)


def with_user_and_game(f):
    @contextmanager
    @with_user
    def mk_game(test, user):
        res = test.curl.post('/games', json=test_game_obj)
        if res.status_code != 200:
            raise Exception("Error performing request [{}]: {}"
                            "".format(res.status_code, res.data))
        yield user, res.asObject()
    return with_context_using_instance(mk_game)(f)


class TestAPI(unittest.TestCase):
    def setUp(self):
        os.environ['DATABASE_URL'] = \
            'sqlite:////tmp/test-erpa-srv-{}.sqlite'.format(datetime.now())
        app.testing = True
        self.app = app.test_client()
        self.curl = MyCurl(self.app)

    def test_ping(self):
        res = self.app.get('/ping')
        assert b'pong' in res.data

    def test_register_new_user(self):
        username = user_prefix + str(datetime.now())
        user_url = '/users/user/{}'.format(username)
        user_uuid = create_and_register_username(self.curl, username)
        self.assertEqual(user_uuid, self.curl.get(user_url).asStr())
        user = self.curl.get('/users/uuid/{}'.format(user_uuid)).asObject()
        self.assertTrue(user.uuid)

    def test_update_user(self):
        username = user_prefix + str(datetime.now())
        user_url = '/users/user/{}'.format(username)
        user_uuid = create_and_register_username(self.curl, username)
        self.assertEqual(user_uuid, self.curl.get(user_url).asStr())
        user = self.curl.get('/users/uuid/{}'.format(user_uuid)).asObject()
        self.assertIsNotNone(user)
        self.assertIsNotNone(user.isGm)

        old_isgm = user.isGm
        user.isGm = False if user.isGm else True
        self.curl.post('/users/uuid/{}'.format(user_uuid),
                       headers={'user_uuid': user_uuid},
                       json=user)
        new_user = self.curl.get('/users/uuid/{}'.format(user_uuid)).asObject()
        self.assertNotEqual(old_isgm, user.isGm)
        self.assertNotEqual(old_isgm, new_user.isGm)

    def test_get_games(self):
        self.app.get('/games')

    @with_user_and_game
    def test_create_game(self, user, game):
        self.assertTrue(user.uuid)
        self.assertTrue(game.uuid)
        game_dict = self.curl.get('/games/uuid/{}'.format(game.uuid)).asDict()
        for gpk, gpv in test_game_obj.items():
            self.assertIn(gpk, game_dict)
            self.assertEqual(str(gpv), str(game_dict[gpk]))

    @with_user_and_game
    def test_update_game(self, user, game):
        self.assertTrue(user.uuid)
        self.assertTrue(game.uuid)
        old_title = game.title
        game.title = "The hitchhiker's guide to " + old_title

        new_game = self.curl.post('/games', json=game).asObject()

        self.assertNotEqual(old_title, new_game.title)
        self.assertEqual(game.title, new_game.title)

    @with_user_and_game
    def test_join_game(self, user, game):
        user_uuid = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))
        user_uuid2 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))

        participats_url = '/games/participants/{}'.format(game.uuid)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests)

        join_request = self.curl.post('/games/join/{}'.format(game.uuid),
                                      headers={'user_uuid': user_uuid}
                                      ).asObject()
        self.assertIsNotNone(join_request.requestStatus)

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers={'user_uuid': user_uuid2}).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REQUEST_TO_JOIN.numerator,
                         join_request.requestStatus)
        join_request.requestStatus = \
            models.PlayerInGameStatus.CONFIRMED.numerator
        self.curl.post(participats_url, json=join_request).asDict()

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers={'user_uuid': user_uuid2}).asDict()
        self.assertTrue(game_join_requests_seen_by_others)
        join_request = Bunch(game_join_requests_seen_by_others[0])
        self.assertEqual(models.PlayerInGameStatus.CONFIRMED.numerator,
                         join_request.requestStatus)

    @with_user_and_game
    def test_join_game_after_reject(self, user, game):
        user_uuid = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))
        user_uuid2 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))

        participats_url = '/games/participants/{}'.format(game.uuid)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests)

        join_request = self.curl.post('/games/join/{}'.format(game.uuid),
                                      headers={'user_uuid': user_uuid}
                                      ).asObject()
        self.assertIsNotNone(join_request.requestStatus)

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers={'user_uuid': user_uuid2}).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REQUEST_TO_JOIN.numerator,
                         join_request.requestStatus)
        join_request.requestStatus = \
            models.PlayerInGameStatus.REJECTED.numerator
        self.curl.post(participats_url, json=join_request).asDict()

        game_join_requests_seen_by_others = self.curl.get(
                participats_url, headers={'user_uuid': user_uuid2}).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REJECTED.numerator,
                         join_request.requestStatus)
