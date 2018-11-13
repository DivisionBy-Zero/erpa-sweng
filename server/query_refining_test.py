import unittest
import uuid
from datetime import datetime
from sqlalchemy import create_engine

from contexts import with_context
from models import Game, GameStatus, User, UserAuth, Username, PlayerJoinGameRequest, PlayerInGameStatus
from session import SessionBroker
from query_refining import get_game_list


def with_session(f):
    engine = create_engine('sqlite:///:memory:')
    return with_context(SessionBroker(engine=engine).get_session)(f)


class TestQueryRefining(unittest.TestCase):

    @with_session
    def test_db_queries_works(self, session):
        
        user_auth = UserAuth(
            user_uuid="user|{}".format(uuid.uuid4()),
            public_key="tatatatatatatatatatatatatatatatatatatatatatatatatatatatatatata",
            authentication_strategy="Grenouille",
            timestamp_registered=datetime.now(),
        )

        user = User(
            uuid=user_auth.user_uuid,
            isGm=True,
            isPlayer=True,
            location_lat=30,
            location_lon=30,
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
            duration=60,
            is_campaign=False,
            max_players=10,
            location_lat=0,
            location_lon=0,
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
            timestamp_registered=datetime.now(),
        )

        user1 = User(
            uuid="This is a UUID",
            isGm=True,
            isPlayer=False,
            location_lat=50,
            location_lon=50,
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
            duration=120,
            is_campaign=True,
            max_players=2,
            location_lat=0,
            location_lon=0,
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
            isGm=False,
            isPlayer=True,
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
            duration=180,
            is_campaign=False,
            max_players=5,
            location_lat=0,
            location_lon=0,
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

        session.add_all([user_auth, user_auth1, user_auth2, user, user1, user2, game, game1, game2, username, username1, username2, join_request, join_request1])

        # Test if we get all the games if no filter
        self.assertEqual(3, len(get_game_list(session, {}, []).all()))

        # Test if difficulty filter works
        self.assertEqual(game1, get_game_list(session, {"diff":5}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"diff":5}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"diff":100}, []).all()))

        # Test if universe filter works
        self.assertEqual(game1, get_game_list(session, {"universe":"DnD"}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"universe":"DnD"}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"universe":"lol"}, []).all()))

        # Test if GM filter works
        self.assertEqual(game1, get_game_list(session, {"with_GM":"This is a UUID"}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"with_GM":"This is a UUID"}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"with_GM":"This is not a UUID"}, []).all()))

        # Test if the from date filter  works
        self.assertEqual(game2, get_game_list(session, {"from_timestamp":180}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"from_timestamp":180}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"from_timestamp":200}, []).all()))

        #Test if the to date filter works
        self.assertEqual(game,  get_game_list(session, {"to_timestamp":60}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"to_timestamp":60}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"to_timestamp":10}, []).all()))

        # Test if the title filter works
        self.assertEqual(game,  get_game_list(session, {"title_query":"cool"}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"title_query":"cool"}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"title_query":"ahahaha"}, []).all()))

        # Test if the confirmed player filter works
        self.assertEqual(game1, get_game_list(session, {"with_player":"This is a UUID"}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"with_player":"This is a UUID"}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"with_player":"ahahaha"}, []).all()))

        # Test if the requesting player filter works
        self.assertEqual(game1, get_game_list(session, {"with_requesting_player":"This is a UUID"}, []).one())
        self.assertEqual(1, len(get_game_list(session, {"with_requesting_player":"This is a UUID"}, []).all()))
        self.assertEqual(0, len(get_game_list(session, {"with_requesting_player":"ahahaha"}, []).all()))

        # Test if the ascending difficulty sort works
        self.assertEqual([game2, game1, game], get_game_list(session, {}, ["asc_diff"]).all())
        # Test if the descending difficulty sort works
        self.assertEqual([game, game1, game2], get_game_list(session, {}, ["dsc_diff"]).all())

        # Test if the ascending date sort works
        self.assertEqual([game1, game2, game], get_game_list(session, {}, ["asc_date"]).all())
        # Test if the descending date sort works
        self.assertEqual([game, game2, game1], get_game_list(session, {}, ["dsc_date"]).all())

        # Test if the ascending max number of players works
        self.assertEqual([game1, game2, game], get_game_list(session, {}, ["asc_max_player"]).all())
        # Test if the descending max number of players works
        self.assertEqual([game, game2, game1], get_game_list(session, {}, ["dsc_max_player"]).all())