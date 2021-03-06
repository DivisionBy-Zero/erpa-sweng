from contextlib import contextmanager
from typing import Dict, List, Optional, Set
import base64

from datetime import datetime
from sqlalchemy.orm import Session

from contexts import with_context_using_instance
from crypto import Crypto
from game_refiner import refine_queries
from models import Game, GameStatus, User, UserAuth, UserAuthChallenge, \
    UserSessionToken, UserUuid, Username, \
    PlayerJoinGameRequest, PlayerInGameStatus
from session import SessionBroker


def with_session(f):
    @contextmanager
    def mk_session(ops):
        with ops.session_broker.get_session() as session:
            yield session
    return with_context_using_instance(mk_session)(f)


class Operations:
    def __init__(self, session_broker: SessionBroker = None):
        self.session_broker = session_broker
        if not self.session_broker:
            self.session_broker = SessionBroker()

    @with_session
    def get_authenticated_user(self, authtoken: str, session: Session) -> User:
        user = (session.query(User)
                .join(UserSessionToken,
                      User.uuid == UserSessionToken.user_uuid)
                .filter(UserSessionToken.session_token == authtoken)
                .scalar()
                )
        return user

    @with_session
    def create_game(self, new_game: Game, user: User, session: Session
                    ) -> Game:
        if not new_game.uuid_is_valid():
            new_game.generate_new_uuid()

        existing_game_uuid = (session.query(Game.uuid)
                              .filter(Game.uuid == new_game.uuid)
                              .scalar()
                              )

        if existing_game_uuid:
            new_game.generate_new_uuid()
            return self.create_game(new_game, user)

        if not user.is_gm:
            raise ValueError("User is not registered as Game Master")

        new_game.game_status = GameStatus.CREATED
        new_game.gm_user_uuid = user.uuid
        new_game.timestamp_created = datetime.now()
        new_game.timestamp_modified = datetime.now()

        session.add(new_game)

        return new_game

    @with_session
    def update_game(self, updated_game: Game, user: User, session: Session
                    ) -> Game:
        existing_game = (session.query(Game)
                         .filter(Game.uuid == updated_game.uuid)
                         .filter(Game.gm_user_uuid == user.uuid)  # ZKP
                         .scalar()
                         )

        if not existing_game:
            raise KeyError("Game does not exist or user does not have "
                           "the permission to access it.")

        existing_game.fromdict(updated_game.asdict(
            exclude=['uuid', 'gm_user_uuid', 'timestamp_created']))
        existing_game.timestamp_modified = datetime.now()

        session.add(existing_game)

        return existing_game

    @with_session
    def get_game(self, uuid: str, session: Session) -> Game:
        existing_game = (session.query(Game)
                         .filter(Game.uuid == uuid)
                         .scalar()
                         )

        # TODO: Filter out information if the user is not GM or been accepted
        return existing_game

    @with_session
    def get_games(self, session: Session,
                  page_start: int = 0, page_length: int = 20,
                  refinements: Dict[str, str] = dict()) -> List[Game]:
        return (refine_queries(session.query(Game), refinements)
                .offset(page_start)
                .limit(page_length)
                ).all()

    @with_session
    def get_game_participants(self, game_uuid: str, user: Optional[User],
                              session: Session) -> List[PlayerJoinGameRequest]:
        game = self.get_game(game_uuid)
        game_join_requests = (session.query(PlayerJoinGameRequest)
                              .filter(PlayerJoinGameRequest.game_uuid
                                      == game_uuid)
                              .all()
                              )

        def filter_game_join_requests(gp: PlayerJoinGameRequest) -> bool:
            return gp.request_status == PlayerInGameStatus.CONFIRMED or \
                (user is not None and gp.user_uuid == user.uuid)

        # If not the game master
        if not user or user.uuid != game.gm_user_uuid:
            game_join_requests = [gp for gp in game_join_requests
                                  if filter_game_join_requests(gp)]
        return game_join_requests

    @with_session
    def join_game(self, game_uuid: str, user: User, session: Session
                  ) -> PlayerJoinGameRequest:
        if user.uuid == self.get_game(game_uuid).gm_user_uuid:
            raise ValueError("The GM can't join it's own game!")
        game_participants = (session.query(PlayerJoinGameRequest)
                             .filter(PlayerJoinGameRequest.game_uuid
                                     == game_uuid)
                             .all()
                             )
        existing_request = next((jreq for jreq in game_participants
                                 if jreq.user_uuid == user.uuid), None)
        if existing_request:
            request = existing_request
            if request.request_status in [PlayerInGameStatus.HAS_QUIT,
                                          PlayerInGameStatus.REMOVED,
                                          PlayerInGameStatus.REQUEST_TO_JOIN]:
                request.request_status = PlayerInGameStatus.REQUEST_TO_JOIN
            else:
                raise ValueError("A join request already exist.")
        else:
            request = PlayerJoinGameRequest(game_uuid=game_uuid,
                                            user_uuid=user.uuid)
        session.add(request)
        return request

    @with_session
    def update_game_join_request(self, game_uuid: str, user: User,
                                 updated_request: PlayerJoinGameRequest,
                                 session: Session) -> PlayerJoinGameRequest:
        def joined_user_leaving_game(old_request: PlayerJoinGameRequest,
                                     new_request: PlayerJoinGameRequest):
            if not old_request or not new_request:
                return False
            return (old_request.request_status == PlayerInGameStatus.REQUEST_TO_JOIN and
                    new_request.request_status == PlayerInGameStatus.HAS_QUIT)

        def has_left_user_joining_game(old_request: PlayerJoinGameRequest,
                                       new_request: PlayerJoinGameRequest):
            if not old_request or not new_request:
                return False
            return (old_request.request_status == PlayerInGameStatus.HAS_QUIT and
                    new_request.request_status == PlayerInGameStatus.REQUEST_TO_JOIN)

        game = self.get_game(game_uuid)
        game_join_request = (session.query(PlayerJoinGameRequest)
                             .filter(PlayerJoinGameRequest.game_uuid
                                     == game_uuid)
                             .filter(PlayerJoinGameRequest.user_uuid
                                     == updated_request.user_uuid)
                             .scalar()
                             )

        if (game.gm_user_uuid != user.uuid):
            if not (joined_user_leaving_game(game_join_request, updated_request) or
                    has_left_user_joining_game(game_join_request, updated_request)):
                raise KeyError("Game does not exist or user does not have "
                               "the permission to access it.")  # ZKP

        if not game_join_request:
            raise KeyError("Player with uuid {} has no join request"
                           "".format(user.uuid))
        game_join_request.request_status = updated_request.request_status
        game_join_request.timestamp_modified = datetime.now()
        session.add(game_join_request)
        return game_join_request

    @with_session
    def register_username(self, username_str: str, session: Session) -> str:
        existing_username = (session.query(Username.user_uuid)
                             .filter(Username.username == username_str)
                             .scalar()
                             )
        if existing_username:
            raise KeyError("This username already taken")

        user_uuid = UserUuid()
        session.add(user_uuid)
        session.flush()  # Required to force defaults generation
        username = Username(username=username_str,
                            user_uuid=user_uuid.user_uuid)

        session.add(username)
        session.flush()

        return user_uuid.user_uuid

    @with_session
    def get_user_uuid_from_username(self, username_str: str, session: Session
                                    ) -> str:
        existing_username_uuid = (session.query(Username.user_uuid)
                                  .filter(Username.username == username_str)
                                  .scalar()
                                  )
        if not existing_username_uuid:
            raise KeyError("No user with such username could be found")

        return existing_username_uuid

    @with_session
    def get_username_from_user_uuid(self, user_uuid: str, session: Session
                                    ) -> str:
        existing_username = (session.query(Username)
                             .filter(Username.user_uuid == user_uuid)
                             .scalar()
                             )
        if not existing_username:
            raise KeyError("No username with such uuid could be found")

        return existing_username

    @with_session
    def register_user_auth(self, user_auth: UserAuth, session: Session
                           ) -> UserAuth:
        user_uuid = user_auth.user_uuid
        existing_user_auth = (session.query(UserAuth.public_key)
                              .filter(UserAuth.user_uuid == user_uuid)
                              .scalar()
                              )

        if existing_user_auth:
            raise KeyError("User has already registered a public key")

        user_auth.authentication_strategy = "Grenouille"
        user_auth.timestamp_registered = datetime.now()

        session.add(user_auth)
        return user_auth

    @with_session
    def get_user_auth(self, user_uuid: str, session: Session) -> UserAuth:
        existing_user_auth = (session.query(UserAuth)
                              .filter(UserAuth.user_uuid == user_uuid)
                              .scalar()
                              )

        if not existing_user_auth:
            raise KeyError("User has not registered a public key")
        return existing_user_auth

    @with_session
    def register_user(self, user: User, session: Session):
        existing_user = (session.query(User.uuid)
                         .filter(User.uuid == user.uuid)
                         .scalar()
                         )

        if existing_user:
            raise KeyError("A user profile with this UUID already exists")

        user.timestamp_created = datetime.now()
        session.add(user)
        return user

    @with_session
    def update_user(self, updated_user: User, requesting_user: User,
                    session: Session) -> User:
        existing_user = (session.query(User)
                         .filter(User.uuid == requesting_user.uuid)
                         .scalar()
                         )

        if not existing_user:
            raise KeyError("Couldn't find the requested profile")

        existing_user.fromdict(updated_user.asdict(
            exclude=['uuid', 'timestamp_created']))
        existing_user.timestamp_modified = datetime.now()
        session.add(existing_user)
        return existing_user

    @with_session
    def get_user(self, user_uuid: str, session: Session):
        existing_user = (session.query(User)
                         .filter(User.uuid == user_uuid)
                         .scalar()
                         )

        if not existing_user:
            raise KeyError("User profile does not exist")

        return existing_user

    @with_session
    def get_user_from_session_token(self, session_token: str,
                                    session: Session):
        existing_token_user_uuid = (session.query(UserSessionToken.user_uuid)
                                    .filter(UserSessionToken.session_token
                                            == session_token)
                                    .scalar()
                                    )
        if not existing_token_user_uuid:
            raise KeyError("Unknown session token")

        return self.get_user(existing_token_user_uuid)

    @with_session
    def get_user_tokens_for_user(self, user_uuid: str, session: Session
                                 ) -> Set[UserSessionToken]:
        return set(session.query(UserSessionToken)
                   .filter(UserSessionToken.user_uuid == user_uuid)
                   .all()
                   )

    @with_session
    def get_auth_challenge_for_user(self, user_uuid: str, session: Session
                                    ) -> UserAuthChallenge:
        existing_challenge = (session.query(UserAuthChallenge)
                              .filter(UserAuthChallenge.user_uuid == user_uuid)
                              .scalar()
                              )
        if not existing_challenge:
            raise ValueError('No challenge exist for the specified user uuid.')
        return existing_challenge

    @with_session
    def gen_auth_challenge_for_user(self, user_uuid: str, session: Session
                                    ) -> UserAuthChallenge:
        existing_challenge = (session.query(UserAuthChallenge)
                              .filter(UserAuthChallenge.user_uuid == user_uuid)
                              .scalar()
                              )
        if existing_challenge:
            session.delete(existing_challenge)
            session.flush()
        new_challenge = UserAuthChallenge(user_uuid=user_uuid)
        session.add(new_challenge)
        return new_challenge

    @with_session
    def session_token_from_signed_challenge(self, user_uuid: str,
                                            b64_signed: str, session: Session
                                            ) -> UserSessionToken:
        existing_challenge = self.get_auth_challenge_for_user(user_uuid)
        user_auth = self.get_user_auth(user_uuid)
        if not Crypto().verify_message_signature(
                b64_signed, existing_challenge.user_challenge, user_auth):
            raise ValueError('Invalid challenge.')
        session.delete(existing_challenge)
        created_token = UserSessionToken(user_uuid=user_uuid)
        session.add(created_token)
        return created_token
