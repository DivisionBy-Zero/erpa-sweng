# pylint: skip-file
# flake8: noqa

from datetime import datetime
import uuid

from dictalchemy import DictableModel
from flask_jsontools import JsonSerializableBase
from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.types import Enum, Float
import enum

Base = declarative_base(cls=(JsonSerializableBase, DictableModel))

def mk_uuid_gen(prefix):
    return lambda: "{}|{}".format(prefix, uuid.uuid4())

mk_auth_challenge = mk_uuid_gen('challenge')
mk_player_join_game_request = mk_uuid_gen('join_game')
mk_session_token = mk_uuid_gen('session')
mk_user_uuid = mk_uuid_gen('user')


class PlayerInGameStatus(enum.IntEnum):
    REQUEST_TO_JOIN = 1
    CONFIRMED = 2
    REJECTED = 3
    REMOVED = 4
    HAS_QUIT = 5


class GameStatus(enum.IntEnum):
    CREATED = 1
    CONFIRMED = 2
    CANCELLED = 3
    IN_PROGRESS = 4
    FINISHED = 5


class Game(Base):
    __tablename__              = 'games'

    uuid                       = Column(String, primary_key=True)
    title                      = Column(String, nullable=False)
    description                = Column(String(1200), nullable=False)
    game_status                = Column(Enum(GameStatus), nullable=False)

    difficulty                 = Column(Integer, nullable=False)
    is_campaign                = Column(String, nullable=False)
    location_lat               = Column(Float, nullable=False)  # ie. City/District Center
    location_lon               = Column(Float, nullable=False)  # ie. City/District Center
    max_players                = Column(Integer, nullable=False)
    min_players                = Column(Integer, nullable=False)
    number_of_sessions         = Column(Integer)
    session_length_in_minutes  = Column(Integer)
    universe                   = Column(String, nullable=False)

    organizational_details     = Column(String(1200))
    precise_location           = Column(String)  # ie. Full address

    gm_user_uuid               = Column(String, ForeignKey('user_uuids.user_uuid'), nullable=False)
    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)
    timestamp_modified         = Column(DateTime(timezone=False), nullable=False, default=datetime.now)

    def generate_new_uuid(self):
        self.uuid = "game|{}".format(uuid.uuid4())

    def uuid_is_valid(self):
        return str(self.uuid).startswith("game|")


class PlayerJoinGameRequest(Base):
    __tablename__              = 'player_join_game_requests'
    join_request_id            = Column(String, primary_key=True, default=mk_player_join_game_request)

    game_uuid                  = Column(String, ForeignKey('games.uuid'), nullable=False)
    request_status             = Column(Enum(PlayerInGameStatus), nullable=False, default=PlayerInGameStatus.REQUEST_TO_JOIN)
    user_uuid                  = Column(String, ForeignKey('users.uuid'), nullable=False)

    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)
    timestamp_modified         = Column(DateTime(timezone=False), nullable=False, default=datetime.now)


class User(Base):
    __tablename__              = 'users'

    uuid                       = Column(String, ForeignKey('user_uuids.user_uuid'), primary_key=True)
    is_gm                      = Column(Boolean, nullable=False)
    is_player                  = Column(Boolean, nullable=False)
    location_lat               = Column(Float)  # ie. City/District Center
    location_lon               = Column(Float)  # ie. City/District Center

    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)


class UserAuth(Base):
    __tablename__              = 'user_auths'

    user_uuid                  = Column(String, ForeignKey('user_uuids.user_uuid'), primary_key=True)
    public_key                 = Column(String, nullable=False)
    authentication_strategy    = Column(String, nullable=False)

    timestamp_registered       = Column(DateTime(timezone=False), nullable=False, default=datetime.now)


class UserSessionToken(Base):
    __tablename__              = 'user_session_tokens'

    session_token              = Column(String, primary_key=True, default=mk_session_token)
    user_uuid                  = Column(String, ForeignKey('user_auths.user_uuid'), nullable=False)
    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)


class UserAuthChallenge(Base):
    __tablename__              = 'user_auth_challenges'

    user_uuid                  = Column(String, ForeignKey('user_auths.user_uuid'), primary_key=True)
    user_challenge             = Column(String, nullable=False, default=mk_auth_challenge)
    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)


class Username(Base):
    __tablename__              = 'usernames'
    record_id                  = Column(Integer, primary_key=True)

    user_uuid                  = Column(String, ForeignKey('user_uuids.user_uuid'), nullable=False)
    username                   = Column(String, nullable=False, unique=True)
    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)

class UserUuid(Base):
    __tablename__              = 'user_uuids'
    user_uuid                  = Column(String, primary_key=True, default=mk_user_uuid)
    timestamp_created          = Column(DateTime(timezone=False), nullable=False, default=datetime.now)

    def generate_new_uuid(self):
        self.user_uuid = mk_user_uuid()

    def uuid_is_valid(self):
        return str(self.user_uuid).startswith("user|")
