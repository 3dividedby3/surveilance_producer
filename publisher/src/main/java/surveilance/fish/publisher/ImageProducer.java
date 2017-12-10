package surveilance.fish.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import surveilance.fish.model.DataBrick;

/**
 * It is the job of the producer to encode the image data to base 64 before sending it to the consumer
 */
public class ImageProducer {
    
    public static final String CONSUMER_URL = "https://imaage.herokuapp.com/";
    public static final Path PATH_TO_IMAGES_FOLDER = Paths.get("D:/images/");
    public static final long DELAY_MS_FOR_PRODUCER = 1000 * 20;
    public static final int HTTP_CLIENT_TIMEOUT = 1000 * 15;
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));
    
    private final RsaEncrypter rsaEncrypter;
    private final AesEncrypter aesEncrypter;
    private final AesUtil aesUtil;
    
    private final ObjectWriter objectWriter;
    private final CloseableHttpClient httpClient;
    
    public ImageProducer(RsaEncrypter rsaEncrypter, AesEncrypter aesEncrypter, AesUtil aesUtil) {
        this.rsaEncrypter = rsaEncrypter;
        this.aesEncrypter = aesEncrypter;
        this.aesUtil = aesUtil;
        
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(HTTP_CLIENT_TIMEOUT)
                .setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        System.out.println("Application started from: " + BASE_PATH);
        System.out.println("Reading images from from: " + PATH_TO_IMAGES_FOLDER);
        System.out.println("Sending data to: " + CONSUMER_URL);
    }

    public void start() throws InterruptedException {
        Thread t = new Thread(() -> produceImages());
        t.start();
        System.out.println("Started!");
        t.join();
    }

    private void produceImages() {
        while(true) {
            byte[] imageData = null;
            try {
                imageData = getNewestImageData();
                if (imageData != null) {
                    String dataBrickJson = objectWriter.writeValueAsString(createDataBrick(imageData));
                    System.out.println("Sending data to consumer: " + dataBrickJson);
                    System.out.println("Consumer responded with: " + sendDataToConsumer(dataBrickJson.getBytes()));
                }
            } catch(IOException e) {
                System.out.println("Error processing image: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                Thread.sleep(DELAY_MS_FOR_PRODUCER);
            } catch (InterruptedException e) {
                //just ignore it...
            }
        }
    }
    
    private DataBrick createDataBrick(byte[] payload) {
        DataBrick dataBrick = new DataBrick();
        byte[] key = aesUtil.createAesKey();
        dataBrick.setAesKey(rsaEncrypter.encryptAndEncode(key));
        dataBrick.setPayload(aesEncrypter.encryptAndEncode(payload, key));
        
        return dataBrick;
    }

    private int sendDataToConsumer(byte[] dataToSend) {
        HttpPut putRequest = new HttpPut(CONSUMER_URL);
        putRequest.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
        HttpEntity input = new ByteArrayEntity(dataToSend);
        putRequest.setEntity(input);
        try(CloseableHttpResponse response = httpClient.execute(putRequest)) {
            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            System.out.println("Error while sending image to the consumer: " + e.getMessage());
        }
        
        return -1;
    }

    /**
     * @return base 64 encoded image data
     * @throws IOException 
     */
    private byte[] getNewestImageData() throws IOException {
        File newestFile = getNewestFile(PATH_TO_IMAGES_FOLDER);
        if (newestFile == null) {
            System.out.println("No file found");
            return null;
        }
        System.out.println("Newest file found: " + newestFile);
        byte[] rawData = Files.readAllBytes(newestFile.toPath()); 
        
        return BASE64_ENCODER.encode(rawData);
    }

    private File getNewestFile(Path pathToFolder) throws IOException {
        File newestFile = null;
        try (Stream<Path> pathList = Files.list(pathToFolder)) {
            for (Iterator<Path> iterator = pathList.iterator(); iterator.hasNext();) {
                Path currentPath = iterator.next();
                if (Files.isRegularFile(currentPath)) {
                    File currentFile = currentPath.toFile();
                    if (newestFile == null) {
                        newestFile = currentFile;
                        continue;
                    }
                    if (currentFile.lastModified() > newestFile.lastModified()) {
                        newestFile = currentFile;
                    }
                }
            }    
        }
        
        return newestFile;
    }

    
}
