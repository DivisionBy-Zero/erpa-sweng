import unittest
import pysodium
from auth import authenticate

class TestAuthentification(unittest.TestCase):

    def test_authenticate_works(self):
        pk, sk = pysodium.crypto_sign_keypair()
        chall = b'This is a test'
        signed = pysodium.crypto_sign(chall, sk)
        self.assertTrue(authenticate(signed, chall, pk))

    def test_authenticate_fails_false_sign(self):
        pk, sk = pysodium.crypto_sign_keypair()
        chall = b'This is a test'
        self.assertFalse(authenticate("lol", chall, pk))

    def test_authenticate_fails_false_key(self):
        pk, sk = pysodium.crypto_sign_keypair()
        chall = b'This is a test'
        signed = pysodium.crypto_sign(chall, sk)
        self.assertFalse(authenticate(signed, chall, sk))

    def test_authenticate_fails_false_challenge(self):
        pk, sk = pysodium.crypto_sign_keypair()
        chall = b'This is a test'
        signed = pysodium.crypto_sign(chall, sk)
        self.assertFalse(authenticate(signed, "lol", pk))