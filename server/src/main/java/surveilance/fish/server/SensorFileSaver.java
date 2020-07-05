package surveilance.fish.server;

import surveilance.fish.persistence.simple.FileSaver;
import surveilance.fish.server.sensor.SensorData;

public class SensorFileSaver extends FileSaver<SensorData> {

    private final String id;
    
    public SensorFileSaver(String id, String path) {
        super(path);
        this.id = id;
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
}
