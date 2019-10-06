package surveilance.fish.publisher.base;

import static surveilance.fish.publisher.App.PROP_AUTH_COOKIE;
import static surveilance.fish.publisher.ImageProducer.SECOND;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import surveilance.fish.model.BeCommand;
import surveilance.fish.model.DataBrick;
import surveilance.fish.publisher.PublisherException;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.RsaDecrypter;

public abstract class BaseConsumer<T> {
    
    //TODO: same constant found in surveilance.fish.business.security.AuthValidator.NAME_AUTH_COOKIE, extract to common module
    public static final String NAME_AUTH_COOKIE = "authCookie";
    public static final String PROP_CLIENT_TIMEOUT = "client.timeout";
    
    public static final DataBrick EMPTY_DATA_BRICK = new DataBrick() {
        @Override
        public void setAesKey(String aesKey) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPayload(String payload) {
            throw new UnsupportedOperationException();
        }
    };

    private final int dataProducerGetDataDelay;
    private final int clientTimeout;
    private final String dataProducerUrl;
    private final CloseableHttpClient httpClient;
    
    private final ObjectMapper objectMapper;
    
    private final AesDecrypter aesDecrypter;
    private final RsaDecrypter rsaDecrypter;

    private final String authCookie;
    
    protected BaseConsumer(Map<String, String> properties, AesDecrypter aesDecrypter, RsaDecrypter rsaDecrypter) {
        dataProducerGetDataDelay = SECOND * Integer.valueOf(getDataProducerGetDelay(properties));
        clientTimeout = SECOND * Integer.valueOf(properties.get(PROP_CLIENT_TIMEOUT));
        dataProducerUrl = getDataProducerUrl(properties);
        
        objectMapper = new ObjectMapper();
        
        this.aesDecrypter = aesDecrypter;
        this.rsaDecrypter = rsaDecrypter;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(clientTimeout)
                .setConnectionRequestTimeout(clientTimeout)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        authCookie = properties.get(PROP_AUTH_COOKIE);
        
        System.out.println("Reading data from: " + dataProducerUrl);
    }

    protected abstract String getDataProducerGetDelay(Map<String, String> properties);
    protected abstract String getDataProducerUrl(Map<String, String> properties);
    protected abstract void doWork();

    protected void repeatDoWork() {
        while(true) {
            try {
                doWork();
            } catch(Throwable t) {
                System.out.println("Error while consuming data: " + t.getMessage());
                t.printStackTrace();
            }
            
            try {
                Thread.sleep(dataProducerGetDataDelay);
            } catch (InterruptedException e) {
                //just ignore it...
            }
        }
    }
    
    protected DataBrick<T> retrieveData() {
        HttpGet getRequest;
        try {
            URIBuilder builder = new URIBuilder(dataProducerUrl);
            builder.setParameter(NAME_AUTH_COOKIE, authCookie);
            getRequest = new HttpGet(builder.build());
        } catch (URISyntaxException e) {
            throw new PublisherException("Cannot create URIBuilder", e);
        }

        DataBrick<T> dataBrick = EMPTY_DATA_BRICK;
        try(CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Data producer [" + dataProducerUrl + "] responded with: " + responseCode);
            if (responseCode == HttpStatus.SC_OK) {
                String body = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining());
                System.out.println("Received data from data producer: " + body);
                dataBrick = objectMapper.readValue(body, new TypeReference<DataBrick<T>>() {});
            }
        } catch (IOException e) {
            System.out.println("Error while reading data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dataBrick;
    }
    
    protected BeCommand decryptPayload(DataBrick<T> dataBrick) throws IOException {
        byte[] aesKey = rsaDecrypter.decrypt(dataBrick.getAesKey().getBytes());
        String decryptedPayload = new String(aesDecrypter.decrypt(dataBrick.getPayload(), aesKey));
        System.out.println("Consumed data after decryption: " + decryptedPayload);

        return objectMapper.readValue(decryptedPayload, BeCommand.class);
    }
}
