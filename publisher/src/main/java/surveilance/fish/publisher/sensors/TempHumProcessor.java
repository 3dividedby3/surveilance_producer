package surveilance.fish.publisher.sensors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.publisher.base.BaseRepeatableTask;

public class TempHumProcessor extends BaseRepeatableTask {
    
    public static final String PROP_TEMP_HUM_PROC_GET_DATA_DELAY = "temphum.proc.get.data.delay";

    public static final String NO_TEMP_HUM_DATA = "no temp and hum data";
    final static public String TEMP_LINE_START = "Sample OK: ";
    public static final String ARDUINO_DEVICE_FILE_USB_LOCATION = "/dev/ttyACM0";
    
    private final DataAccessor<SensorData> dataAccessor;
    
    public TempHumProcessor(Map<String, String> properties, DataAccessor<SensorData> dataAccessor) {
        super(properties);
        this.dataAccessor = dataAccessor;
    }

    @Override
    protected String getRepeatTaskDelay(Map<String, String> properties) {
        return properties.get(PROP_TEMP_HUM_PROC_GET_DATA_DELAY);
    }

    @Override
    protected void doWork() throws IOException {
        saveTempAndHumData();
    }

    private void saveTempAndHumData() throws IOException {
        String tempAndHum = NO_TEMP_HUM_DATA;
        Stream<String> linesStream = Files.lines(Paths.get(ARDUINO_DEVICE_FILE_USB_LOCATION));
        Optional<String> tempAndHumLine = linesStream
                .filter(line -> line.startsWith(TEMP_LINE_START))
                .findFirst();
        long dataReadTime = System.currentTimeMillis();
        tempAndHum = tempAndHumLine.map(line -> line.substring(TEMP_LINE_START.length())).orElse(NO_TEMP_HUM_DATA);
        //TODO: replace with ExecutorService
        new Thread(() -> linesStream.close()).start();

        System.out.println(new Date() + " - Temperature and humidity: " + tempAndHum);
        //27 *C, 57 H
        int temperature = Integer.valueOf(tempAndHum.substring(0, tempAndHum.indexOf(' ')));
        int humidity = Integer.valueOf(tempAndHum.substring(tempAndHum.indexOf(", ") + 2, tempAndHum.length() - 2));
        SensorData sensorData = new SensorData(dataReadTime, temperature, humidity);
        
        dataAccessor.saveData(sensorData);
    }

}
