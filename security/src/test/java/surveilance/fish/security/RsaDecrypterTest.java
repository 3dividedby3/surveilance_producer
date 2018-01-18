package surveilance.fish.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RsaDecrypterTest {

    @Test
    public void testDecrypt() {
        RsaDecrypter rsaDecrypter = new RsaDecrypter("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDsjHdO49slqJoXQI6CLPHX6rtuZrmF4ddFSu4F42IEZs1152QOxXdyNvdh/4jRO1CS9DJsjvF9qG9uXvCCco5LHFIrurrKPKBhI4W8kTGo5dHEHGuR1YJK2O3vrfezcA441tgI+jR93LVasFps+CwNE5nohOQBMr+7f2B6dDzVOwIDAQAB");
        byte[] result = rsaDecrypter.decrypt("qt/5MxYu3bt7u+PZ6ldCNOwsrAZGPENI+IWZPJeGu+bYY2JL5nleQgVMbQz9L5igg3KX8ga4mm9KzIjlleNgaozSjvzjjV+aK6yPRJyRmYS86wHqCvkN2vW1tM6kKPZX7dArHtaKRc9dhWTeowIcdj/9WjmRTibEQdVGW5+d0Gk=".getBytes());
        
        assertEquals("Result must be identical"
                , "test"
                , new String(result));
    }
}
