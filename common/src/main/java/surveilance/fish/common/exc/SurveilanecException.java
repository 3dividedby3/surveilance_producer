package surveilance.fish.common.exc;

public class SurveilanecException extends RuntimeException {

    private static final long serialVersionUID = 1006432934034402627L;

    public SurveilanecException(Throwable t) {
        super(t);
    }
    
    public SurveilanecException(String message) {
        super(message);
    }
    
    public SurveilanecException(String message, Throwable t) {
        super(message, t);
    }
}
