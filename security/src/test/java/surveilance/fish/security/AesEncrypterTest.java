package surveilance.fish.security;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AesEncrypterTest {

    @Test
    public void testAesEncrypt() {
        AesEncrypter aesEncrypter = new AesEncrypter();
        byte[] key = {-110, 39, 114, 92, -47, 18, -64, 99, 42, -69, 40, 78, -50, -24, -81, -52};
        byte[] encryptedData = aesEncrypter.encryptAndEncode("test".getBytes(), key);
        
        //using IV cannot predict outcome, just check that no errors happened
        assertNotNull("Must not be empty", encryptedData);
    }
}
