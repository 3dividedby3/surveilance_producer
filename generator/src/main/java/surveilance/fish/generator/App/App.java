package surveilance.fish.generator.App;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * Generate a RSA key pair that will be used by the image publisher and surveillance web service to communicate
 * This will provide authentication of the publisher to the webservice by being able to decrypt messages that arrive from the publisher only.
 */
public class App {

    public static final String ALGORITHM_RSA = "RSA";
    public static final String ROOT_FOLDER = "src/main/resources";
    public static final int KEY_LENGTH = 1024;

    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    
    private final KeyPairGenerator keyGenerator;
    private final KeyPair keyPair;

    private App() {
        try {
            keyGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
        } catch(NoSuchAlgorithmException e) {
            System.out.println("Could not create [" + ALGORITHM_RSA + "] key generator: " + e.getMessage());
            throw new GeneratorException(e);
        }
        keyGenerator.initialize(KEY_LENGTH);
        keyPair = keyGenerator.generateKeyPair();
    }
    
    public static void main(String[] args) {
        App app = new App();
        app.saveKeyToFile(Paths.get(ROOT_FOLDER, "privateKey"), app.getKeyPair().getPrivate());
        app.saveKeyToFile(Paths.get(ROOT_FOLDER, "publicKey"), app.getKeyPair().getPublic());
    }

    private void saveKeyToFile(Path filePath, Key key) {
        try(OutputStream pirvateKeyFos = new FileOutputStream(filePath.toFile())) {
            pirvateKeyFos.write(BASE64_ENCODER.encode(key.getEncoded()));
        } catch(IOException e) {
            System.out.println("Could not save key: " + e.getMessage());
            throw new GeneratorException(e);
        }
        System.out.println("Saved key to file [" + filePath.toString() + "]");
    }

    private KeyPair getKeyPair() {
        return keyPair;
    }
}
