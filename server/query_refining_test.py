import unittest
import uuid

from contextlib import contextmanager
from contexts import with_context
from copy import deepcopy
from datetime import datetime
from models import Game, GameStatus, User, UserAuth, Username, PlayerJoinGameRequest, PlayerInGameStatus
from query_refining import get_game_list
from session import SessionBroker
from sqlalchemy import create_engine
from sqlalchemy.orm import Session


user_auth = UserAuth(
    user_uuid="user|{}".format(uuid.uuid4()),
    public_key="tatatatatatatatatatatatatatatatatatatatatatatatatatatatatatata",
    authentication_strategy="Grenouille",
    timestamp_registered=datetime.now(),
)

user = User(
    uuid=user_auth.user_uuid,
    is_gm=True,
    is_player=True,
    location_lat=30,
    location_lon=30
)

username = Username(
    user_uuid=user.uuid,
    username="avestruz57"
)

game = Game(
    uuid="game|{}".format(uuid.uuid4()),
    title="Super cool name",
    description="Super fun game",
    game_status=GameStatus.CREATED,

    difficulty=10,
    is_campaign=False,
    min_players=1,
    max_players=10,
    location_lat=30,
    location_lon=30,
    number_of_sessions = 3,
    session_length_in_minutes = 60,
    universe="Lovecraft",

    organizational_details="Wawa dodo diti sono",
    precise_location="Avenue Piccard 44, 7218 Amsterdam",

    gm_user_uuid=user.uuid,
    timestamp_created= datetime.fromtimestamp(10000),
    timestamp_modified=datetime.fromtimestamp(10000)
)

user_auth1 = UserAuth(
    user_uuid="user|{}".format(uuid.uuid4()),
    public_key="dadadadadadadadadadadadadadadadadadadadadadadadadadadada",
    authentication_strategy="Crapaud",
    timestamp_registered=datetime.now()
)

user1 = User(
    uuid="This is a UUID",
    is_gm=True,
    is_player=False,
    location_lat=50,
    location_lon=50
)

username1 = Username(
    user_uuid=user1.uuid,
    username="lol"
)

game1 = Game(
    uuid="This is a game UUID",
    title="Super lame name",
    description="Super lame game",
    game_status=GameStatus.CONFIRMED,

    difficulty=5,
    is_campaign=True,
    min_players=1,
    max_players=2,
    location_lat=0,
    location_lon=0,
    number_of_sessions = 3,
    session_length_in_minutes = 120,
    universe="DnD",

    organizational_details="Yay",
    precise_location="Rue du Poulet 1, 1000 Lausanne",

    gm_user_uuid=user1.uuid,
    timestamp_created= datetime.fromtimestamp(100),
    timestamp_modified=datetime.fromtimestamp(100)
)



user_auth2 = UserAuth(
    user_uuid="user|{}".format(uuid.uuid4()),
    public_key="lalalalalalalalalalalalalalalalalalalalalalalalalala",
    authentication_strategy="Serpent",
    timestamp_registered=datetime.now()
)

user2 = User(
    uuid=user_auth2.user_uuid,
    is_gm=False,
    is_player=True,
    location_lat=90,
    location_lon=90
)

username2 = Username(
    user_uuid=user2.uuid,
    username="mdr"
)

game2 = Game(
    uuid="game|{}".format(uuid.uuid4()),
    title="Ultra great name",
    description="Ultra great game",
    game_status=GameStatus.CANCELLED,

    difficulty=1,
    is_campaign=False,
    min_players=1,
    max_players=5,
    location_lat=10,
    location_lon=10,
    number_of_sessions = 3,
    session_length_in_minutes = 180,
    universe="Other",

    organizational_details="ptdr",
    precise_location="Rue Incroyable 3, 2000 Neuchâtel",

    gm_user_uuid=user2.uuid,
    timestamp_created= datetime.fromtimestamp(1000),
    timestamp_modified=datetime.fromtimestamp(1000)
)

join_request = PlayerJoinGameRequest(
    join_request_id= "1",

    game_uuid="This is a game UUID",
    request_status=PlayerInGameStatus.CONFIRMED,
    user_uuid="This is a UUID"
)

join_request1 = PlayerJoinGameRequest(
    join_request_id= "2",

    game_uuid="This is a game UUID",
    request_status=PlayerInGameStatus.REQUEST_TO_JOIN,
    user_uuid="This is a UUID"
)

# Returns a simple session
def with_session(f):
    engine = create_engine('sqlite:///:memory:')
    return with_context(SessionBroker(engine=engine).get_session)(f)

# Returns a populated session
def with_full_session(f):
    @contextmanager
    @with_session
    def mk_session(session):
        session.add_all([deepcopy(user_auth), deepcopy(user_auth1), 
                        deepcopy(user_auth2), deepcopy(user), 
                        deepcopy(user1), deepcopy(user2), 
                        deepcopy(game), deepcopy(game1), 
                        deepcopy(game2), deepcopy(username), 
                        deepcopy(username1), deepcopy(username2), 
                        deepcopy(join_request), deepcopy(join_request1)])
        yield session
        session.flush()
        session.expunge_all()
    return with_context(mk_session)(f)

def compareTwoGames(game1, game2):
    # If the uuid is the same the games are the same
    return game1.uuid == game2.uuid

def compareArrayOfGames(array1, array2):
    if len(array1) == len(array2):
        for i in range(len(array1)):
            if not compareTwoGames(array1[i], array2[i]):
                return False
        return True
    return False

def create_default_test(self, session, correct_game, correct_filter, fail_filter, user = None):
    self.assertTrue(compareTwoGames(correct_game, get_game_list(session, correct_filter, []).one()))
    self.assertEqual(1, len(get_game_list(session, correct_filter, []).all()))
    self.assertEqual(0, len(get_game_list(session, fail_filter, []).all()))

class TestQueryRefining(unittest.TestCase):

    @with_full_session
    def test_db_queries_works(self, session):
        self.assertEqual(3, len(get_game_list(session, {}, []).all()))

    @with_full_session
    def test_difficulty_filter(self, session):
        create_default_test(self, session, game1, {"diff":5}, {"diff":100})

    @with_full_session
    def test_universe_filter(self, session):
        create_default_test(self, session, game1, {"universe":"DnD"}, {"universe":"lol"})

    @with_full_session
    def test_GM_filter(self, session):
        create_default_test(self, session, game1, {"with_GM":"This is a UUID"}, {"with_GM":"This is not a UUID"})

    @with_full_session
    def test_from_date_filter(self, session):
        create_default_test(self, session, game2, {"from_timestamp":180}, {"from_timestamp":200})

    @with_full_session
    def test_to_date_filter(self, session):
        create_default_test(self, session, game, {"to_timestamp":60}, {"to_timestamp":10})

    @with_full_session
    def test_title_filter(self, session):
        create_default_test(self, session, game, {"title_query":"cool"}, {"title_query":"ahahaha"})

    @with_full_session
    def test_confirmed_player_filter(self, session):
        create_default_test(self, session, game1, {"with_player":"This is a UUID"}, {"with_player":"ahahaha"})

    @with_full_session
    def test_requesting_player_filter(self, session):
        create_default_test(self, session, game1, {"with_requesting_player":"This is a UUID"}, {"with_requesting_player":"ahahaha"})

    @with_full_session
    def test_from_distance_filter(self, session):
        create_default_test(self, session, game1, {"from_distance":30}, {"from_distance":100}, user = user)

    @with_full_session
    def test_to_distance_filter(self, session):
        create_default_test(self, session, game, {"to_distance":1}, {"to_distance":-100}, user = user)

    @with_full_session
    def test_gusame_stat_filter(self, session):
        self.assertTrue(compareTwoGames(game, get_game_list(session, {"game_status":GameStatus.CREATED}, []).one()))
        self.assertEqual(1, len(get_game_list(session, {"game_status":GameStatus.CREATED}, []).all()))
        self.assertTrue(compareTwoGames(game1, get_game_list(session, {"game_status":GameStatus.CONFIRMED}, []).one()))
        self.assertEqual(1, len(get_game_list(session, {"game_status":GameStatus.CONFIRMED}, []).all()))
        self.assertTrue(compareTwoGames(game2, get_game_list(session, {"game_status":GameStatus.CANCELLED}, []).one()))
        self.assertEqual(1, len(get_game_list(session, {"game_status":GameStatus.CANCELLED}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"game_status":GameStatus.FINISHED}, []).all()))

    @with_full_session
    def test_difficulty_sort_asc(self, session):
        self.assertTrue(compareArrayOfGames([game2, game1, game], get_game_list(session, {}, ["asc_diff"]).all()))

    @with_full_session
    def test_difficulty_sort_dsc(self, session):
        self.assertTrue(compareArrayOfGames([game, game1, game2], get_game_list(session, {}, ["dsc_diff"]).all()))

    @with_full_session
    def test_date_sort_asc(self, session):
        self.assertTrue(compareArrayOfGames([game1, game2, game], get_game_list(session, {}, ["asc_date"]).all()))

    @with_full_session
    def test_date_sort_dsc(self, session):
        self.assertTrue(compareArrayOfGames([game, game2, game1], get_game_list(session, {}, ["dsc_date"]).all()))

    @with_full_session
    def test_max_players_sort_asc(self, session):
        self.assertTrue(compareArrayOfGames([game1, game2, game], get_game_list(session, {}, ["asc_max_player"]).all()))

    @with_full_session
    def test_max_players_sort_dsc(self, session):
        self.assertTrue(compareArrayOfGames([game, game2, game1], get_game_list(session, {}, ["dsc_max_player"]).all()))

    @with_full_session
    def test_distance_sort_asc(self, session):
        self.assertTrue(compareArrayOfGames([game, game2, game1], get_game_list(session, {}, ["asc_distance"], user = user).all()))

    @with_full_session
    def test_distance_sort_dsc(self, session):
        self.assertTrue(compareArrayOfGames([game1, game2, game], get_game_list(session, {}, ["dsc_distance"], user = user).all()))
