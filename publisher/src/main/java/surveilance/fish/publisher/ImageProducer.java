package surveilance.fish.publisher;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

/**
 * It is the job of the producer to encode the data to base 64 before sending it to the consumer
 */
public class ImageProducer {
    
    public static final String CONSUMER_URL = "https://imaage.herokuapp.com/";
    public static final long DELAY_MS_FOR_PRODUCER = 1000 * 20;
    public static final int HTTP_CLIENT_TIMEOUT = 1000 * 30;
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    
    private final RsaEncrypter rsaEncrypter;
    private final CloseableHttpClient httpClient;
    
    public ImageProducer(RsaEncrypter rsaEncrypter) {
        this.rsaEncrypter = rsaEncrypter;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(HTTP_CLIENT_TIMEOUT)
                .setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    public void start() throws InterruptedException {
        Thread t = new Thread(() -> produceImages());
        t.start();
        System.out.println("Started!");
        t.join();
    }

    private void produceImages() {
        while(true) {
            byte[] encodedEncryptedData = rsaEncrypter.encryptAndEncode(getImageData());
            System.out.println("Sending data to consumer: " + new String(encodedEncryptedData));
            int responseCode = sendDataToConsumer(encodedEncryptedData);
            System.out.println("Consumer responded with: " + responseCode);
            try {
                Thread.sleep(DELAY_MS_FOR_PRODUCER);
            } catch (InterruptedException e) {
                //just ignore it...
            }
        }
    }

    private int sendDataToConsumer(byte[] dataToSend) {
        HttpPut putRequest = new HttpPut(CONSUMER_URL);
        putRequest.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain; charset=utf-8"));
        HttpEntity input = new ByteArrayEntity(dataToSend);
        putRequest.setEntity(input);
        try {
            return httpClient.execute(putRequest).getStatusLine().getStatusCode();
        } catch (IOException e) {
            System.out.println("Error while sending image to the consumer: " + e.getMessage());
        }
        
        return HttpStatus.SC_BAD_REQUEST;
    }

    /**
     * @return base 64 encoded image data
     */
    private byte[] getImageData() {
        // TODO just stub for now, it will read image data from the disk
        byte[] rawData = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==".getBytes();
        
        BASE64_ENCODER.encode(rawData);
        //TODO: do not forget to encode in the final version, the line above
        return rawData;
    }

    
}
