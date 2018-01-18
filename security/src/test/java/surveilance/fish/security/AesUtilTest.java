package surveilance.fish.security;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AesUtilTest {

    @Test
    public void testCreateAesKey() {
        byte[] key = new AesUtil().createAesKey();
        
        assertNotNull(key);
    }
}
