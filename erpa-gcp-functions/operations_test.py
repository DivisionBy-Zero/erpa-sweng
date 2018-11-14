from contextlib import contextmanager
from sqlalchemy import create_engine
import pytest
import unittest
import uuid

from contexts import with_context
from session import SessionBroker
from operations import Operations
import models


def with_operations(f):
    @contextmanager
    def mk_ops():
        engine = create_engine('sqlite:///:memory:')
        yield Operations(session_broker=SessionBroker(engine=engine))
    return with_context(mk_ops)(f)


username_str = "avestruz57"
test_user_auth_obj = {'public_key': "Hello, I'm a BASE64 public key :D"}
test_user_obj = {'is_gm': True, 'is_player': True}
test_game_obj = {'title': "My super game",
                 'description': "A super game description",
                 'difficulty': 10,
                 'is_campaign': 0,
                 'location_lat': 0.0,
                 'location_lon': 0.0,
                 'max_players': 0,
                 'min_players': 0,
                 'session_length_in_minutes': 0,
                 'universe': "Marvel"}


def mk_user(**overrides):
    return models.User(**{**test_user_obj, **overrides})


def mk_game(**overrides):
    return models.Game(**{**test_game_obj, **overrides})


def create_and_register_username(username, ops):
    new_user_uuid = ops.register_username(username)
    user_auth_obj = {**test_user_auth_obj, 'user_uuid': new_user_uuid}
    ops.register_user_auth(models.UserAuth(**user_auth_obj))
    return ops.register_user(mk_user(uuid=new_user_uuid))


def with_operations_and_user(f):
    @contextmanager
    @with_operations
    def register_user(ops):
        yield (ops, create_and_register_username(username_str, ops))
    return with_context(register_user)(f)


class TestOperations(unittest.TestCase):
    @with_operations
    @with_context(pytest.raises, KeyError)
    def test_mk_same_username_twice_excepts(self, ops, _):
        self.assertIsNotNone(ops.register_username(username_str))
        ops.register_username(username_str)

    @with_operations
    def test_mk_user(self, ops):
        created_user_uuid = ops.register_username(username_str)
        ops.register_user(mk_user(uuid=created_user_uuid))
        ops.get_user(created_user_uuid)

        user_uuid_from_username = ops.get_user_uuid_from_username(username_str)
        self.assertIsNotNone(user_uuid_from_username)
        self.assertEqual(created_user_uuid, user_uuid_from_username)

    @with_operations
    def test_register_new_user(self, ops):
        new_user_uuid = ops.register_username(username_str)

        user_auth_obj = {**test_user_auth_obj, 'user_uuid': new_user_uuid}
        user_auth = ops.register_user_auth(models.UserAuth(**user_auth_obj))

        self.assertIsNotNone(user_auth)
        self.assertIsNotNone(user_auth.authentication_strategy)
        self.assertIsNotNone(user_auth.timestamp_registered)

        registered_user = ops.register_user(mk_user(uuid=new_user_uuid))

        self.assertIsNotNone(registered_user.timestamp_created)

    @with_operations_and_user
    def test_update_user(self, ops, user):
        old_gm = user.is_gm
        user.is_gm = False if user.is_gm else True

        ops.update_user(user, user)
        new_user = ops.get_user(user.uuid)

        self.assertNotEqual(old_gm, user.is_gm)
        self.assertEqual(user.is_gm, new_user.is_gm)

    @with_operations_and_user
    def test_create_game_invalid_uuid(self, ops, user):
        a_uuid = str(uuid.uuid4())

        created_game = ops.create_game(mk_game(uuid=a_uuid), user)

        self.assertNotEqual(created_game.uuid, a_uuid)
        self.assertEqual(created_game.gm_user_uuid, user.uuid)
        self.assertIsNotNone(created_game.timestamp_created)
        self.assertIsNotNone(created_game.timestamp_modified)

    @with_operations_and_user
    def test_create_game_twice_yields_two_uuid(self, ops, user):
        created_game = ops.create_game(mk_game(), user)
        first_game_uuid = created_game.uuid
        created_game = ops.create_game(mk_game(), user)

        self.assertNotEqual(first_game_uuid, created_game.uuid)

    @with_operations_and_user
    def test_update_existing_game(self, ops, user):
        created_game = ops.create_game(mk_game(), user)
        previous_description = created_game.description
        previous_modified_timestamp = created_game.timestamp_modified
        created_game.description = previous_description + previous_description
        updated_game = ops.update_game(created_game, user)

        self.assertNotEqual(previous_description, updated_game.description)
        self.assertNotEqual(created_game, updated_game)
        self.assertTrue(previous_modified_timestamp
                        < updated_game.timestamp_modified)

    @with_operations_and_user
    @with_context(pytest.raises, KeyError)
    def test_update_not_existing_game(self, ops, user, _):
        game = mk_game()
        ops.update_game(game, user)

    @with_operations_and_user
    def test_get_game(self, ops, user):
        created_game = ops.create_game(mk_game(), user)
        retrieved_game = ops.get_game(created_game.uuid)

        stored_game_dict = retrieved_game.__json__()
        # We can't directly compare the internal representation with the dto
        [self.assertEqual(str(stored_game_dict[k]), str(v))
            for k, v in created_game.__json__().items()]

    @with_operations_and_user
    def test_get_games(self, ops, user):
        for i in range(0, 100):
            ops.create_game(mk_game(difficulty=i), user)

        stored_games = ops.get_games()

        self.assertIsNotNone(stored_games)
        self.assertTrue(len(stored_games) > 0)
        [self.assertIsNotNone(game) for game in stored_games]

    @with_operations_and_user
    def test_get_user_from_session_token(self, ops, user):
        new_session_token = ops.register_session_token(user.uuid)

        self.assertIsNotNone(new_session_token)

        user_from_token = ops.get_user_from_session_token(
                new_session_token.session_token)

        self.assertEqual(user_from_token.uuid, user.uuid)

    @with_operations_and_user
    def test_get_user_tokens(self, ops, user):
        for i in range(0, 5):
            ops.register_session_token(user.uuid)

        tokens = ops.get_user_tokens_for_user(user.uuid)

        self.assertEqual(5, len(tokens))
        [self.assertEqual(session_token.user_uuid, user.uuid)
            for session_token in tokens]

    @with_operations_and_user
    def test_register_and_retrieve_auth_challenges(self, ops, user):
        challenge = ops.register_auth_challenge(user.uuid)
        retrieved_challenge = ops.get_auth_challenge_for_user(user.uuid)

        self.assertEqual(challenge.user_uuid, user.uuid)
        self.assertEqual(challenge.user_uuid, retrieved_challenge.user_uuid)
        self.assertEqual(challenge.user_challenge,
                         retrieved_challenge.user_challenge)

    @with_operations_and_user
    def test_player_joins_game(self, ops, user):
        game = ops.create_game(mk_game(), user)
        u1 = create_and_register_username("a user", ops)

        self.assertFalse(ops.get_game_participants(game.uuid, None))
        self.assertFalse(ops.get_game_participants(game.uuid, user))
        self.assertFalse(ops.get_game_participants(game.uuid, u1))
        # User requests to join game
        request = ops.join_game(game.uuid, u1)

        self.assertFalse(ops.get_game_participants(game.uuid, None))
        self.assertTrue(ops.get_game_participants(game.uuid, user))
        self.assertTrue(ops.get_game_participants(game.uuid, u1))
        request.request_status = models.PlayerInGameStatus.CONFIRMED
        # Request is confirmed
        ops.update_game_join_request(game.uuid, user, request)

        self.assertTrue(ops.get_game_participants(game.uuid, None))
        self.assertTrue(ops.get_game_participants(game.uuid, user))
        self.assertTrue(ops.get_game_participants(game.uuid, u1))

    @with_operations_and_user
    def test_player_joining_after_removed(self, ops, user):
        game = ops.create_game(mk_game(), user)
        u1 = create_and_register_username("a user", ops)

        request = ops.join_game(game.uuid, u1)
        request.request_status = models.PlayerInGameStatus.REMOVED
        ops.update_game_join_request(game.uuid, user, request)

        request = ops.join_game(game.uuid, u1)

    @with_operations_and_user
    @with_context(pytest.raises, ValueError)
    def test_gm_joining_his_own_game(self, ops, user, _):
        game = ops.create_game(mk_game(), user)
        ops.join_game(game.uuid, user)

    @with_operations_and_user
    @with_context(pytest.raises, ValueError)
    def test_player_joining_after_rejected(self, ops, user, _):
        game = ops.create_game(mk_game(), user)
        u1 = create_and_register_username("a user", ops)

        request = ops.join_game(game.uuid, u1)
        request.request_status = models.PlayerInGameStatus.REJECTED
        ops.update_game_join_request(game.uuid, user, request)

        request = ops.join_game(game.uuid, u1)
