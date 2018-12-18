from functools import wraps
import base64
import inspect
import ed25519

from models import UserAuth


def non_null_params(f):
    @wraps(f)
    def fn(*args):
        for i in range(1, len(args)):
            if args[i] is None:
                argname = inspect.getfullargspec(f).args[i]
                raise ValueError('{} cannot be null'.format(argname))
        return f(*args)
    return f


class CryptoGrenouille:
    # OID 1.3.101.xxx: https://tools.ietf.org/html/draft-ietf-curdle-pkix-04
    OID_ED25519 = 112
    OID_BYTE = 8
    IDLEN_BYTE = 3

    def _get_verifying_key_from_user_auth(self, user_auth: UserAuth):
        x509b = base64.b64decode(user_auth.public_key)
        if x509b[self.OID_BYTE] != self.OID_ED25519:
            raise ValueError('Grenouille authentication requires an x509 Ed25519 Certificate')
        total_length = 46 if x509b[self.IDLEN_BYTE] == 7 else 44
        id_length = 7 if x509b[self.IDLEN_BYTE] == 7 else 5
        if len(x509b) != total_length:
            raise ValueError('Malformed x509 message: Expecting {} bytes, actual {}'.format(total_length, len(x509b)))
        # Magic numbers extracted from the java implementation of ed25519: str4d/ed25519-java
        # https://github.com/str4d/ed25519-java/blob/master/src/net/i2p/crypto/eddsa/EdDSAPublicKey.java
        expected_header = bytes([0x30, (total_length - 2), 0x30, id_length, 0x06, 3, (1 * 40) + 3, 101])
        if x509b[0:len(expected_header)] != expected_header:
            raise ValueError('Malformed x509 message: Malformed header')
        header_scan_idx = len(expected_header) + 1  # +1: OID_BYTE, which we already checked
        if id_length == 7:
            if x509b[header_scan_idx:header_scan_idx + 2] != bytes([0x05, 0]):
                raise ValueError('Unsupported x509 key spec')
            header_scan_idx = header_scan_idx + 2
        if x509b[header_scan_idx:header_scan_idx + 3] != bytes([0x03, 33, 0]):
            raise ValueError('Unsupported x509 key spec')
        header_scan_idx = header_scan_idx + 3
        return ed25519.VerifyingKey(x509b[header_scan_idx:])

    @non_null_params
    def validate_user_auth(self, user_auth: UserAuth):
        self._get_verifying_key_from_user_auth(user_auth)

    @non_null_params
    def verify_message_signature(self, b64_signature: str, message: str,
                                 user_auth: UserAuth) -> bool:
        verifying_key = self._get_verifying_key_from_user_auth(user_auth)
        signature = base64.b64decode(b64_signature)
        try:
            verifying_key.verify(signature, message.encode('UTF-8'))
        except ed25519.BadSignatureError:
            return False
        return True


strategy_engines = {
    'Grenouille': CryptoGrenouille()
}


def with_engine_from_last_argument(f):
    @wraps(f)
    def fn(*args):
        user_auth = args[-1]
        strategy = user_auth.authentication_strategy
        engine = strategy_engines[strategy]
        if not engine:
            msg = 'Cannot proceed with {}-type crypto'.format(strategy)
            raise ValueError(msg)
        return f(*(args + (engine,)))
    return fn


class Crypto:
    @non_null_params
    @with_engine_from_last_argument
    def validate_user_auth(self, user_auth: UserAuth, engine):
        return engine.validate_user_auth(user_auth)

    @non_null_params
    @with_engine_from_last_argument
    def verify_message_signature(self, b64_signature: str, message: str,
                                 user_auth: UserAuth, engine) -> bool:
        return engine.verify_message_signature(b64_signature, message, user_auth)
