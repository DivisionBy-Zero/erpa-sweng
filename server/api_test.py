from bunch import Bunch
from contextlib import contextmanager
from datetime import datetime
from typing import Tuple
import base64
import ed25519
import json
import os
import unittest

from api import app
from contexts import with_context_using_instance
import models


signing_key, verifying_key = ed25519.create_keypair()
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
    def __init__(self, app, token=None):
        self.app = app
        self.token = token

    def with_session(self, token: str):  # typing: MyCurl:
        return MyCurl(self.app, token)

    def __getattr__(self, x):
        def mk_wrapped_call(*args, **kwargs):
            headers = {} if 'headers' not in kwargs else kwargs['headers']
            if self.token and 'Authorization' not in headers:
                headers.update({'Authorization': self.token})
            kwargs.update({'headers': headers})
            res = getattr(self.app, x)(*args, **kwargs)
            res.asDict = lambda: json.loads(res.data)
            res.asStr = lambda: res.data.decode('UTF-8')
            res.asObject = lambda: Bunch(res.asDict())
            return res
        return mk_wrapped_call


def mk_default_auth(username: str, user_uuid: str) -> models.UserAuth:
    x509b = base64.b64decode('MCowBQYDK2VwAyEA') + verifying_key.to_bytes()
    return models.UserAuth(public_key=base64.b64encode(x509b).decode('UTF-8'),
                           user_uuid=user_uuid)


def mk_def_auth_header(user_uuid):
    return {'Authorization': user_uuid}


def create_and_register_username(
        curl, username) -> Tuple[str, models.UserSessionToken]:
    user_uuid = curl.post('/users/newuser/{}'.format(username)).asStr()
    curl.post('/users/register_auth',
              json=mk_default_auth(username, user_uuid))
    challenge = curl.get('/auth/challenge/{}'.format(user_uuid)).asStr()
    signature_b = signing_key.sign(base64.b64decode(challenge.encode('UTF-8')))
    signature_str = base64.b64encode(signature_b).decode('UTF-8')
    session = curl.post('/auth/challenge/{}'.format(user_uuid),
                        data=signature_str).asObject()
    curl.post('/users',
              json={'is_gm': True, 'is_player': True, 'uuid': user_uuid})
    return user_uuid, session.sessionToken


def with_user_and_token(f):
    @contextmanager
    def mk_user(test):
        def_username = "Lavoisier"
        username = def_username + str(datetime.now())
        user_uuid, token = create_and_register_username(test.curl, username)
        yield type('user', (object,), {'uuid': user_uuid}), token
    return with_context_using_instance(mk_user)(f)


def with_user_token_and_game(f):
    @contextmanager
    @with_user_and_token
    def mk_game(test, user, token):
        res = test.curl.with_session(token).post('/games', json=test_game_obj)
        if res.status_code != 200:
            raise Exception("Error performing request [{}]: {}"
                            "".format(res.status_code, res.data))
        yield user, token, res.asObject()
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
        user_uuid, token = create_and_register_username(self.curl, username)
        self.assertIsNotNone(token)
        self.curl.token = token
        self.assertEqual(user_uuid, self.curl.get(user_url).asStr())
        user = self.curl.get('/users/uuid/{}'.format(user_uuid)).asObject()
        retrieved_username = self.curl.get(
            '/users/username/{}'.format(user_uuid)).asObject()
        self.assertTrue(user.uuid)
        self.assertEqual(username, retrieved_username.username)

    def test_update_user(self):
        username = user_prefix + str(datetime.now())
        user_url = '/users/user/{}'.format(username)
        user_uuid, token = create_and_register_username(self.curl, username)
        self.curl.token = token
        self.assertEqual(user_uuid, self.curl.get(user_url).asStr())
        user = self.curl.get('/users/uuid/{}'.format(user_uuid)).asObject()
        self.assertIsNotNone(user)
        self.assertIsNotNone(user.isGm)

        old_isgm = user.isGm
        user.isGm = False if user.isGm else True
        self.curl.post('/users/uuid/{}'.format(user_uuid), json=user)
        new_user = self.curl.get('/users/uuid/{}'.format(user_uuid)).asObject()
        self.assertNotEqual(old_isgm, user.isGm)
        self.assertNotEqual(old_isgm, new_user.isGm)

    def test_get_games(self):
        self.app.get('/games')

    @with_user_token_and_game
    def test_create_game(self, user, token, game):
        self.assertTrue(user.uuid)
        self.assertTrue(game.uuid)
        self.curl.token = token
        game_dict = self.curl.get('/games/uuid/{}'.format(game.uuid)).asDict()
        for gpk, gpv in test_game_obj.items():
            self.assertIn(gpk, game_dict)
            self.assertEqual(str(gpv), str(game_dict[gpk]))

    @with_user_token_and_game
    def test_update_game(self, user, token, game):
        self.curl.token = token
        self.assertTrue(user.uuid)
        self.assertTrue(game.uuid)
        old_title = game.title
        game.title = "The hitchhiker's guide to " + old_title

        new_game = self.curl.post('/games', json=game).asObject()

        self.assertNotEqual(old_title, new_game.title)
        self.assertEqual(game.title, new_game.title)

    @with_user_token_and_game
    def test_join_game(self, user, token, game):
        user_uuid1, token1 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))
        user_uuid2, token2 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))

        gm_curl = self.curl.with_session(token)
        joiner_curl = self.curl.with_session(token1)
        others_curl = self.curl.with_session(token2)

        participats_url = '/games/participants/{}'.format(game.uuid)

        game_join_requests = gm_curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests)

        join_request = joiner_curl.post('/games/join/{}'
                                        ''.format(game.uuid)).asObject()
        self.assertIsNotNone(join_request.requestStatus)

        game_join_requests_seen_by_others = \
            others_curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests_seen_by_joiner = \
            joiner_curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests_seen_by_joiner))

        game_join_requests_seen_by_gm = gm_curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests_seen_by_gm))

        join_request = Bunch(game_join_requests_seen_by_gm[0])
        self.assertEqual(models.PlayerInGameStatus.REQUEST_TO_JOIN.numerator,
                         join_request.requestStatus)
        join_request.requestStatus = \
            models.PlayerInGameStatus.CONFIRMED.numerator
        gm_curl.post(participats_url, json=join_request).asDict()

        game_join_requests_seen_by_others = \
            others_curl.get(participats_url).asDict()
        self.assertTrue(game_join_requests_seen_by_others)
        join_request = Bunch(game_join_requests_seen_by_others[0])
        self.assertEqual(models.PlayerInGameStatus.CONFIRMED.numerator,
                         join_request.requestStatus)

    @with_user_token_and_game
    def test_join_game_after_reject(self, user, token, game):
        user_uuid, token1 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))
        user_uuid2, token2 = create_and_register_username(
            self.curl, user_prefix + str(datetime.now()))

        gm_curl = self.curl.with_session(token)
        joiner_curl = self.curl.with_session(token1)
        others_curl = self.curl.with_session(token2)

        participats_url = '/games/participants/{}'.format(game.uuid)

        game_join_requests = gm_curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests)

        join_request = joiner_curl.post('/games/join/{}'
                                        ''.format(game.uuid)).asObject()
        self.assertIsNotNone(join_request.requestStatus)

        game_join_requests_seen_by_others = \
            others_curl.get(participats_url).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests = gm_curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REQUEST_TO_JOIN.numerator,
                         join_request.requestStatus)
        join_request.requestStatus = \
            models.PlayerInGameStatus.REJECTED.numerator
        gm_curl.post(participats_url, json=join_request).asDict()

        game_join_requests_seen_by_others = gm_curl.get(
            participats_url, headers=mk_def_auth_header(user_uuid2)).asDict()
        self.assertFalse(game_join_requests_seen_by_others)

        game_join_requests_seen_by_joiner = \
            joiner_curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests_seen_by_joiner))

        game_join_requests = gm_curl.get(participats_url).asDict()
        self.assertEqual(1, len(game_join_requests))

        join_request = Bunch(game_join_requests[0])
        self.assertEqual(models.PlayerInGameStatus.REJECTED.numerator,
                         join_request.requestStatus)
