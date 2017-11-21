package surveilance.fish.publisher;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AesEncrypter {

    //can't use bigger key size because of missing Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
    private static final int AES_KEY_SIZE = 128;
    private static final String ALGORITHM_AES = "AES";
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    
    private final KeyGenerator keyGenerator;
    private final Cipher cipher; 

    public AesEncrypter() {
        try {
            keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
            cipher = Cipher.getInstance(ALGORITHM_AES);
        } catch(GeneralSecurityException e) {
            System.out.println("Error while creating Aes encrypter: " + e.getMessage());
            throw new PublisherException(e);
        }
        keyGenerator.init(AES_KEY_SIZE);
    }

    public byte[] encryptAndEncode(String data, byte[] key) {
        return encryptAndEncode(data.getBytes(), key);
    }
    
    public byte[] encryptAndEncode(byte[] data, byte[] key) {
        byte[] encrypted = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, initKey(key));
            encrypted = cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            System.out.println("Cannot encrypt data [" + new String(data) + "] with key [" + new String(key) + "], error: "+ e.getMessage());
            throw new PublisherException(e);
        }

        return BASE64_ENCODER.encode(encrypted);
    }
    
    public byte[] createAesKey() {
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey.getEncoded();
    }
    
    private Key initKey(byte[] key) throws GeneralSecurityException {
        return new SecretKeySpec(key, ALGORITHM_AES);
    }
}
