package surveilance.fish.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import surveilance.fish.security.base.BaseAesSec;

public class AesDecrypter extends BaseAesSec {
    
    private static final Decoder BASE64_DECODER = Base64.getDecoder();
    
    /**
     * throws {@link SecurityException}
     */
    public byte[] decrypt(String data, byte[] key) {
        byte[] decrypted;
        byte[] decodedData = BASE64_DECODER.decode(data);
        InputStream dataInputStream = new ByteArrayInputStream(decodedData);
        try {
            Cipher cipher = Cipher.getInstance(TRANS_AES_CBC_PADDING);
            byte[] initVector = new byte[cipher.getBlockSize()];
            dataInputStream.read(initVector);
            cipher.init(Cipher.DECRYPT_MODE, initKey(key), new IvParameterSpec(initVector));
            byte[] dataToDecrypt = new byte[decodedData.length - initVector.length];
            dataInputStream.read(dataToDecrypt);
            decrypted = cipher.doFinal(dataToDecrypt);
        } catch (GeneralSecurityException | IOException e) {
            System.out.println("Cannot decrypt data [" + data + "] with key [" + new String(key) + "], error: "+ e.getMessage());
            throw new SecurityException(e);
        }

        return decrypted;
    }
}
