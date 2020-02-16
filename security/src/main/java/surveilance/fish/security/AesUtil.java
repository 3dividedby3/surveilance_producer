package surveilance.fish.security;

import static surveilance.fish.security.AesEncrypter.ALGORITHM_AES;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AesUtil {

    //can't use bigger key size because of missing Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
    private static final int AES_KEY_SIZE = 128;
    
    private final KeyGenerator keyGenerator;
    
    /**
     * throws {@link SecurityException}
     */
    public AesUtil() {
        try {
            keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
        } catch(NoSuchAlgorithmException e) {
            System.out.println("Error while creating AES key generator: " + e.getMessage());
            throw new SecurityException("Error while creating AES key generator", e);
        }
        keyGenerator.init(AES_KEY_SIZE);
        
    }
    
    public byte[] createAesKey() {
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey.getEncoded();
    }
}
