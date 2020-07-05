package surveilance.fish.publisher.sensor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import surveilance.fish.common.base.BaseRepeatableTask;
import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.publisher.sensor.Dht11Pi4j.TempHum;
import surveilance.fish.server.sensor.SensorData;

public class TempHumProcessor extends BaseRepeatableTask {
    
    public static final String PROP_TEMP_HUM_PROC_GET_DATA_DELAY = "temphum.proc.get.data.delay";
    public static final String PROP_TEMP_HUM_DHT11_PIN = "temphum.dht11.pin";

    public static final String NO_TEMP_HUM_DATA = "no temp and hum data";
    
    private static final int NR_OF_RETRIES_WHEN_ERROR = 10;
    
    private final DataAccessor<SensorData> dataAccessor;
    private final Dht11Pi4j dht11Pi4j;
    
    public TempHumProcessor(Map<String, String> properties, DataAccessor<SensorData> dataAccessor) {
        super(properties);
        this.dataAccessor = dataAccessor;
        dht11Pi4j = new Dht11Pi4j(Integer.valueOf(properties.get(PROP_TEMP_HUM_DHT11_PIN)));
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
        TempHum tempAndHum = null;
        int retries = NR_OF_RETRIES_WHEN_ERROR;
        while(retries-- > 0) {
            try {
                tempAndHum = dht11Pi4j.readDht11Data();
                break;
            } catch(InterruptedException ie) {
                System.out.println("Could not read temp because of interruption, aborting: " + ie.getMessage());
                return;
            } catch(SensorException se) {
                System.out.println("Sensor exception during read, trying again: " + se.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //just ignore it...
                    e.printStackTrace();
                }
            }
        }
        if (tempAndHum == null) {
            //no correct data was read, nothing more to do
            return;
        }
        long dataReadTime = System.currentTimeMillis();
        System.out.println(new Date() + " - Temperature and humidity: " + tempAndHum);
        int temperature = (int)tempAndHum.getTemperature();
        int humidity = (int)tempAndHum.getHumidity();
        SensorData sensorData = new SensorData(dataReadTime, temperature, humidity);
        
        dataAccessor.saveData(sensorData);
    }

}
