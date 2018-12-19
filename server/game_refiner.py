from typing import Dict
import logging

from sqlalchemy.orm.query import Query
from sqlalchemy import func

from models import Game, PlayerJoinGameRequest, PlayerInGameStatus


log = logging.getLogger(__name__)


def refine_queries(query: Query, filters: Dict[str, str]) -> Query:
    for k, v in filters.items():
        query = refine_query(query, k.lower(), v)
    return query


def refine_query(query: Query, key: str, value: str) -> Query:
    lw = func.lower

    if key == 'difficulty':
        query = query.filter(Game.difficulty == int(value))
    elif key == 'universe':
        query = query.filter(lw(Game.universe) == value.lower())
    elif key == 'with_gm':
        query = query.filter(lw(Game.gm_user_uuid) == value.lower())
    elif key == 'game_status':
        query = query.filter(Game.game_status == value)
    elif key == 'min_minutes':
        query = query.filter(Game.session_length_in_minutes >= value)
    elif key == 'max_minutes':
        query = query.filter(Game.session_length_in_minutes <= value)
    elif key == 'title_query':
        query = query.filter(lw(Game.title).like('%{}%'.format(value.lower())))
    elif key == 'sort_difficulty' and value == 'asc':
        query = query.order_by(Game.difficulty.asc())
    elif key == 'sort_difficulty' and value == 'desc':
        query = query.order_by(Game.difficulty.desc())
    elif key == 'sort_date' and value == 'asc':
        query = query.order_by(Game.timestamp_created.asc())
    elif key == 'sort_date' and value == 'desc':
        query = query.order_by(Game.timestamp_created.desc())
    elif key == 'sort_max_players' and value == 'asc':
        query = query.order_by(Game.max_players.asc())
    elif key == 'sort_max_players' and value == 'desc':
        query = query.order_by(Game.max_players.desc())
    elif key == 'sort_min_players' and value == 'asc':
        query = query.order_by(Game.min_players.asc())
    elif key == 'sort_min_players' and value == 'desc':
        query = query.order_by(Game.min_players.desc())
    elif key == 'player_pending':
        query = (query.join(PlayerJoinGameRequest,
                            PlayerJoinGameRequest.game_uuid == Game.uuid)
                 .filter(PlayerJoinGameRequest.user_uuid == value)
                 .filter(PlayerJoinGameRequest.request_status ==
                         PlayerInGameStatus.REQUEST_TO_JOIN)
                 )
    elif key == 'player_confirmed':
        query = (query.join(PlayerJoinGameRequest,
                            PlayerJoinGameRequest.game_uuid == Game.uuid)
                 .filter(PlayerJoinGameRequest.user_uuid == value)
                 .filter(PlayerJoinGameRequest.request_status ==
                         PlayerInGameStatus.CONFIRMED)
                 )
    elif key == 'game_status':
        query = query.filter(Game.game_status == value)
    else:
        log.warning('Could not parse query spec with key {} and value {}'
                    ''.format(key, value))
    return query
