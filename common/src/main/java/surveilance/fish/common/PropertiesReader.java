package surveilance.fish.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import surveilance.fish.common.exc.SurveilanecException;

public class PropertiesReader {

    public static final String DEFAULT_APP_PROPERTIES_PATH = "classpath:/app.properties";
    public static final String CLASSPATH_PREFIX = "classpath:";

    public Properties readProperties(String pathToFile) {
        Properties properties = new Properties();
        InputStream inputStream;
        try {
            if (pathToFile.startsWith(CLASSPATH_PREFIX)) {
                inputStream = getClass().getResourceAsStream(pathToFile.substring(CLASSPATH_PREFIX.length()));
                if (inputStream == null) {
                    throw new SurveilanecException("Could not read properties file with classpath: " + pathToFile);
                }
            } else {
                inputStream = new FileInputStream(new File(pathToFile));
            }
            properties.load(inputStream);
        } catch (IOException | NullPointerException e) {
            throw new SurveilanecException("Could not read properties file: " + pathToFile, e);
        }
        
        return properties;
    }
}
