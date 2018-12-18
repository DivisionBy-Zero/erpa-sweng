package ch.epfl.sweng.erpa.operations;

import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PublicKey;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CryptoGrenouilleTest {
    CryptoGrenouille underTest;

    @Before
    public void prepare() {
        underTest = new CryptoGrenouille("Have you heard the news that you're dead?", UUID.randomUUID().toString());
    }

    @Test
    public void samePassphraseAndUserUuidCanCreateValidSignature() throws Throwable {
        String password = "Roos is awesome!";
        String userUuid = UUID.randomUUID().toString();
        CryptoGrenouille c1 = new CryptoGrenouille(password, userUuid);
        CryptoGrenouille c2 = new CryptoGrenouille(password, userUuid);

        byte[] rawMessage = "Can you hear me cry out to you words I thought I'd choke on".getBytes("UTF-8");
        String base64EncodedMessage = Base64.encodeToString(rawMessage, Base64.NO_WRAP);
        String base64EncodedSigned = c2.signBase64Encoded(base64EncodedMessage);
        String base64EncodedPublicKey = c1.getBase64EncodedPublicKey();
        Log.i("TestGrenouilleValidSig", "Generated message: " + base64EncodedMessage);
        Log.i("TestGrenouilleValidSig", "Generated signature: " + base64EncodedSigned);
        Log.i("TestGrenouilleValidSig", "Generated public key: " + base64EncodedPublicKey);
        assertTrue(CryptoGrenouille.verifyBase64Encoded(base64EncodedMessage, base64EncodedSigned, base64EncodedPublicKey));
    }

    @Test
    public void rsaKeyPairFromSameSeedDeterministic() throws Throwable {
        String password = "Tell me I'm an angel\n" + "Take this to my grave\n";
        String userUuid = UUID.randomUUID().toString();

        PublicKey key1 = CryptoGrenouille.ed25519KeyPairFromPassphraseAndUserUuid(password, userUuid).getPublic();
        PublicKey key2 = CryptoGrenouille.ed25519KeyPairFromPassphraseAndUserUuid(password, userUuid).getPublic();
        Log.e("dsda", Base64.encodeToString(key1.getEncoded(), Base64.NO_WRAP));
        Log.e("dsda", Base64.encodeToString(key2.getEncoded(), Base64.NO_WRAP));
        assertArrayEquals(key1.getEncoded(), key2.getEncoded());
    }

    @Test
    public void signBase64Encoded() throws Throwable {
        String encodedMessage = Base64.encodeToString("Hello world".getBytes("UTF-8"), Base64.NO_WRAP);
        String signed = underTest.signBase64Encoded(encodedMessage);
        assertTrue(CryptoGrenouille.verifyBase64Encoded(encodedMessage, signed, underTest.getBase64EncodedPublicKey()));
    }

    @Test
    public void signBytes() throws Throwable {
        byte[] message = "Hello world".getBytes("UTF-8");
        byte[] signed = underTest.signBytes(message);
        assertTrue(CryptoGrenouille.verifyBytes(message, signed, underTest.getPublicKey()));
    }
}
