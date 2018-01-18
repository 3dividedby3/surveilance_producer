package surveilance.fish.publisher;

import static surveilance.fish.publisher.ImageProducer.SECOND;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import surveilance.fish.model.DataBrick;
import surveilance.fish.model.ViewerData;
import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.RsaDecrypter;

public class ViewerDataConsumer {
    
    private static final DataBrick EMPTY_DATA_BRICK = new DataBrick();
    
    private final int dataProducerGetDataDelay;
    private final int clientTimeout;
    private final String dataProducerUrl;
    private final CloseableHttpClient httpClient;
    
    private final ObjectMapper objectMapper;
    
    private final AesDecrypter aesDecrypter;
    private final RsaDecrypter rsaDecrypter;
    
    private final DataAccessor<ViewerData> dataAccessor;
    
    public ViewerDataConsumer(Map<String, String> properties, AesDecrypter aesDecrypter, RsaDecrypter rsaDecrypter, DataAccessor<ViewerData> dataAccessor) {
        dataProducerGetDataDelay = SECOND * Integer.valueOf(properties.get("data.producer.get.data.delay"));
        clientTimeout = SECOND * Integer.valueOf(properties.get("client.timeout"));
        dataProducerUrl = properties.get("data.producer.url");
        
        objectMapper = new ObjectMapper();
        
        this.aesDecrypter = aesDecrypter;
        this.rsaDecrypter = rsaDecrypter;
        
        this.dataAccessor = dataAccessor;

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(clientTimeout)
                .setConnectionRequestTimeout(clientTimeout)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        System.out.println("Reading data from: " + dataProducerUrl);
        System.out.println("Saving data to: " + properties.get("persist.data.folder.path"));
    }

    public void start() throws InterruptedException {
        Thread t = new Thread(() -> retrieveAndPersistData());
        t.start();
        System.out.println("Started viewer data consumer!");
    }
    
    private void retrieveAndPersistData() {
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

    private void doWork() {
        DataBrick dataBrick = retrieveData();
        if (EMPTY_DATA_BRICK == dataBrick) {
            return;
        }
        byte[] aesKey = rsaDecrypter.decrypt(dataBrick.getAesKey().getBytes());
        String viewerData = new String(aesDecrypter.decrypt(dataBrick.getPayload(), aesKey));
        System.out.println("ViewerData after decryption: " + viewerData);
        try {
            List<ViewerData> listViewerData = objectMapper.readValue(viewerData, new TypeReference<List<ViewerData>>() {});
            for (ViewerData currentViewerData : listViewerData) {
                dataAccessor.saveData(currentViewerData);
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DataBrick retrieveData() {
        HttpGet getRequest = new HttpGet(dataProducerUrl);
        DataBrick dataBrick = EMPTY_DATA_BRICK;
        try(CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Viewer data producer responded with: " + responseCode);
            if (responseCode == HttpStatus.SC_OK) {
                String body = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining());
                System.out.println("Received data from viewer data producer: " + body);
                dataBrick = objectMapper.readValue(body, DataBrick.class);
            }
        } catch (IOException e) {
            System.out.println("Error while sending image to the consumer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dataBrick;
    }

}
