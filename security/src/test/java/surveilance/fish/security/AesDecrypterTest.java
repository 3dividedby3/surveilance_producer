package surveilance.fish.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AesDecrypterTest {
    
    @Test
    public void testDecrypt() {
        AesDecrypter aesDecrypter = new AesDecrypter();
        byte[] key = {-110, 39, 114, 92, -47, 18, -64, 99, 42, -69, 40, 78, -50, -24, -81, -52};
        byte[] decryptedData = aesDecrypter.decrypt("GRJQpADzNLTd45f5Af37H+8iOWGHaiW1u/v0e/iWp9s=", key);

        assertEquals("Result must be identical"
                , "test"
                , new String(decryptedData));
    }
}
