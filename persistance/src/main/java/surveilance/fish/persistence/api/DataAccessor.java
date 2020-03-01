package surveilance.fish.persistence.api;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

public interface DataAccessor<T extends BaseData> {
    
    public List<T> getLastNoOfElems(int noOfElem, TypeReference<T> typeReference) throws IOException;
    
    public void saveData(T data) throws IOException;
}
