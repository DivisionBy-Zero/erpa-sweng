from contextlib import contextmanager
from datetime import datetime
import unittest
import uuid
import pytest

from sqlalchemy import create_engine
from sqlalchemy.exc import IntegrityError

from contexts import with_context
from models import Game, GameStatus, User, UserAuth, Username, UserUuid
from session import SessionBroker


def with_session(f):
    @contextmanager
    def mk_session():
        engine = create_engine('sqlite:///:memory:')
        SessionBroker.maybe_initialize_tables(engine)
        with SessionBroker(engine=engine).get_session() as session:
            session.execute('pragma foreign_keys=on')  # https://stackoverflow.com/a/31797403
            yield session
    return with_context(mk_session)(f)


username_str = "avestruz57"


class TestDBSession(unittest.TestCase):
    def test_session_test_context_unique(self):
        @with_session
        def test_create_user_in_session(session):
            user_uuid = UserUuid()
            user_uuid.generate_new_uuid()
            session.add(user_uuid)
            session.flush()

            username = Username(username=username_str,
                                user_uuid=user_uuid.user_uuid)
            session.add(username)
            self.assertEqual(username, session.query(Username).one())

        # When called twice with the same user, both sessions should be different
        test_create_user_in_session()
        test_create_user_in_session()

    @with_session
    def test_mk_user(self, session):
        user_uuid = UserUuid()
        user_uuid.generate_new_uuid()
        username = Username(username=username_str, user_uuid=user_uuid.user_uuid)

        session.add_all([user_uuid, username])
        # Flush the user_uuid first, since the username depends on its PK
        session.flush([user_uuid])

        self.assertEqual(username, session.query(Username).one())
        self.assertEqual(user_uuid, session.query(UserUuid).one())

    @with_session
    def test_mk_user_uuid(self, session):
        user_uuid = UserUuid()
        self.assertIsNone(user_uuid.user_uuid)
        session.add(user_uuid)
        self.assertEqual(user_uuid, session.query(UserUuid).one())
        self.assertIsNotNone(user_uuid.user_uuid)

    @with_session
    @with_context(pytest.raises, IntegrityError)
    def test_mk_user_without_existing_uuid_should_fail(self, session, _):
        user_uuid = UserUuid()
        user_uuid.generate_new_uuid()
        username = Username(username=username_str, user_uuid=user_uuid.user_uuid)

        session.add(username)
        session.flush()
        self.assertEqual(username, session.query(Username).one())
        username, session.query(UserUuid).one()


    @with_session
    def test_db_storage_backend(self, session):
        user_uuid = UserUuid()
        user_uuid.generate_new_uuid()
        session.add(user_uuid)
        session.flush()

        username = Username(
            username=username_str,
            user_uuid=user_uuid.user_uuid
        )
        session.add(username)
        session.flush()

        user_auth = UserAuth(
            user_uuid=username.user_uuid,
            public_key="tatatatatatatatatatatatatatatatatatatatatatatatatatatatatatata",
            authentication_strategy="Grenouille",
        )
        session.add(user_auth)
        session.flush()

        user = User(
            uuid=user_auth.user_uuid,
            is_gm=True,
            is_player=True,
            location_lat=0,
            location_lon=0,
        )
        session.add(user)
        session.flush()

        game = Game(
            uuid="game|".format(uuid.uuid4()),
            title="Super cool name",
            description="Super fun game",
            game_status=GameStatus.CREATED,

            difficulty=10,
            is_campaign=False,
            max_players=10,
            location_lat=0,
            location_lon=0,
            max_players=10,
            min_players=10,
            number_of_sessions=2,
            session_length_in_minutes=120,
            universe="Lovecraft",

            organizational_details="Wawa dodo diti sono",
            precise_location="Avenue Piccard 44, 7218 Amsterdam",

            gm_user_uuid=user.uuid,
        )
        session.add(game)
        session.flush()

        self.assertEqual(game, session.query(Game).one())
        self.assertEqual(user, session.query(User).one())
        self.assertEqual(user_auth, session.query(UserAuth).one())
        self.assertEqual(username, session.query(Username).one())
