package surveilance.fish.publisher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.http.HttpStatus;

import surveilance.fish.persistence.simple.FileSaver;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaDecrypter;
import surveilance.fish.security.RsaEncrypter;

public class App {

    public static final int SECOND = 1000;
    
    private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));

    public static final String PROP_CLIENT_TIMEOUT = "client.timeout";
    
    public static final String RSA_PRIVATE_KEY_ENCODED = "rsa.private.key.encoded";
    public static final String DEFAULT_APP_PROPERTIES_PATH = "classpath:/app.properties";
    
    public static final String PROP_AUTH_COOKIE = "auth.cookie";

    public static void main(String[] args) throws InterruptedException {
        //sleeping to give the system enough time to initialize the network, otherwise it will fail
        Thread.sleep(10 * SECOND);
        
        String pathToPropFile = null;
        if (args.length > 0) {
            pathToPropFile = args[0];
        } else {
            pathToPropFile = DEFAULT_APP_PROPERTIES_PATH;
        }
        Map<String, String> properties = (Map)(new PropertiesReader().readProperties(pathToPropFile));
        System.out.println("Starting image producer with properties: " + properties);

        AuthCookieUpdater authCookieUpdater = new AuthCookieUpdater(properties, new RsaEncrypter(properties.get(RSA_PRIVATE_KEY_ENCODED)), new AesEncrypter(), new AesUtil());
        int authStatusCode = authCookieUpdater.update(properties.get(PROP_AUTH_COOKIE));
        if (authStatusCode != HttpStatus.SC_OK) {
            System.out.println("!!! Auth cookie could noy be set at start-up, continue as-is");
            //let it run as-is and let any other service give it a try every time it fails
        }
        
        ImageProducer imageProducer = new ImageProducer(properties
                , new RsaEncrypter(properties.get(RSA_PRIVATE_KEY_ENCODED))
                , new AesEncrypter()
                , new AesUtil()
                , authCookieUpdater
                , new TempHumProcessor());
        imageProducer.start();
        
        new ViewerDataConsumer(properties
                , new AesDecrypter()
                , new RsaDecrypter(properties.get(RSA_PRIVATE_KEY_ENCODED), true)
                , new FileSaver(properties.get("persist.data.folder.path"))
                , authCookieUpdater)
            .start();
        
        new BeCommandConsumer(properties
                , new AesDecrypter()
                , new RsaDecrypter(properties.get(RSA_PRIVATE_KEY_ENCODED), true)
                , imageProducer
                , authCookieUpdater)
            .start();
        
        System.out.println("Application started from: " + BASE_PATH);

        Thread.currentThread().join();
    }
}
