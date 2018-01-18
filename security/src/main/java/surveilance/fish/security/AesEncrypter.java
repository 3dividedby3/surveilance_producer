package surveilance.fish.security;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesEncrypter {

    public static final String ALGORITHM_AES = "AES";
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();

    private final Cipher cipher; 

    public AesEncrypter() {
        try {
            cipher = Cipher.getInstance(ALGORITHM_AES);
        } catch(GeneralSecurityException e) {
            System.out.println("Error while creating AES cipher: " + e.getMessage());
            throw new SecurityException(e);
        }
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
            throw new SecurityException(e);
        }

        return BASE64_ENCODER.encode(encrypted);
    }
    
    private Key initKey(byte[] key) throws GeneralSecurityException {
        return new SecretKeySpec(key, ALGORITHM_AES);
    }
}
