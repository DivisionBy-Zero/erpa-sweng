import pysodium

def authenticate(signed_challenge, challenge, public_key):
        try: 
             opened = pysodium.crypto_sign_open(signed_challenge, public_key)
        except ValueError:
            return False
        return opened == challenge