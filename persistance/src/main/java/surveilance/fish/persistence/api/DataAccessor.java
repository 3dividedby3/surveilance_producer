package surveilance.fish.persistence.api;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

public interface DataAccessor<T extends BaseData> {

    public T getNewestData(TypeReference<T> typeReference) throws IOException;
    
    public List<T> getData(int id);
    
    public void saveData(T data) throws IOException;
}
