from contextlib import contextmanager
from datetime import datetime
from functools import wraps
import unittest
import uuid

from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from contexts import with_context
from game_refiner import refine_query, refine_queries
from models import Game, GameStatus, PlayerJoinGameRequest, \
        PlayerInGameStatus, User, UserUuid, Username
from session import SessionBroker


class T:
    @staticmethod
    def with_session(f):
        @wraps(f)
        @contextmanager
        def mk_session():
            engine = create_engine('sqlite:///:memory:')
            SessionBroker.maybe_initialize_tables(engine)
            with SessionBroker(engine=engine).get_session() as session:
                session.autocommit = True
                yield session
        return with_context(mk_session)(f)

    @staticmethod
    def with_session_as_kwarg(f):
        @T.with_session
        def runner(*args, **kwargs):
            kwargs.update({'session': args[-1]})
            return f(*args[0:len(args)-1], **kwargs)
        return runner

    @staticmethod
    def with_username(username_or_fn=None, no_inject=False):
        """Creates a new Username entity which is appended as last argument in
        the function call. Requires a `session` element to be present in
        kwargs.
        If no_inject is true, the created entity will not be appended.
        """
        username = "user-{}".format(datetime.now())

        def dec(f):
            @wraps(f)
            def mk_entity(*args, **kwargs):
                session = kwargs['session']
                user_uuid = UserUuid()
                user_uuid.generate_new_uuid()
                session.add(user_uuid)
                entity = Username(username=username,
                                  user_uuid=user_uuid.user_uuid)
                session.add(entity)
                args = args if no_inject else args + (entity,)
                return f(*args, **kwargs)
            return mk_entity
        if callable(username_or_fn):
            return dec(username_or_fn)
        username = username_or_fn if username_or_fn else username
        return dec

    @staticmethod
    def with_userprofile(skip_or_fn):
        """Uses the property `user_uuid` from the last argument to the decorated
        function to construct a new User entity which is appended as
        last argument in the function call. Requires a `session` element to be
        present in kwargs.
        If no_inject is true, the created entity will not be appended.
        """
        no_inject = False

        def dec(f):
            @wraps(f)
            def mk_entity(*args, **kwargs):
                user_uuid = getattr(args[-1], 'user_uuid')
                session = kwargs['session']
                user = User(uuid=user_uuid, is_gm=True, is_player=True,
                            location_lat=0, location_lon=0)
                session.add(user)
                args = args if no_inject else args + (user,)
                return f(*args, **kwargs)
            return mk_entity
        if callable(skip_or_fn):
            return dec(skip_or_fn)
        return dec

    @staticmethod
    def with_game(title_or_fn=None, no_inject=False):
        """Uses the property `user_uuid` from the last argument to the decorated
        function as gm_user_uuid to construct a new game which is appended as
        last argument in the function call. Requires a `session` element to be
        present in kwargs.
        If no_inject is true, the created entity will not be appended.
        """
        title = "t-{}".format(datetime.now())

        def dec(f):
            @wraps(f)
            def mk_entity(*args, **kwargs):
                user_uuid = getattr(args[-1], 'user_uuid')
                session = kwargs['session']
                game = Game(uuid="game|{}".format(uuid.uuid4()),
                            gm_user_uuid=user_uuid, title=title,
                            description="Super fun game", difficulty=10,
                            game_status=GameStatus.CREATED, is_campaign=False,
                            location_lat=0, location_lon=0, max_players=10,
                            min_players=10, session_length_in_minutes=120,
                            number_of_sessions=2, universe="Minecraft",
                            organizational_details="Wubba lubba dub dub.",
                            precise_location="Avenue Piccard, 7218 Amsterdam")
                session.add(game)
                args = args if no_inject else args + (game,)
                return f(*args, **kwargs)
            return mk_entity
        if callable(title_or_fn):
            return dec(title_or_fn)
        title = title_or_fn if title_or_fn else title
        return dec


class TestGameRefiner(unittest.TestCase):
    @T.with_session_as_kwarg
    @T.with_username
    @T.with_game("I'm More Than A Man, I'm More Than A Plane!")
    @T.with_username
    @T.with_game(no_inject=True)
    @T.with_game(no_inject=True)
    @T.with_game(no_inject=True)
    @T.with_game(no_inject=True)
    @T.with_game(no_inject=True)
    @T.with_game
    def test_refine_singletons(self, u1, g1, *_, session: Session):
        retrieved_game = session.query(Game).filter(Game.uuid == g1.uuid)
        self.assertIsNotNone(retrieved_game)

        g1.difficulty = 20
        g1.game_status = GameStatus.IN_PROGRESS
        g1.session_length_in_minutes = 20
        session.add(g1)

        qGi = lambda: session.query(Game.uuid)

        self.assertEqual(g1.uuid, refine_query(qGi(), 'difficulty', '20').all()[-1][0])
        self.assertEqual(g1.uuid, refine_query(qGi(), 'sort_difficulty', 'asc').all()[-1][0])
        self.assertEqual(g1.uuid, refine_query(qGi(), 'sort_difficulty', 'desc').all()[0][0])
        self.assertEqual(g1.uuid, refine_query(qGi(), 'with_gm', u1.user_uuid).all()[0][0])
        self.assertEqual(g1.uuid, refine_query(qGi(), 'title_query', 'than a plane').scalar())
        self.assertEqual(g1.uuid, refine_queries(qGi(), {'min_minutes': '5', 'max_minutes': '30'}).scalar())

    @T.with_session_as_kwarg
    @T.with_username
    @T.with_game
    @T.with_username
    @T.with_game
    @T.with_username
    @T.with_game
    def test_refine_all(self, u1, g1, u2, g2, u3, g3, *_, session: Session):
        g1.min_players, g1.max_players = 5, 15
        g2.min_players, g2.max_players = 4, 14
        g3.min_players, g3.max_players = 3, 13
        session.add_all([g1, g2, g3])

        qGi = lambda: session.query(Game.uuid)

        asc = refine_query(qGi(), 'sort_min_players', 'asc').all()
        desc = refine_query(qGi(), 'sort_min_players', 'desc').all()
        self.assertEqual(asc[::-1], desc)

        asc = refine_query(qGi(), 'sort_max_players', 'asc').all()
        desc = refine_query(qGi(), 'sort_max_players', 'desc').all()
        self.assertEqual(asc[::-1], desc)

        asc = refine_query(qGi(), 'sort_date', 'asc').all()
        desc = refine_query(qGi(), 'sort_date', 'desc').all()
        self.assertEqual(asc[::-1], desc)

    @T.with_session_as_kwarg
    @T.with_username
    @T.with_game
    @T.with_username
    @T.with_game
    @T.with_username
    @T.with_game
    def test_player_requests_queries(self, u1, g1, u2, g2, u3, g3, *_,
                                     session: Session):
        membership_state_1 = PlayerJoinGameRequest(
                game_uuid=g2.uuid, user_uuid=u1.user_uuid,
                request_status=PlayerInGameStatus.REQUEST_TO_JOIN)
        membership_state_2 = PlayerJoinGameRequest(
                game_uuid=g3.uuid, user_uuid=u1.user_uuid,
                request_status=PlayerInGameStatus.CONFIRMED)
        g3.game_status = GameStatus.FINISHED
        session.add_all([membership_state_1, membership_state_2, g3])

        pending_game = refine_query(session.query(Game.uuid),
                                    'player_pending', u1.user_uuid).scalar()
        self.assertEqual(g2.uuid, pending_game)

        confirmed_game = refine_query(session.query(Game.uuid),
                                      'player_confirmed', u1.user_uuid).scalar()
        self.assertEqual(g3.uuid, confirmed_game)

        finished_game = refine_query(session.query(Game.uuid),
                                     'game_status', 'FINISHED').scalar()
        self.assertEqual(g3.uuid, finished_game)
