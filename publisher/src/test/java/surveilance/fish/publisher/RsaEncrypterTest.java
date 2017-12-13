package surveilance.fish.publisher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RsaEncrypterTest {

    @Test
    public void testEncryption() {
        RsaEncrypter rsaEncrypter = new RsaEncrypter("nottherealkeyy==");
        byte[] result = rsaEncrypter.encryptAndEncode("test".getBytes());
        
        assertEquals("Results must be identical"
                , "qt/5MxYu3bt7u+PZ6ldCNOwsrAZGPENI+IWZPJeGu+bYY2JL5nleQgVMbQz9L5igg3KX8ga4mm9KzIjlleNgaozSjvzjjV+aK6yPRJyRmYS86wHqCvkN2vW1tM6kKPZX7dArHtaKRc9dhWTeowIcdj/9WjmRTibEQdVGW5+d0Gk="
                , new String(result));
    }
}
