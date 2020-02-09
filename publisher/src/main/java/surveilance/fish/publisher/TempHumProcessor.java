package surveilance.fish.publisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

public class TempHumProcessor {

    public static final String NO_TEMP_HUM_DATA = "no temp+hum data";
    final static public String TEMP_LINE_START = "Sample OK: ";
    public static final String ARDUINO_USB_LOCATION = "/dev/ttyACM0";
    
    public String getTempAndHumData() throws IOException {
        String tempAndHum = NO_TEMP_HUM_DATA;
        try(Stream<String> linesStream = Files.lines(Paths.get(ARDUINO_USB_LOCATION))) {
            Optional<String> tempAndHumLine = linesStream.filter(line -> line.startsWith(TEMP_LINE_START)).findFirst();
            tempAndHum = tempAndHumLine.map(line -> line.substring(TEMP_LINE_START.length())).orElse(NO_TEMP_HUM_DATA);
        }
        System.out.println(new Date() + " - Temperature and humidity: " + tempAndHum);
        
        return tempAndHum;
    }

}
