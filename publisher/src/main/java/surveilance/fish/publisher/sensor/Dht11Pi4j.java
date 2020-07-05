package surveilance.fish.publisher.sensor;

import com.pi4j.wiringpi.Gpio;

public class Dht11Pi4j {
    
    public class TempHum {

        private float humidity;
        private float temperature;
        
        /**
         * @return the humidity
         */
        public float getHumidity() {
            return humidity;
        }
        
        /**
         * @param humidity the humidity to set
         */
        public void setHumidity(float humidity) {
            this.humidity = humidity;
        }
        
        /**
         * @return the temperature
         */
        public float getTemperature() {
            return temperature;
        }
        
        /**
         * @param temperature the temperature to set
         */
        public void setTemperature(float temperature) {
            this.temperature = temperature;
        }   
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TempHum [humidity=" + humidity + ", temperature=" + temperature + "]";
        }
    }
    
    private int pin;

    public Dht11Pi4j(int pin) throws SensorException {
        this.pin = pin;
        init();
    }
    
    protected void init() throws SensorException {
        if (Gpio.wiringPiSetupGpio() == -1) {
            throw new SensorException("Could not set up gpio");
        }
    }

    public TempHum readDht11Data() throws SensorException, InterruptedException {
        TempHum response = new TempHum();
        //maxNrOfReads must be large enough to ensure all bits are read, the value is determined empirically
        int maxNrOfReads = 50000;
        int currentValue;
        int nrOfOnes = 0;
        int[] bitData = new int[40];
        int bitDataIdx = 0;
        int bitLengthSum = 0;

        Gpio.pinMode(pin, Gpio.OUTPUT);
        Gpio.digitalWrite(pin, Gpio.HIGH);
        Gpio.digitalWrite(pin, Gpio.LOW);
        Thread.sleep(18);
        Gpio.pinMode(pin, Gpio.INPUT);
        while(maxNrOfReads-- > 0) {
            currentValue = Gpio.digitalRead(pin);
            if (currentValue == 1) {
                ++nrOfOnes;
            } else if (nrOfOnes != 0) {
                //skip first two bits since they are sent as control signal 
                if (bitDataIdx > 1) {
                    bitData[bitDataIdx - 2] = nrOfOnes;
                    bitLengthSum += nrOfOnes;
                }
                ++bitDataIdx;
                if (bitDataIdx >= 42) {
                    break;
                }
                nrOfOnes = 0;
            }
        }
        if (maxNrOfReads <= 0) {
            throw new SensorException("Last bit could not be read");
        }
        int averageBitLength = bitLengthSum / bitData.length;
        for (int i = 0; i < bitData.length; ++i) {
            bitData[i] = bitData[i] > averageBitLength ? 1: 0;
        }
        
        int firstValue = Integer.parseInt(makeStringFromBinary(bitData, 0), 2);
        int secondValue = Integer.parseInt(makeStringFromBinary(bitData, 1), 2);
        int thirdValue = Integer.parseInt(makeStringFromBinary(bitData, 2), 2);
        int fourthValue = Integer.parseInt(makeStringFromBinary(bitData, 3), 2);
        int fifthValue = Integer.parseInt(makeStringFromBinary(bitData, 4), 2);

        //normally dht11 does not send useful data on secondValue and fourthValue, they are supposed to be used only for checksum
        //but it also appears to be valid data that can be used for humidity and temperature calculations respectively
        float humidity = firstValue + secondValue * 0.1f;
        float temperature = thirdValue + fourthValue * 0.1f;
        
        if (firstValue + secondValue + thirdValue + fourthValue != fifthValue) {
            throw new SensorException("Invalid values: humidity=" + humidity + ", temperature=" + temperature);
        }
        response.setHumidity(humidity);
        response.setTemperature(temperature);
        
        return response;
    }
    
    private String makeStringFromBinary(int[] extractedData, int byteNumber) {
        StringBuilder buildFromBinary = new StringBuilder();
        for (int i = byteNumber * 8; i < byteNumber * 8 + 8; ++i) {
            buildFromBinary.append(extractedData[i]);
        }

        return buildFromBinary.toString();
    }
}
