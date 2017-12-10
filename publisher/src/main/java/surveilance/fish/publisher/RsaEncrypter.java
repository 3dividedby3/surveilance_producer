package surveilance.fish.publisher;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;

public class RsaEncrypter {

    public static final String EMPTY_STRING = "";
    public static final String ALGORITHM_RSA = "RSA";
    
    //easier to do this than read data from resources folder
    private static final String ENCODED_PRIVATE_KEY = "nottherealkeyy==";

    private static final Decoder BASE64_DECODER = Base64.getDecoder();
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    
    private final Key privateKey;
    private final Cipher encryptCipher;
    
    public RsaEncrypter() {
        try {
            privateKey = initKey(ENCODED_PRIVATE_KEY);
            encryptCipher = Cipher.getInstance(ALGORITHM_RSA);
            encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        } catch(IOException | GeneralSecurityException e) {
            System.out.println("Error while creating RSA encrypter: " + e.getMessage());
            throw new PublisherException(e);
        }
    }
    
    public byte[] encryptAndEncode(byte[] imageData) {
        byte[] result = EMPTY_STRING.getBytes();
        try {
            result =  encryptCipher.doFinal(imageData);
        } catch (GeneralSecurityException e) {
            System.out.println("Cannot encrypt message [" + new String(imageData) + "], returning empty; error: " + e.getMessage());
        }
        
        return BASE64_ENCODER.encode(result);
    }

    private Key initKey(String encodedKeyData) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = BASE64_DECODER.decode(encodedKeyData);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
        
        return keyFactory.generatePrivate(spec);
    }
}
