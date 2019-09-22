package surveilance.fish.publisher;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static surveilance.fish.publisher.App.PROP_AUTH_COOKIE;
import org.apache.http.HttpEntity;
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
import static surveilance.fish.publisher.ViewerDataConsumer.NAME_AUTH_COOKIE;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import surveilance.fish.model.DataBrick;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaEncrypter;

/**
 * It is the job of the producer to encode the image data to base 64 before sending it to the consumer
 */
public class ImageProducer {
    public static final String SEND_IMAGE_DELAY = "send.image.delay";
    public static final String CONSUMER_URL = "consumer.url";
    public static final String IMAGES_FOLDER_PATH = "images.folder.path";
    public static final String CLIENT_TIMEOUT = "client.timeout";
    
    public static final int SECOND = 1000;
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));

    private final RsaEncrypter rsaEncrypter;
    private final AesEncrypter aesEncrypter;
    private final AesUtil aesUtil;
    
    private final ObjectWriter objectWriter;
    private final CloseableHttpClient httpClient;
    private final Path pathToImagesFolder;
    
    private final int sendImageDelay;
    private final int clientTimeout;
    private final String consumerUrl;
    
    private final String authCookie;

    public ImageProducer(Map<String, String> properties, RsaEncrypter rsaEncrypter, AesEncrypter aesEncrypter, AesUtil aesUtil) {
        this.rsaEncrypter = rsaEncrypter;
        this.aesEncrypter = aesEncrypter;
        this.aesUtil = aesUtil;
        sendImageDelay = SECOND * Integer.valueOf(properties.get(SEND_IMAGE_DELAY));
        clientTimeout = SECOND * Integer.valueOf(properties.get(CLIENT_TIMEOUT));
        consumerUrl = properties.get(CONSUMER_URL);
        pathToImagesFolder = Paths.get(properties.get(IMAGES_FOLDER_PATH));
        
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(clientTimeout)
                .setConnectionRequestTimeout(clientTimeout)
                .build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        
        authCookie = properties.get(PROP_AUTH_COOKIE);
        
        System.out.println("Application started from: " + BASE_PATH);
        System.out.println("Reading images from from: " + pathToImagesFolder);
        System.out.println("Sending data to: " + consumerUrl);
    }

    public void start() throws InterruptedException {
        Thread t = new Thread(() -> produceImages());
        t.start();
        System.out.println("Started image producer!");
    }

    private void produceImages() {
        while(true) {
            try {
                doWork();
            } catch(Throwable t) {
                System.out.println("Error while producing image: " + t.getMessage());
                t.printStackTrace();
            }

            try {
                Thread.sleep(sendImageDelay);
            } catch (InterruptedException e) {
                //just ignore it...
            }
        }
    }

    private void doWork() {
        byte[] imageData = null;
        try {
            imageData = getNewestImageData();
            if (imageData == null) {
                return;
            }
            String dataBrickJson = objectWriter.writeValueAsString(createDataBrick(imageData));
//          System.out.println("Sending data to consumer: " + dataBrickJson);
            System.out.println("Consumer responded with: " + sendDataToConsumer(dataBrickJson.getBytes()));
        } catch(IOException e) {
            System.out.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private DataBrick<byte[]> createDataBrick(byte[] payload) {
        DataBrick<byte[]> dataBrick = new DataBrick<>();
        byte[] key = aesUtil.createAesKey();
        dataBrick.setAesKey(rsaEncrypter.encryptAndEncode(key));
        dataBrick.setPayload(aesEncrypter.encryptAndEncode(payload, key));
        
        return dataBrick;
    }

    private int sendDataToConsumer(byte[] dataToSend) {
        HttpPut putRequest;
        try {
            URIBuilder builder = new URIBuilder(consumerUrl);
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
            System.out.println("Error while sending image to the consumer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * @return base 64 encoded image data
     * @throws IOException 
     */
    private byte[] getNewestImageData() throws IOException {
        File newestFile = getNewestFile(pathToImagesFolder);
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
