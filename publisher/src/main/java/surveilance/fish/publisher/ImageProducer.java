package surveilance.fish.publisher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.type.TypeReference;

import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.publisher.base.BaseProducer;
import surveilance.fish.publisher.sensors.SensorData;
import surveilance.fish.security.AesEncrypter;
import surveilance.fish.security.AesUtil;
import surveilance.fish.security.RsaEncrypter;

public class ImageProducer extends BaseProducer<byte[]> {
    public static final String PROP_CAPTURE_IMAGE_SCRIPT = "capture.image.script";
    public static final String PROP_SEND_IMAGE_DELAY = "send.image.delay";
    public static final String PROP_CONSUMER_URL = "consumer.url";
    public static final String PROP_IMAGES_FOLDER_PATH = "images.folder.path";

    //TODO: same as surveilance.fish.business.UIServlet.BODY_ITEM_SEPARATOR, extract to commmon module
    public static final String BODY_ITEM_SEPARATOR = ";";
    
    private static final Encoder BASE64_ENCODER = Base64.getEncoder();
    
    private final Path pathToImagesFolder;
    private String captureImageScript;
    private final DataAccessor<SensorData> dataAccessor;

    public ImageProducer(Map<String, String> properties, RsaEncrypter rsaEncrypter, AesEncrypter aesEncrypter
            , AesUtil aesUtil, AuthCookieUpdater authCookieUpdater, DataAccessor<SensorData> dataAccessor) {
        super(properties, rsaEncrypter, aesEncrypter, aesUtil, authCookieUpdater);
        pathToImagesFolder = Paths.get(properties.get(PROP_IMAGES_FOLDER_PATH));
        captureImageScript = properties.get(PROP_CAPTURE_IMAGE_SCRIPT);
        this.dataAccessor = dataAccessor;

        System.out.println("Reading images from disk location: " + pathToImagesFolder);
    }

    @Override
    protected String getRepeatTaskDelay(Map<String, String> properties) {
        return properties.get(PROP_SEND_IMAGE_DELAY);
    }

    @Override
    protected String getDataConsumerUrl(Map<String, String> properties) {
        return properties.get(PROP_CONSUMER_URL);
    }

    @Override
    protected void doWork() throws IOException {
        captureImage();
        byte[] imageData = getNewestImageData();
        String tempAndHumidDataToDisplay = getTempAndHumidDataToDisplay();
        imageData = addArrays(imageData, (BODY_ITEM_SEPARATOR + tempAndHumidDataToDisplay).getBytes());
        if (imageData == null) {
            return;
        }
        processData(imageData);
    }
    
    private String getTempAndHumidDataToDisplay() throws IOException  {
        List<SensorData> sensorData = dataAccessor.getLastNoOfElems(25, new TypeReference<SensorData>() {});
        List<String> temperatures = sensorData.stream().map(sensorItem -> String.valueOf(sensorItem.getTemperature())).collect(Collectors.toList());
        //add one more item to have a line to draw
        if (temperatures.size() == 1) {
            temperatures.add(temperatures.get(0));
        }
        List<String> humidities = sensorData.stream().map(sensorItem -> String.valueOf(sensorItem.getHumidity())).collect(Collectors.toList());
        if (humidities.size() == 1) {
            humidities.add(humidities.get(0));
        }
        
        return String.join(",", temperatures) + BODY_ITEM_SEPARATOR + String.join(",", humidities);
    }
    
    //TODO: copied from surveilance.fish.security.AesEncrypter.addArrays, replace in both places with org.eclipse.jetty.util.ArrayUtil
    private byte[] addArrays(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        for (int i = 0 ; i < first.length; ++i) {
            result[i] = first[i];
        }
        for (int i = 0 ; i < second.length; ++i) {
            result[first.length + i] = second[i];
        }
        
        return result;
    }

    private void captureImage() {
        Process p;
        try {
            p = Runtime.getRuntime().exec(captureImageScript);
            p.waitFor(10, TimeUnit.SECONDS);
            System.out.println ("Capture image script returned: " + p.exitValue());
            p.destroy();
        } catch (InterruptedException | IOException e) {
            System.out.println("Could not capture image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @return base 64 encoded image data
     * @throws IOException 
     */
    private byte[] getNewestImageData() throws IOException {
        File newestFile = getNewestFile(pathToImagesFolder);
        if (newestFile == null) {
            System.out.println("No file found");
            return null;
        }
        System.out.println("Newest file found: " + newestFile);
        
        try {
            byte[] imgDateWithTemp = addTempDataToImage(newestFile);
            return BASE64_ENCODER.encode(imgDateWithTemp);
        } catch (IOException exc) {
            System.out.println("Could not add temperature data to image");
            exc.printStackTrace();
        }
        
        byte[] rawData = Files.readAllBytes(newestFile.toPath());
        
        return BASE64_ENCODER.encode(rawData);
    }

    private File getNewestFile(Path pathToFolder) throws IOException {
        File newestFile = null;
        try (Stream<Path> pathList = Files.list(pathToFolder)) {
            for (Iterator<Path> iterator = pathList.iterator(); iterator.hasNext();) {
                Path currentPath = iterator.next();
                if (Files.isRegularFile(currentPath)) {
                    File currentFile = currentPath.toFile();
                    if (newestFile == null) {
                        newestFile = currentFile;
                        continue;
                    }
                    if (currentFile.lastModified() > newestFile.lastModified()) {
                        newestFile = currentFile;
                    }
                }
            }
        }
        
        return newestFile;
    }

    private byte[] addTempDataToImage(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        Graphics graphics = image.getGraphics();
        
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 60, 15);
        
        graphics.setColor(Color.WHITE);
        graphics.setFont(graphics.getFont().deriveFont(10f));
        List<SensorData> sensorData = dataAccessor.getLastNoOfElems(1, new TypeReference<SensorData>() {});
        //27 *C, 57 H
        graphics.drawString(sensorData.get(0).getTemperature() + " *C, " + sensorData.get(0).getHumidity() + " H", 1, 10);
        
        graphics.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //check com.sun.imageio.plugins.jpeg.JPEG.names for valid names
        ImageIO.write(image, "jpeg", baos);
        
        return baos.toByteArray();
    }

}
