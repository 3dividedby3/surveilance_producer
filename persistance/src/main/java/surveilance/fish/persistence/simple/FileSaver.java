package surveilance.fish.persistence.simple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import surveilance.fish.persistence.api.BaseData;
import surveilance.fish.persistence.api.DataAccessException;
import surveilance.fish.persistence.api.DataAccessor;

public class FileSaver<T extends BaseData> implements DataAccessor<T> {

    public static final String INDEX_CONTENT_SEPARATOR = " - ";
    private static final DateTimeFormatter FORMAT_DD_MM_YYYY_HH_MM_SS = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss").withZone(ZoneId.of("UTC"));
    private static final String INDEX_FILE_NAME = "index.txt";

    private String saveLocationPath;

    private ObjectWriter objectWriter;
    private ObjectMapper objectMapper;

    public FileSaver(String path) {
        saveLocationPath = path;
        objectMapper = new ObjectMapper();
        objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
    }

    @Override
    public synchronized List<T> getLastNoOfElems(int noOfElems, TypeReference<T> typeReference) throws IOException {
        List<String> itemsSorted = getItemsSorted();
        noOfElems = noOfElems > itemsSorted.size() ? itemsSorted.size() : noOfElems;
        List<String> onlyNeededItems = itemsSorted.subList(itemsSorted.size() - noOfElems, itemsSorted.size());

        List<T> itemsParsed = onlyNeededItems.stream().filter(item -> !"".equals(item)).map(item -> {
                    Path itemPath = extractPath(item);
                    try {
                        return readValueFromPath(typeReference, itemPath);
                    } catch (Exception e) {
                        throw new DataAccessException(e);
                    }
                }).collect(Collectors.toList());

        return itemsParsed;
    }

    @Override
    public synchronized void saveData(T data) throws IOException {
        String dateCreated = FORMAT_DD_MM_YYYY_HH_MM_SS.format(Instant.ofEpochMilli(data.getTimestampCreated()));
        String fileName = data.getTimestampCreated() + "_" + dateCreated + ".json";
        byte[] dataContent = objectWriter.writeValueAsString(data).getBytes();
        Path fullLocation = createDirsAndSaveData(data.getTimestampCreated(), fileName, dataContent);
        appendFileNameToIndex(fullLocation, data.getTimestampCreated());
    }

    private T readValueFromPath(TypeReference<T> typeReference, Path path) throws IOException {
        String content = new String(Files.readAllBytes(path));
        
        return objectMapper.readValue(content, typeReference);
    }

    private void appendFileNameToIndex(Path fullLocation, long timestampCreated) throws IOException {
        Files.write(Paths.get(saveLocationPath, INDEX_FILE_NAME)
                , (System.lineSeparator() + timestampCreated + INDEX_CONTENT_SEPARATOR + fullLocation.toString()).getBytes()
                , StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private Path createDirsAndSaveData(Long timestampCreated, String fileName, byte[] content) throws IOException {
        ZonedDateTime instantCreate = Instant.ofEpochMilli(timestampCreated).atZone(ZoneId.of("UTC"));
        String year = String.valueOf(instantCreate.getYear());
        String month = instantCreate.getMonth().getDisplayName(TextStyle.FULL, Locale.UK);
        String day = String.valueOf(instantCreate.getDayOfMonth());
        Path yearMonthDayPath = Paths.get(saveLocationPath, year, month, day);
        Files.createDirectories(yearMonthDayPath);
        Path fullLocation = yearMonthDayPath.resolve(fileName);
        Files.write(fullLocation, content);
        
        return fullLocation;
    }

    private List<String> getItemsSorted() throws IOException {
        Stream<String> sortedLinesStream = Files.lines(Paths.get(saveLocationPath, INDEX_FILE_NAME)).sorted();
        List<String> lines = sortedLinesStream.collect(Collectors.toList());
        sortedLinesStream.close();
        
        return lines;
    }

    private Path extractPath(String lastItem) {
        Path newestFilePath = Paths.get(lastItem.substring(lastItem.indexOf(INDEX_CONTENT_SEPARATOR) + INDEX_CONTENT_SEPARATOR.length()));
        
        return newestFilePath;
    }
}
