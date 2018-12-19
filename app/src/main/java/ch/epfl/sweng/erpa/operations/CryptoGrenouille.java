package ch.epfl.sweng.erpa.operations;

import android.util.Base64;
import android.util.Log;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CryptoGrenouille {
    public static final String AUTH_STRATEGY_NAME = "Grenouille";
    private static final int PBKDF2_ITERATIONS_COUNT = 4096;
    private final KeyPair keyPair;

    public CryptoGrenouille(String passphrase, String userUuid) {
        try {
            keyPair = ed25519KeyPairFromPassphraseAndUserUuid(passphrase.trim(), userUuid.trim());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | UnsupportedEncodingException e) {
            throw logAndWrapException(e, "Could not derive KeyPair");
        }
    }

    public static boolean verifyBase64Encoded(String plaintext, String signed, String base64EncodedPublicKey)
        throws InvalidKeyException {
        try {
            byte[] plaintextBytes = Base64.decode(plaintext, Base64.DEFAULT);
            byte[] signedBytes = Base64.decode(signed, Base64.DEFAULT);
            byte[] publicKeyBytes = Base64.decode(base64EncodedPublicKey, Base64.DEFAULT);
            PublicKey publicKey = new EdDSAPublicKey(new X509EncodedKeySpec(publicKeyBytes));
            return verifyBytes(plaintextBytes, signedBytes, publicKey);
        } catch (InvalidKeySpecException e) {
            throw logAndWrapException(e, "Could not rebuild public key to verify secret");
        }
    }

    public static boolean verifyBytes(byte[] plaintext, byte[] signed, PublicKey publicKey) throws InvalidKeyException {
        try {
            Signature signer = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
            signer.initVerify(publicKey);
            signer.update(plaintext);

            return signer.verify(signed);
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw logAndWrapException(e, "Could not verify signature");
        }
    }

    private static RuntimeException logAndWrapException(Throwable e, String msg) {
        Log.wtf("CryptoGrenouille", msg, e);
        return new RuntimeException("Irrecoverable error in CryptoGrenouille", e);
    }

    static KeyPair ed25519KeyPairFromPassphraseAndUserUuid(String passphrase, String userUuid)
        throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException {
        byte[] salt = userUuid.getBytes("UTF-8");
        char[] password = passphrase.toCharArray();

        EdDSAParameterSpec edParams = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);

        int pbkdf2DerivedKeyLengthBits = edParams.getCurve().getField().getb();
        // https://stackoverflow.com/q/2375541
        byte[] seed = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(new PBEKeySpec(password, salt, PBKDF2_ITERATIONS_COUNT, pbkdf2DerivedKeyLengthBits))
            .getEncoded();

        EdDSAPrivateKeySpec privKey = new EdDSAPrivateKeySpec(seed, edParams);
        EdDSAPublicKeySpec pubKey = new EdDSAPublicKeySpec(privKey.getA(), edParams);

        return new KeyPair(new EdDSAPublicKey(pubKey), new EdDSAPrivateKey(privKey));
    }

    public String getBase64EncodedPublicKey() {
        return Base64.encodeToString(getPublicKey().getEncoded(), Base64.NO_WRAP);
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String signBase64Encoded(String message) {
        byte[] plaintext = Base64.decode(message, Base64.NO_WRAP);
        byte[] signed = signBytes(plaintext);
        return Base64.encodeToString(signed, Base64.NO_WRAP);
    }

    public byte[] signBytes(byte[] plaintext) {
        try {
            Signature signer = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
            signer.initSign(keyPair.getPrivate());
            signer.update(plaintext);
            return signer.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw logAndWrapException(e, "Could not perform signature");
        }
    }
}
