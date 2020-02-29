package surveilance.fish.publisher.sensors;

import surveilance.fish.persistence.api.BaseData;

public class SensorData extends BaseData {
    
    /** in Celsius */
    private int temperature;
    
    /** in percentage 0 - 100% */
    private int humidity;
    
    public SensorData() {
        super(null);
        //made for jackson
    }
    
    public SensorData(long created, int temperature, int humidity) {
        super(created);
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + humidity;
        result = prime * result + temperature;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SensorData)) {
            return false;
        }
        SensorData other = (SensorData) obj;
        if (humidity != other.humidity) {
            return false;
        }
        if (temperature != other.temperature) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SensorData [temperature=" + temperature + ", humidity=" + humidity + ", getTimestampCreated()="
                + getTimestampCreated() + "]";
    }

}
