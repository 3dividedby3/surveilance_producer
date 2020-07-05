package surveilance.fish.publisher;

import static surveilance.fish.common.PropertiesReader.DEFAULT_APP_PROPERTIES_PATH;
import static surveilance.fish.common.base.BaseRepeatableTask.SECOND;
import static surveilance.fish.server.SupportServer.PROP_SUPPORT_SERVER_PORT;
import static surveilance.fish.server.SupportServer.PROP_SUPPORT_SERVER_TEMPHUM_LOCATION;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.http.HttpStatus;

import surveilance.fish.common.PropertiesReader;
import surveilance.fish.persistence.simple.FileSaver;
import surveilance.fish.publisher.audit.ViewerDataConsumer;
import surveilance.fish.publisher.sensor.TempHumProcessor;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaDecrypter;
import surveilance.fish.security.RsaEncrypter;
import surveilance.fish.server.SensorFileSaver;
import surveilance.fish.server.SupportServer;
import surveilance.fish.server.sensor.SensorData;

public class App {
    
    private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));

    public static final String PROP_CLIENT_TIMEOUT = "client.timeout";
    
    public static final String PROP_RSA_PRIVATE_KEY_ENCODED = "rsa.private.key.encoded";
    
    public static final String PROP_PERSIST_DATA_FOLDER_PATH = "persist.data.folder.path";
    public static final String PROP_PERSIST_TEMPHUM_FOLDER_PATH = "persist.temphum.folder.path";
    
    public static final String PROP_AUTH_COOKIE = "auth.cookie";

    public static void main(String[] args) throws InterruptedException {
        //since this application is started from cron when the system boots, it needs to
        //sleep to give the system enough time to initialize the network, otherwise it will fail
        Thread.sleep(10 * SECOND);
        
        String pathToPropFile = null;
        if (args.length > 0) {
            pathToPropFile = args[0];
        } else {
            pathToPropFile = DEFAULT_APP_PROPERTIES_PATH;
        }
        Map<String, String> properties = (Map)(new PropertiesReader().readProperties(pathToPropFile));
        System.out.println("[publisher] Starting with properties: " + properties);

        FileSaver<SensorData> localSensorDataFileSaver 
            = new FileSaver<>(properties.get(PROP_PERSIST_TEMPHUM_FOLDER_PATH));
        SensorFileSaver remoteOneSensorFileSaver 
            = new SensorFileSaver("remoteone", properties.get(PROP_SUPPORT_SERVER_TEMPHUM_LOCATION) + "temphum/remoteone");

        new SupportServer(Integer.valueOf(properties.get(PROP_SUPPORT_SERVER_PORT)), remoteOneSensorFileSaver)
            .start();

        AuthCookieUpdater authCookieUpdater = new AuthCookieUpdater(properties, new RsaEncrypter(properties.get(PROP_RSA_PRIVATE_KEY_ENCODED)), new AesEncrypter(), new AesUtil());
        int authStatusCode = authCookieUpdater.update(properties.get(PROP_AUTH_COOKIE));
        if (authStatusCode != HttpStatus.SC_OK) {
            //let it run as-is and let any other service give it a try every time it fails
            System.out.println("!!! Auth cookie could noy be set at start-up, continue as-is");
        }
        
        ImageProducer imageProducer = new ImageProducer(properties
                , new RsaEncrypter(properties.get(PROP_RSA_PRIVATE_KEY_ENCODED))
                , new AesEncrypter()
                , new AesUtil()
                , authCookieUpdater
                , localSensorDataFileSaver
                , remoteOneSensorFileSaver);
        imageProducer.start();
        
        new ViewerDataConsumer(properties
                , new AesDecrypter()
                , new RsaDecrypter(properties.get(PROP_RSA_PRIVATE_KEY_ENCODED), true)
                , new FileSaver<>(properties.get(PROP_PERSIST_DATA_FOLDER_PATH))
                , authCookieUpdater)
            .start();
        
        new BeCommandConsumer(properties
                , new AesDecrypter()
                , new RsaDecrypter(properties.get(PROP_RSA_PRIVATE_KEY_ENCODED), true)
                , imageProducer
                , authCookieUpdater)
            .start();
        
        new TempHumProcessor(properties
                , localSensorDataFileSaver)
            .start();
        
        System.out.println("[publisher] Application started from: " + BASE_PATH);

        Thread.currentThread().join();
    }
}
