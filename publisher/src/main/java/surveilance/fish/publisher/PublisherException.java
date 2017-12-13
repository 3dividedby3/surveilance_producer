package surveilance.fish.publisher;

public class PublisherException extends RuntimeException {

    private static final long serialVersionUID = 1006432934034402627L;

    public PublisherException(Throwable t) {
        super(t);
    }
    
    public PublisherException(String message) {
        super(message);
    }
    
    public PublisherException(String message, Throwable t) {
        super(message, t);
    }
}
