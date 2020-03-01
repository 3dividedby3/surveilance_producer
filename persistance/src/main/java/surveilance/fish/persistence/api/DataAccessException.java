package surveilance.fish.persistence.api;

public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 8941591607174803108L;
    
    public DataAccessException() {    
    }

    public DataAccessException(Throwable source) {
        super(source);
    }
}
