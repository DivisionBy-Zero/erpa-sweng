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
            res.asStr = lambda: res.data.decode('UTF-8')
            res.asObject = lambda: Bunch(res.asDict())
            return res
        return mk_wrapped_call


def mk_default_auth(username: str, user_uuid: str) -> models.UserAuth:
    return models.UserAuth(public_key=username, user_uuid=user_uuid)


def mk_default_session_token(user_uuid: str) -> models.UserSessionToken:
    return models.UserSessionToken(user_uuid=user_uuid,
                                   session_token=user_uuid)


def mk_def_auth_header(user_uuid):
    return {'Authorization': user_uuid}


def create_and_register_username(curl, username):
    user_uuid = curl.post('/users/newuser/{}'.format(username)).asStr()
    # TODO(@Roos): Fix this when the auth system is complete
    curl.post('/users/register_auth',
              json=mk_default_auth(username, user_uuid))
    curl.post('/users', json={'is_gm': True, 'is_player': True,
                              'uuid': user_uuid}).asObject()
    return user_uuid


def with_user(f):
    @contextmanager
    def mk_user(test):
        def_username = "Lavoisier"
        username = def_username + str(datetime.now())
        user_uuid = create_and_register_username(test.curl, username)
        yield type('user', (object,), {'uuid': user_uuid})
    return with_context_using_instance(mk_user)(f)


def with_user_and_game(f):
    @contextmanager
    @with_user
    def mk_game(test, user):
        res = test.curl.post('/games', json=test_game_obj,
                             headers=mk_def_auth_header(user.uuid))
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
        retrieved_username = self.curl.get(
            '/users/username/{}'.format(user_uuid)).asObject()
        self.assertTrue(user.uuid)
        self.assertEqual(username, retrieved_username.username)

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
                       headers=mk_def_auth_header(user_uuid), json=user)
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
        gm_headers = mk_def_auth_header(user.uuid)
        self.assertTrue(user.uuid)
        self.assertTrue(game.uuid)
        old_title = game.title
        game.title = "The hitchhiker's guide to " + old_title

        new_game = self.curl.post(
            '/games', json=game, headers=gm_headers).asObject()

        self.assertNotEqual(old_title, new_game.title)
        self.assertEqual(game.title, new_game.title)

    @with_user_and_game
    def test_join_game(self, user, game):
        user_uuid = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))
        user_uuid2 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))

        gm_headers = mk_def_auth_header(user.uuid)
        joiner_headers = mk_def_auth_header(user_uuid)
        others_headers = mk_def_auth_header(user_uuid2)

        participats_url = '/games/participants/{}'.format(game.uuid)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests)

        join_request = self.curl.post('/games/join/{}'.format(game.uuid),
                                      headers=joiner_headers).asObject()
        self.assertIsNotNone(join_request.requestStatus)

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers=others_headers).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests_seen_by_joiner = self.curl.get(
            participats_url, headers=joiner_headers).asDict()
        self.assertEqual(1, len(game_join_requests_seen_by_joiner))

        game_join_requests_seen_by_gm = self.curl.get(
            participats_url, headers=gm_headers).asDict()
        self.assertEqual(1, len(game_join_requests_seen_by_gm))

        join_request = Bunch(game_join_requests_seen_by_gm[0])
        self.assertEqual(models.PlayerInGameStatus.REQUEST_TO_JOIN.numerator,
                         join_request.requestStatus)
        join_request.requestStatus = \
            models.PlayerInGameStatus.CONFIRMED.numerator
        self.curl.post(participats_url, json=join_request,
                       headers=gm_headers).asDict()

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers=others_headers).asDict()
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

        gm_headers = mk_def_auth_header(user.uuid)
        joiner_headers = mk_def_auth_header(user_uuid)
        others_headers = mk_def_auth_header(user_uuid2)

        participats_url = '/games/participants/{}'.format(game.uuid)

        game_join_requests = self.curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests)

        join_request = self.curl.post('/games/join/{}'.format(game.uuid),
                                      headers=joiner_headers).asObject()
        self.assertIsNotNone(join_request.requestStatus)

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers=others_headers).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests = self.curl.get(
            participats_url, headers=gm_headers).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REQUEST_TO_JOIN.numerator,
                         join_request.requestStatus)
        join_request.requestStatus = \
            models.PlayerInGameStatus.REJECTED.numerator
        self.curl.post(participats_url, json=join_request,
                       headers=gm_headers).asDict()

        game_join_requests_seen_by_others = self.curl.get(
            participats_url, headers=mk_def_auth_header(user_uuid2)).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests_seen_by_joiner = self.curl.get(
            participats_url, headers=joiner_headers).asDict()
        self.assertEqual(1, len(game_join_requests_seen_by_joiner))

        game_join_requests = self.curl.get(
            participats_url, headers=gm_headers).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REJECTED.numerator,
                         join_request.requestStatus)
