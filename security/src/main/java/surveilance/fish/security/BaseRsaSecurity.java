package surveilance.fish.security;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;

public abstract class BaseRsaSecurity {

    public static final String EMPTY_STRING = "";
    public static final String ALGORITHM_RSA = "RSA";
    
    private final Key key;
    private final Cipher cipher;

    public static final Decoder BASE64_DECODER = Base64.getDecoder();
    public static final Encoder BASE64_ENCODER = Base64.getEncoder();

    protected BaseRsaSecurity(String encodedKey, boolean isPrivate) {
        if (encodedKey == null) {
            throw new IllegalArgumentException("encodedKey cannot be null");
        }
        try {
            key = initKey(encodedKey, isPrivate);
            cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(getMode(), key);
        } catch(GeneralSecurityException e) {
            throw new SecurityException("Error while creating RSA decrypter", e);
        }
    }
    
    protected abstract int getMode();

    public byte[] doFinal(byte[] dataToEncrypt) {
        if (dataToEncrypt == null) {
            throw new IllegalArgumentException("dataToEncrypt cannot be null");
        }
        try {
            return cipher.doFinal(dataToEncrypt);
        } catch (GeneralSecurityException e) {
            throw new SecurityException("Cannot decrypt message: [" + new String(dataToEncrypt) + "]", e);
        }
    }
    
    private Key initKey(String encodedKeyData, boolean isPrivate) throws GeneralSecurityException {
        byte[] keyBytes = BASE64_DECODER.decode(encodedKeyData);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM_RSA);
        if (isPrivate) {
            return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } else {
            return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
        }
    }
}
