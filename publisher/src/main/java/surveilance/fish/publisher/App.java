package surveilance.fish.publisher;

import java.util.Map;

public class App {

    public static final String RSA_PRIVATE_KEY_ENCODED = "rsa.private.key.encoded";
    public static final String DEFAULT_APP_PROPERTIES_PATH = "classpath:/app.properties";

    public static void main(String[] args) throws InterruptedException {
        String pathToFile = null;
        if (args.length > 0) {
            pathToFile = args[0];
        } else {
            pathToFile = DEFAULT_APP_PROPERTIES_PATH;
        }
        Map<String, String> properties = (Map)(new PropertiesReader().readProperties(pathToFile));
        System.out.println("Starting image producer with properties: " + properties);
        new ImageProducer(properties, new RsaEncrypter(properties.get(RSA_PRIVATE_KEY_ENCODED)), new AesEncrypter(), new AesUtil()).start();
    }
}
