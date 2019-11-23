package surveilance.fish.publisher.base;

import static surveilance.fish.publisher.App.PROP_AUTH_COOKIE;
import static surveilance.fish.publisher.ViewerDataConsumer.NAME_AUTH_COOKIE;
import static surveilance.fish.publisher.base.BaseConsumer.PROP_CLIENT_TIMEOUT;
import static surveilance.fish.publisher.App.SECOND;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
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
import surveilance.fish.publisher.AuthCookieUpdater;
import surveilance.fish.publisher.PublisherException;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaEncrypter;

/**
 * It is the job of the producer to encode the data to base 64 before sending it to the consumer
 */
public abstract class BaseProducer<T> extends BaseRepeatableTask {
    
    private final RsaEncrypter rsaEncrypter;
    private final AesEncrypter aesEncrypter;
    private final AesUtil aesUtil;
    
    private final AuthCookieUpdater authCookieUpdater;
    private final String authCookie;

    private final int clientTimeout;
    private final String dataConsumerUrl;
    private final CloseableHttpClient httpClient;
    
    private final ObjectWriter objectWriter;

    public BaseProducer(Map<String, String> properties, RsaEncrypter rsaEncrypter, AesEncrypter aesEncrypter, AesUtil aesUtil, AuthCookieUpdater authCookieUpdater) {
        super(properties);
        this.rsaEncrypter = rsaEncrypter;
        this.aesEncrypter = aesEncrypter;
        this.aesUtil = aesUtil;
        this.authCookieUpdater = authCookieUpdater;
        
        authCookie = properties.get(PROP_AUTH_COOKIE);
        clientTimeout = SECOND * Integer.valueOf(properties.get(PROP_CLIENT_TIMEOUT));
        dataConsumerUrl = getDataConsumerUrl(properties);
        
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(clientTimeout)
                .setConnectionRequestTimeout(clientTimeout)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        System.out.println("Producer [" + getClass().getName() + "] is sending data to: " + dataConsumerUrl);
    }

    protected abstract String getDataConsumerUrl(Map<String, String> properties);

    
    protected void processData(T dataToProcess) throws IOException {
        String dataBrickJson = objectWriter.writeValueAsString(createDataBrick(dataToProcess));
//          System.out.println("Sending data to consumer: " + dataBrickJson);
        int statusCode = sendDataToConsumer(dataBrickJson.getBytes());
        if (statusCode != HttpStatus.SC_OK) {
            authCookieUpdater.update(authCookie);
        }
        System.out.println("Consumer responded with: " + statusCode);
    }

    private DataBrick<T> createDataBrick(T payload) {
        DataBrick<T> dataBrick = new DataBrick<>();
        byte[] key = aesUtil.createAesKey();
        dataBrick = new DataBrick<>();
        String payloadString;
        if (payload instanceof byte[]) {
            payloadString = new String((byte[])payload);
        } else {
            try {
                payloadString = objectWriter.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new PublisherException("Cannot write payload as string", e);
            }
        }
        dataBrick.setAesKey(rsaEncrypter.encryptAndEncode(key));
        dataBrick.setPayload(aesEncrypter.encryptAndEncode(payloadString, key));
        
        return dataBrick;
    }
    
    private int sendDataToConsumer(byte[] dataToSend) {
        HttpPut putRequest;
        try {
            URIBuilder builder = new URIBuilder(dataConsumerUrl);
            builder.setParameter(NAME_AUTH_COOKIE, authCookie);
            putRequest = new HttpPut(builder.build());
        } catch (URISyntaxException e) {
            throw new PublisherException("Cannot create URIBuilder", e);
        }

        putRequest.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
        HttpEntity input = new ByteArrayEntity(dataToSend);
        putRequest.setEntity(input);
        try(CloseableHttpResponse response = httpClient.execute(putRequest)) {
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            System.out.println("Error while sending data to the consumer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
}
