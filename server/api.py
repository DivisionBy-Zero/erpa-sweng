""" Top level Flask Application
This class is the top-level flask application providing an HTTP REST web API.
"""
from contextlib import contextmanager
from datetime import datetime
from flask import Flask, request, jsonify
from flask_jsontools import DynamicJSONEncoder
from inflection import camelize, underscore
from sqlalchemy import create_engine
from sqlalchemy.exc import IntegrityError
from typing import Optional
from werkzeug.exceptions import HTTPException
import base64
import logging
import os

from contexts import with_context
from models import Game, User, UserAuth, PlayerJoinGameRequest
from session import SessionBroker
from operations import Operations

# Since Google API Engine allows bootstrapping scripts, we use this to set the
# DATABASE_URL and populate the corresponding keys
try:  # On the CI, testing runs without secrets
    from secrets import populate_secrets
    populate_secrets()
except Exception:
    pass


# ################################################## Prelude and util functions
def with_operations(f):
    """
    Injects an operations service on call-time in the surrounded function.
    """
    @contextmanager
    def mk_ops():
        dburl = os.environ.get('DATABASE_URL')
        yield Operations(session_broker=SessionBroker(database_url=dburl))
    return with_context(mk_ops)(f)


def with_operations_and_maybe_user(f):
    """
    Injects an operations service and the identified user (or null if N/A) in
    the surrounded function.
    """
    @contextmanager
    @with_operations
    def register_user(ops):
        auth_header = request.headers.get('Authorization')
        user = (ops.get_authenticated_user(auth_header)
                if auth_header else None)

        yield (ops, user)
    return with_context(register_user)(f)


def with_operations_and_user(f):
    """
    Injects an operations service and the identified user in the surrounded
    function. This function will fail if the user could not be identified.
    """
    @contextmanager
    @with_operations_and_maybe_user
    def register_user(ops, user):
        if user is None:
            raise ValueError("A valid user authentication is required to "
                             "access this resource.")
        yield (ops, user)
    return with_context(register_user)(f)


def gson_camelize(to_camelize):
    """
    Camelizes a string with the first character lowercase as suggested by
    Google's GSON.
    """
    return camelize(to_camelize, False)


class ErpaJsonEncoder(DynamicJSONEncoder):
    """
    Encoder for certain types to custom json representations.
    `gson_camelize`s the result.
    """
    def default(self, obj):
        if isinstance(obj, datetime):
            encoded_object = obj.timestamp()
        else:
            encoded_object = DynamicJSONEncoder.default(self, obj)
        if isinstance(encoded_object, dict):
            encoded_object = {gson_camelize(k): v
                              for k, v in encoded_object.items()}
        return encoded_object


app = Flask(__name__)
app.json_encoder = ErpaJsonEncoder
log = logging.getLogger(__name__)


def retrieve_post_data(dto_type):
    """
    Retrieves an object of type dto_type from the request data.
    The type is required to have underscore-named members.
    """
    request_object = request.get_json(silent=True)
    if not request_object:
        raise ValueError('Expected data of type {} but none was received'
                         ''.format(str(dto_type)))
    dto_type_fields = {gson_camelize(k)
                       for k, _ in dto_type().__json__().items()}
    fields_not_in_dto_type = {k for k, v in request_object.items()
                              if gson_camelize(k) not in dto_type_fields}
    if fields_not_in_dto_type:
        raise ValueError('Unrecognized parameter(s) on request body '
                         '{}'.format(fields_not_in_dto_type))
    return dto_type(**{underscore(k): v for k, v in request_object.items()})


def send_object(obj):
    """
    Returns a response containing an object as json
    """
    return jsonify(obj)


@app.route('/ping')
def ping():
    """Return a friendly HTTP pong."""
    return 'pong'


@app.route('/games')
@with_operations_and_maybe_user
def get_games(ops, maybe_user):
    """Gets the list of games."""
    query = request.args.to_dict()
    page_offset = int(query['page_start'] if 'page_start' in query else 0)
    page_length = int(query['page_length'] if 'page_length' in query else 20)
    if page_length > 100:
        page_length = 100
    refinements = {k: v for k, v in query.items() if k not in
                   ['page_start', 'page_length']}
    ret = ops.get_games(refinements=refinements,
                        page_start=page_offset, page_length=page_length)
    return send_object(ret)


@app.route('/games/uuid/<uuid>')
@with_operations_and_maybe_user
def get_game(ops, maybe_user, uuid: str = None):
    """Retrieves information about a game.
    If the user is authenticated and authorized, the game objects will
    contain privileged details (such as precise location)."""
    return send_object(ops.get_game(uuid))


@app.route('/games', methods=['POST'])
@with_operations_and_user
def create_game(ops, user):
    """Creates a Game."""
    return send_object(ops.create_game(retrieve_post_data(Game), user))


@app.route('/games/uuid/<uuid>', methods=['POST'])
@with_operations_and_user
def update_game(ops, user, uuid: str = None):
    """Updates a Game previously created."""
    return send_object(ops.update_game(retrieve_post_data(Game), user))


@app.route('/games/participants/<uuid>')
@with_operations_and_maybe_user
def get_game_participants(ops, maybe_user, uuid: str = None):
    """Retrieves the list of game join requests for the specified Game."""
    return send_object(ops.get_game_participants(uuid, maybe_user))


@app.route('/games/join/<uuid>', methods=['POST'])
@with_operations_and_user
def join_game(ops, user, uuid: str = None):
    """Joins the specified Game."""
    return send_object(ops.join_game(uuid, user))


@app.route('/games/participants/<uuid>', methods=['POST'])
@with_operations_and_user
def update_game_join_request(ops, user, uuid: str = None):
    """Updates the specified Game. Can only be done by the Game Master"""
    return send_object(ops.update_game_join_request(
        uuid, user, retrieve_post_data(PlayerJoinGameRequest)))


@app.route('/users/user/<username>')
@with_operations
def get_username_uuid(ops, username: str = None):
    """Returns the user_uuid corresponding to the specified username."""
    return ops.get_user_uuid_from_username(username)


@app.route('/users/username/<user_uuid>')
@with_operations
def get_uuid_for_username(ops, user_uuid: str = None):
    """Returns the last username registered to the specified user_uuid."""
    return send_object(ops.get_username_from_user_uuid(user_uuid))


@app.route('/users/newuser/<username>', methods=['POST'])
@with_operations
def register_username(ops, username: str = None):
    """Registers a new username and returns the newly-created user_uuid."""
    return ops.register_username(username)


@app.route('/users/register_auth', methods=['POST'])
@with_operations
def register_user_auth(ops):
    """Register a new user auth."""
    user_auth = retrieve_post_data(UserAuth)
    ops.register_user_auth(user_auth)
    return '', 200


@app.route('/auth/challenge/<user_uuid>')
@with_operations
def get_challenge(ops, user_uuid: str = None):
    """Creates and returns a base64-encoded authentication challenge
    for the specified user_uuid."""
    challenge = ops.gen_auth_challenge_for_user(user_uuid)
    challenge_str = challenge.user_challenge.encode('UTF-8')
    return base64.b64encode(challenge_str).decode('UTF-8')


@app.route('/auth/challenge/<user_uuid>', methods=['POST'])
@with_operations
def get_session_token_from_challenge(ops, user_uuid: str = None):
    """Verifies an authentication challenge and returns a session token in case
    of success."""
    response = request.data.decode('UTF-8').strip()
    token = ops.session_token_from_signed_challenge(user_uuid, response)
    return send_object(token)


@app.route('/users', methods=['POST'])
@with_operations
def register_user(ops):
    """Register a User Profile to the specified user.uuid."""
    return send_object(ops.register_user(retrieve_post_data(User)))


@app.route('/users/uuid/<uuid>', methods=['POST'])
@with_operations_and_user
def update_user(ops, requesting_user, uuid: str = None):
    """Updates a User Profile to the specified user.uuid."""
    return send_object(ops.update_user(retrieve_post_data(User),
                                       requesting_user))


@app.route('/users/uuid/<uuid>')
@with_operations
def get_user(ops, uuid: str = None):
    """Get a user information."""
    return send_object(ops.get_user(uuid))


# #############################################################  Error Handerls


@app.errorhandler(KeyError)
def handle_invalid_resource(exception):
    log.exception('Invalid request %s: %s', request.url, str(exception))
    status_code = getattr(exception, 'status_code', 400)
    return str(exception), status_code


@app.errorhandler(IntegrityError)
def handle_invalid_values(exception):
    log.exception('Invalid request %s: The provided resource is not valid: %s',
                  request.url, str(exception))
    status_code = getattr(exception, 'status_code', 400)
    return str(exception), status_code


@app.errorhandler(ValueError)
def handle_invalid_usage(exception):
    log.exception('Invalid request %s: %s', request.url, str(exception))
    status_code = getattr(exception, 'status_code', 400)
    return str(exception), status_code


@app.errorhandler(Exception)
def handle_server_errors(exception):
    if isinstance(exception, HTTPException):
        return exception
    now = str(datetime.now())
    reference = hash('{}-{}'.format(now, str(exception)))
    log.warning('Request caused server error (%s). url: %s data: %s',
                reference, str(request.url), str(request.data))
    log.exception('Server-side error with reference %s', reference)
    return ('A server-side error happened. If you report the issue, '
            'please mention the following reference number: '
            '{}.'.format(reference)), 500
