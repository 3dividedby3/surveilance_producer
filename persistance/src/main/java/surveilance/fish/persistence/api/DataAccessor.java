package surveilance.fish.persistence.api;

import java.io.IOException;
import java.util.List;

public interface DataAccessor<T> {

    public List<T> getData(int id);
    
    public void saveData(T data) throws IOException;
}
