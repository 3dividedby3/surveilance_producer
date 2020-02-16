package surveilance.fish.security;

import javax.crypto.Cipher;

import surveilance.fish.security.base.BaseRsaSecurity;

public class RsaEncrypter extends BaseRsaSecurity {
 
    public RsaEncrypter(String encodedKey) {
        this(encodedKey, true);
    }
    
    public RsaEncrypter(String encodedKey, boolean isPrivate) {
        super(encodedKey, isPrivate);
    }
    
    /**
     * throws {@link SecurityException}
     */
    public byte[] encryptAndEncode(byte[] dataToEncrypt) {
        return BASE64_ENCODER.encode(doFinal(dataToEncrypt));
    }
    
    @Override
    protected int getMode() {
        return Cipher.ENCRYPT_MODE;
    }

}
