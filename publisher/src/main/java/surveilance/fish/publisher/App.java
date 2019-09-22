package surveilance.fish.publisher;

import java.util.Map;

import surveilance.fish.persistence.simple.FileSaver;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaDecrypter;
import surveilance.fish.security.RsaEncrypter;

public class App {

    public static final String RSA_PRIVATE_KEY_ENCODED = "rsa.private.key.encoded";
    public static final String DEFAULT_APP_PROPERTIES_PATH = "classpath:/app.properties";
    
    public static final String PROP_AUTH_COOKIE = "auth.cookie";

    public static void main(String[] args) throws InterruptedException {
        String pathToFile = null;
        if (args.length > 0) {
            pathToFile = args[0];
        } else {
            pathToFile = DEFAULT_APP_PROPERTIES_PATH;
        }
        Map<String, String> properties = (Map)(new PropertiesReader().readProperties(pathToFile));
        System.out.println("Starting image producer with properties: " + properties);

        new AuthCookieUpdater(properties, new RsaEncrypter(properties.get(RSA_PRIVATE_KEY_ENCODED)), new AesEncrypter(), new AesUtil())
            .update(properties.get(PROP_AUTH_COOKIE));
        
        new ImageProducer(properties
                , new RsaEncrypter(properties.get(RSA_PRIVATE_KEY_ENCODED))
                , new AesEncrypter()
                , new AesUtil())
            .start();
        new ViewerDataConsumer(properties
                , new AesDecrypter()
                , new RsaDecrypter(properties.get(RSA_PRIVATE_KEY_ENCODED), true)
                , new FileSaver(properties.get("persist.data.folder.path")))
            .start();

        Thread.currentThread().join();
    }
}
