from contextlib import contextmanager
import unittest

from contexts import with_context
from crypto import Crypto
from models import UserAuth


TEST_MESSAGE = "Can you hear me cry out to you words I thought I'd choke on"
TEST_SIGNATURE = 'cx5g9NBZk2ucMHATSv5OaUDpFoQoRP/MMPupYNaEja+mwVZ+woSTQpWA/tAF8doDjN3L+oGaZ89q2laZ0E0mAQ=='
TEST_PUBLIC_KEY = 'MCowBQYDK2VwAyEA1s4PRco1AZnUwMgWDFC1trjoMW+o2OBVycFrCyrhyL0='


def with_crypto(f):
    @contextmanager
    def mk_crypto():
        yield Crypto()
    return with_context(mk_crypto)(f)


class CryptoTest(unittest.TestCase):
    @with_crypto
    def test_verify_signature_received(self, crypto):
        user_auth = UserAuth(user_uuid='random_user',
                             public_key=TEST_PUBLIC_KEY,
                             authentication_strategy='Grenouille')
        sig_ok = crypto.verify_message_signature(TEST_SIGNATURE, TEST_MESSAGE, user_auth)
        self.assertTrue(sig_ok)
