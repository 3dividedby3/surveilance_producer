package surveilance.fish.publisher;

import static surveilance.fish.publisher.App.PROP_CLIENT_TIMEOUT;
import static surveilance.fish.publisher.App.SECOND;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import surveilance.fish.model.DataBrick;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaEncrypter;

public class AuthCookieUpdater {
    
    public static final String PROP_AUTH_MANAGER_URL = "auth.manager.url";

    private final RsaEncrypter rsaEncrypter;
    private final AesEncrypter aesEncrypter;
    private final AesUtil aesUtil;
    
    private final ObjectWriter objectWriter;
    private final CloseableHttpClient httpClient;
    
    private final String authManagerUrl;
    
    public AuthCookieUpdater(Map<String, String> properties, RsaEncrypter rsaEncrypter, AesEncrypter aesEncrypter, AesUtil aesUtil) {
        this.rsaEncrypter = rsaEncrypter;
        this.aesEncrypter = aesEncrypter;
        this.aesUtil = aesUtil;
        
        objectWriter = new ObjectMapper().writer();
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(SECOND * Integer.valueOf(properties.get(PROP_CLIENT_TIMEOUT)))
                .setConnectionRequestTimeout(SECOND * Integer.valueOf(properties.get(PROP_CLIENT_TIMEOUT)))
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        authManagerUrl = properties.get(PROP_AUTH_MANAGER_URL);
    }
    
    public int update(String authCookie) {
        int statusCode = -1;
        String dataBrickJson = createDataBrickJson(authCookie);
        
        HttpPut putRequest = new HttpPut(authManagerUrl);
        putRequest.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
        HttpEntity input = new ByteArrayEntity(dataBrickJson.getBytes());
        putRequest.setEntity(input);
        try(CloseableHttpResponse response = httpClient.execute(putRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("Auth cookie set to: " + authCookie);
            } else {
                System.out.println("Could not set auth cookie, received statusCode: " + statusCode);
            }
        } catch (IOException e) {
            System.out.println("Error while sending auth cookie to the UI: " + e.getMessage());
            e.printStackTrace();
        }
        
        return statusCode;
    }

    private String createDataBrickJson(String authCookie) {
        DataBrick<byte[]> dataBrick = new DataBrick<>();
        byte[] key = aesUtil.createAesKey();
        dataBrick.setAesKey(rsaEncrypter.encryptAndEncode(key));
        dataBrick.setPayload(aesEncrypter.encryptAndEncode(authCookie, key));
        String dataBrickJson;
        try {
            dataBrickJson = objectWriter.writeValueAsString(dataBrick);
        } catch (JsonProcessingException e) {
            throw new PublisherException("Cannot write data brick as string", e);
        }
        
        return dataBrickJson;
    }
}
