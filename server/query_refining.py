from sqlalchemy.orm import Session

from typing import List, Dict

import enum
from models import Game, PlayerJoinGameRequest, PlayerInGameStatus

# Need help for filter: distance_from/to
# and sort criterias: asc_dist, dsc_dist

# Returns a query for a list of games given the different filters and sorting criterias given
def get_game_list(_db_session: Session, filters: Dict[str, object], sort_criterias: List[str]):
    games_list = _db_session.query(Game)
    for filter_key in filters.keys():
        filt = filters[filter_key]
        games_list = add_filter(games_list, filter_key, filt)
        
    for sort_key in sort_criterias:
        games_list = add_sort(games_list, sort_key)

    return games_list

# Adds the filters to the query
def add_filter(games_list, filter_key, filt):
    if filter_key == "diff":
        games_list = games_list.filter(Game.difficulty == filt)
    elif filter_key == "universe":
        games_list = games_list.filter(Game.universe == filt)
    elif filter_key == "with_GM":
        games_list = games_list.filter(Game.gm_user_uuid == filt)
    elif filter_key == "from_timestamp":
        games_list = games_list.filter(Game.duration >= filt)
    elif filter_key == "to_timestamp":
        games_list = games_list.filter(Game.duration <= filt)
    elif filter_key == "title_query":
        games_list = games_list.filter(Game.title.like("%{}%".format(filt)))
    elif filter_key == "with_player":
        games_list = (games_list.join(PlayerJoinGameRequest)
                                .filter(PlayerJoinGameRequest.request_status == PlayerInGameStatus.CONFIRMED)
                                .filter(PlayerJoinGameRequest.user_uuid == filt))
    elif filter_key == "with_requesting_player":
        games_list = (games_list.join(PlayerJoinGameRequest)
                                .filter(PlayerJoinGameRequest.request_status == PlayerInGameStatus.REQUEST_TO_JOIN)
                                .filter(PlayerJoinGameRequest.user_uuid == filt))
    return games_list

# Adds the sorting cirteria to the query
def add_sort(games_list, sort_key):
    if sort_key == "asc_diff":
        games_list = games_list.order_by(Game.difficulty.asc())
    elif sort_key == "dsc_diff":
        games_list = games_list.order_by(Game.difficulty.desc())
    elif sort_key == "asc_date":
        games_list = games_list.order_by(Game.timestamp_created.asc())
    elif sort_key == "dsc_date":
        games_list = games_list.order_by(Game.timestamp_created.desc())
    elif sort_key == "asc_max_player":
        games_list = games_list.order_by(Game.max_players.asc())
    elif sort_key == "dsc_max_player":
        games_list = games_list.order_by(Game.max_players.desc())
    return games_list