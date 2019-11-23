package surveilance.fish.security;

import javax.crypto.Cipher;

import surveilance.fish.security.base.BaseRsaSecurity;

public class RsaDecrypter extends BaseRsaSecurity {
    
    public RsaDecrypter(String encodedKey) {
        this(encodedKey, false);
    }
    
    public RsaDecrypter(String encodedKey, boolean isPrivate) {
        super(encodedKey, isPrivate);
    }
    
    public byte[] decrypt(byte[] encodedKey) {
        return doFinal(BASE64_DECODER.decode(encodedKey));
    }
    
    @Override
    protected int getMode() {
        return Cipher.DECRYPT_MODE;
    }
}
