package surveilance.fish.publisher.audit;

import static org.junit.Assert.assertTrue;
import static surveilance.fish.publisher.App.PROP_CLIENT_TIMEOUT;
import static surveilance.fish.publisher.audit.ViewerDataConsumer.PROP_DATA_PRODUCER_GET_DATA_DELAY;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import surveilance.fish.model.DataBrick;
import surveilance.fish.model.ViewerData;
import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.publisher.AuthCookieUpdater;
import surveilance.fish.publisher.audit.AuditData;
import surveilance.fish.publisher.audit.ViewerDataConsumer;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaDecrypter;
import surveilance.fish.security.RsaEncrypter;

public class ViewerDataConsumerTest {
    
    private boolean dataSaved;
    private ViewerDataConsumer testViewerDataConsumer;

    @Before
    public void createTestObject() {
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_CLIENT_TIMEOUT, "2000");
        properties.put(PROP_DATA_PRODUCER_GET_DATA_DELAY, "2000");
        
        AesUtil aesUtil = new AesUtil();
        AesDecrypter aesDecrypter = new AesDecrypter();
        AesEncrypter aesEncrypter = new AesEncrypter();
        RsaDecrypter rsaDecrypter = new RsaDecrypter("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDsjHdO49slqJoXQI6CLPHX6rtuZrmF4ddFSu4F42IEZs1152QOxXdyNvdh/4jRO1CS9DJsjvF9qG9uXvCCco5LHFIrurrKPKBhI4W8kTGo5dHEHGuR1YJK2O3vrfezcA441tgI+jR93LVasFps+CwNE5nohOQBMr+7f2B6dDzVOwIDAQAB");
        DataAccessor<AuditData> dataAccesor = new DataAccessor<AuditData>() {
            @Override
            public List<AuditData> getLastNoOfElems(int noOfElem, TypeReference<AuditData> typeReference) {
                // TODO Auto-generated method stub
                return null;
            }
            @Override
            public void saveData(AuditData data) throws IOException {
                dataSaved = true;
            }
        };
        RsaEncrypter rsaEncrypter = new RsaEncrypter("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOyMd07j2yWomhdAjoIs8dfqu25muYXh10VK7gXjYgRmzXXnZA7Fd3I292H/iNE7UJL0MmyO8X2ob25e8IJyjkscUiu6uso8oGEjhbyRMajl0cQca5HVgkrY7e+t97NwDjjW2Aj6NH3ctVqwWmz4LA0TmeiE5AEyv7t/YHp0PNU7AgMBAAECgYEA2rB+ONwlJaq7UtXnKmg86j5kLAwiZqlFaRV9+smeO62DNAbJlXJwHJK+oMYrCi0JL+M9numET4TiCCsTtbYM35Le2MEX1fj9Whj30oe2CIVnEuo5HGAAVxUZm1gmw1IJIfpPdYGdnKADf/zY9NkFAgGo1g5WeStz2cCNEVz/LwkCQQD7iw70o9PwJBW0nQ4G6DIEh2ThcHGfC20LrfH/zmJM0EG+wjAWFpoKUI5DKrEKFaq9VdWq5sgMMOioNhHRle09AkEA8L1lY12bY+zt7JmlxDUsjYpfsrkEF/jq2fxqZT0HmV40DrZHSEQTgB7N1hTMwCfwjunnsuzPepwI7xOm+CRj1wJAJQAHayTN//zO1Ipljc6JzPC9fgu0KtjbaWRkvGP3QaVAhna57AXXEr1Wr7qP4BPf0YnWrBTWSDTJhDSv2kxbYQJBANWrxKmFE76FNEejt/WGHJ0kC/xdKrz8ObZwyx5AJNaDPkEwl+QUSBqXPKqem3yR9nliMjwk46I3i9Zm4vOrmNECQGAvrc7T8Fr2CLq5+K9wsVpFFk6SR/VpIuF2ZBgfn6u8Qxj0484aQ43x20vK1YgRPvlEyvkgrTrRLcuJATTEgRA=");
        AuthCookieUpdater authCookieUpdater = new AuthCookieUpdater(properties, rsaEncrypter, aesEncrypter, aesUtil);
        
        ViewerDataConsumer viewerDataConsumer = new ViewerDataConsumer(properties, aesDecrypter, rsaDecrypter, dataAccesor, authCookieUpdater) {
            protected DataBrick<List<ViewerData>> retrieveData() {
                DataBrick<List<ViewerData>> responseDataBrick = new DataBrick<>();
                byte[] key = aesUtil.createAesKey();
                responseDataBrick.setAesKey(rsaEncrypter.encryptAndEncode(key));
                responseDataBrick.setPayload(aesEncrypter.encryptAndEncode("[{\"timestamp\":1573371348278,\"headers\":{\"Accept\":[\"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\"],\"Upgrade-Insecure-Requests\":[\"1\"],\"User-Agent\":[\"Mozilla/5.0\"],\"Connection\":[\"keep-alive\"],\"Host\":[\"localhost:8804\"],\"Accept-Language\":[\"en-US,en;q=0.5\"],\"Accept-Encoding\":[\"gzip,deflate\"]},\"body\":\"\"}]", key));
                
                return responseDataBrick;
            }
        };
        
        dataSaved = false;
        testViewerDataConsumer = viewerDataConsumer;
    }

    @Test
    public void testViewerDataConsume() throws IOException {
        testViewerDataConsumer.doWork();

        assertTrue("Data must be saved", dataSaved);
    }

}
