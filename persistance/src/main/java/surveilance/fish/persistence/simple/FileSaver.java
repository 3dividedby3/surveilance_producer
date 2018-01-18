package surveilance.fish.persistence.simple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import surveilance.fish.model.ViewerData;
import surveilance.fish.persistence.api.DataAccessor;

public class FileSaver implements DataAccessor<ViewerData> {
    
   private String saveLocationPath;
   
   private final ObjectWriter objectWriter;

   public FileSaver(String path) {
       saveLocationPath = path;
       objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
   }
   
    @Override
    public List<ViewerData> getData(int id) {
        // TODO add retrieve logic using id as hashCode
        return null;
    }

    @Override
    public void saveData(ViewerData data) throws IOException {
        String fileName = data.getTimestamp() + "_" + data.hashCode() + ".json";
        Files.write(Paths.get(saveLocationPath, fileName), objectWriter.writeValueAsString(data).getBytes());
    }

}
