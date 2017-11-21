package surveilance.fish.publisher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AesEncrypterTest {

    @Test
    public void testCreateAesKey() {
        AesEncrypter aesEncrypter = new AesEncrypter();
        byte[] key = aesEncrypter.createAesKey();
        
        assertNotNull(key);
    }
    
    @Test
    public void testAesEncrypt() {
        AesEncrypter aesEncrypter = new AesEncrypter();
        byte[] key = {-110, 39, 114, 92, -47, 18, -64, 99, 42, -69, 40, 78, -50, -24, -81, -52};
        byte[] encrypted = aesEncrypter.encryptAndEncode("test".getBytes(), key);
        
        assertEquals("Eg/wK2sXlAhBG+y27BE9kg==", new String(encrypted));
    }
}
