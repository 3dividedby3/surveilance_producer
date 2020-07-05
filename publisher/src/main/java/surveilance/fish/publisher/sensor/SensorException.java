package surveilance.fish.publisher.sensor;

import surveilance.fish.common.exc.SurveilanecException;

public class SensorException extends SurveilanecException {

    private static final long serialVersionUID = 5406250092756286505L;

    public SensorException(Throwable t) {
        super(t);
    }

    public SensorException(String message) {
        super(message);
    }

    public SensorException(String message, Throwable t) {
        super(message, t);
    }
}
