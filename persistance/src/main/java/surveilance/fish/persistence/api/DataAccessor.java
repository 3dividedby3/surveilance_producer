package surveilance.fish.persistence.api;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

public interface DataAccessor<T extends BaseData> {
    
    public List<T> getLastNoOfElems(int noOfElem, TypeReference<T> typeReference) throws DataAccessException;
    
    public void saveData(T data) throws DataAccessException;
}
