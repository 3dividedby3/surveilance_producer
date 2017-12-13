package surveilance.fish.publisher;

import static surveilance.fish.publisher.AesEncrypter.ALGORITHM_AES;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AesUtil {

    //can't use bigger key size because of missing Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
    private static final int AES_KEY_SIZE = 128;
    
    private final KeyGenerator keyGenerator;
    
    public AesUtil() {
        try {
            keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
        } catch(NoSuchAlgorithmException e) {
            System.out.println("Error while creating AES key generator: " + e.getMessage());
            throw new PublisherException("Error while creating AES key generator", e);
        }
        keyGenerator.init(AES_KEY_SIZE);
        
    }
    
    public byte[] createAesKey() {
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey.getEncoded();
    }
}
