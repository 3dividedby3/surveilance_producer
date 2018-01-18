package surveilance.fish.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RsaEncrypterTest {

    @Test
    public void testEncryption() {
        RsaEncrypter rsaEncrypter = new RsaEncrypter("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOyMd07j2yWomhdAjoIs8dfqu25muYXh10VK7gXjYgRmzXXnZA7Fd3I292H/iNE7UJL0MmyO8X2ob25e8IJyjkscUiu6uso8oGEjhbyRMajl0cQca5HVgkrY7e+t97NwDjjW2Aj6NH3ctVqwWmz4LA0TmeiE5AEyv7t/YHp0PNU7AgMBAAECgYEA2rB+ONwlJaq7UtXnKmg86j5kLAwiZqlFaRV9+smeO62DNAbJlXJwHJK+oMYrCi0JL+M9numET4TiCCsTtbYM35Le2MEX1fj9Whj30oe2CIVnEuo5HGAAVxUZm1gmw1IJIfpPdYGdnKADf/zY9NkFAgGo1g5WeStz2cCNEVz/LwkCQQD7iw70o9PwJBW0nQ4G6DIEh2ThcHGfC20LrfH/zmJM0EG+wjAWFpoKUI5DKrEKFaq9VdWq5sgMMOioNhHRle09AkEA8L1lY12bY+zt7JmlxDUsjYpfsrkEF/jq2fxqZT0HmV40DrZHSEQTgB7N1hTMwCfwjunnsuzPepwI7xOm+CRj1wJAJQAHayTN//zO1Ipljc6JzPC9fgu0KtjbaWRkvGP3QaVAhna57AXXEr1Wr7qP4BPf0YnWrBTWSDTJhDSv2kxbYQJBANWrxKmFE76FNEejt/WGHJ0kC/xdKrz8ObZwyx5AJNaDPkEwl+QUSBqXPKqem3yR9nliMjwk46I3i9Zm4vOrmNECQGAvrc7T8Fr2CLq5+K9wsVpFFk6SR/VpIuF2ZBgfn6u8Qxj0484aQ43x20vK1YgRPvlEyvkgrTrRLcuJATTEgRA=");
        byte[] result = rsaEncrypter.encryptAndEncode("test".getBytes());
        
        assertEquals("Result must be identical"
                , "qt/5MxYu3bt7u+PZ6ldCNOwsrAZGPENI+IWZPJeGu+bYY2JL5nleQgVMbQz9L5igg3KX8ga4mm9KzIjlleNgaozSjvzjjV+aK6yPRJyRmYS86wHqCvkN2vW1tM6kKPZX7dArHtaKRc9dhWTeowIcdj/9WjmRTibEQdVGW5+d0Gk="
                , new String(result));
    }
}
