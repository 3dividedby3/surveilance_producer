package surveilance.fish.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import surveilance.fish.security.base.BaseAesSec;

public class AesEncrypter extends BaseAesSec {
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder(); 

    public byte[] encryptAndEncode(String data, byte[] key) {
        return encryptAndEncode(data.getBytes(), key);
    }
    
    public byte[] encryptAndEncode(byte[] data, byte[] key) {
        byte[] encrypted;
        byte[] initVector;
        try {
            Cipher cipher = Cipher.getInstance(TRANS_AES_CBC_PADDING);
            initVector = new byte[cipher.getBlockSize()];
            new SecureRandom().nextBytes(initVector);
            cipher.init(Cipher.ENCRYPT_MODE, initKey(key), new IvParameterSpec(initVector));
            encrypted = cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            System.out.println("Cannot encrypt data [" + new String(data) + "] with key [" + new String(key) + "], error: "+ e.getMessage());
            throw new SecurityException(e);
        }
        byte[] ivAndEncrypted = addArrays(initVector, encrypted);

        return BASE64_ENCODER.encode(ivAndEncrypted);
    }

    private byte[] addArrays(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        for (int i = 0 ; i < first.length; ++i) {
            result[i] = first[i];
        }
        for (int i = 0 ; i < second.length; ++i) {
            result[first.length + i] = second[i];
        }
        
        return result;
    }
}
