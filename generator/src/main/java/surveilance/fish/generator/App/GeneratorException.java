package surveilance.fish.generator.App;

public class GeneratorException extends RuntimeException {

    private static final long serialVersionUID = 1546432934034402687L;

    public GeneratorException(Throwable t) {
        super(t);
    }
    
    public GeneratorException(String message, Throwable t) {
        super(message, t);
    }
}
