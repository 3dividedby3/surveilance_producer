package surveilance.fish.security;

public class SecurityException extends RuntimeException {

    private static final long serialVersionUID = 1006432934034402627L;

    public SecurityException(Throwable t) {
        super(t);
    }
    
    public SecurityException(String message) {
        super(message);
    }
    
    public SecurityException(String message, Throwable t) {
        super(message, t);
    }
}
