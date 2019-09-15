package surveilance.fish.security;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public abstract class AesSecBase {

    public static final String TRANS_AES_CBC_PADDING = "AES/CBC/PKCS5Padding";
    public static final String ALGORITHM_AES = "AES";

    protected Key initKey(byte[] key) throws GeneralSecurityException {
        return new SecretKeySpec(key, ALGORITHM_AES);
    }
}
