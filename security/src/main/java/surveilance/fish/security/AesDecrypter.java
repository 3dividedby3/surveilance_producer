package surveilance.fish.security;

import static surveilance.fish.security.AesEncrypter.ALGORITHM_AES;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesDecrypter {
    
    private static final Decoder BASE64_DECODER = Base64.getDecoder();
    private final Cipher cipher; 

    public AesDecrypter() {
        try {
            cipher = Cipher.getInstance(ALGORITHM_AES);
        } catch(GeneralSecurityException e) {
            System.out.println("Error while creating Aes encrypter: " + e.getMessage());
            throw new SecurityException(e);
        }
    }
    
    public byte[] decrypt(String data, byte[] key) {
        byte[] decrypted = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, initKey(key));
            decrypted = cipher.doFinal(BASE64_DECODER.decode(data));
        } catch (GeneralSecurityException e) {
            System.out.println("Cannot decrypt data [" + data + "] with key [" + new String(key) + "], error: "+ e.getMessage());
            throw new SecurityException(e);
        }

        return decrypted;
    }
    
    private Key initKey(byte[] key) throws GeneralSecurityException {
        return new SecretKeySpec(key, ALGORITHM_AES);
    }
}
