package surveilance.fish.publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import surveilance.fish.model.BeCommand;
import surveilance.fish.model.DataBrick;
import surveilance.fish.publisher.base.BaseConsumer;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.RsaDecrypter;

public class BeCommandConsumer extends BaseConsumer<BeCommand> {

    public static final String PROP_BE_COMMAND_PRODUCER_GET_DATA_DELAY = "be.command.producer.get.data.delay";
    public static final String PROP_BE_COMMAND_PRODUCER_URL = "be.command.producer.url";
    
    private ImageProducer imageProducer;

    public BeCommandConsumer(Map<String, String> properties, AesDecrypter aesDecrypter, RsaDecrypter rsaDecrypter, ImageProducer imageProducer) {
        super(properties, aesDecrypter, rsaDecrypter);
        this.imageProducer = imageProducer;
    }

    public void start() throws InterruptedException {
        Thread t = new Thread(() -> repeatDoWork());
        t.start();
        System.out.println("Started be command consumer!");
    }

    @Override
    protected String getDataProducerGetDelay(Map<String, String> properties) {
        return properties.get(PROP_BE_COMMAND_PRODUCER_GET_DATA_DELAY);
    }

    @Override
    protected String getDataProducerUrl(Map<String, String> properties) {
        return properties.get(PROP_BE_COMMAND_PRODUCER_URL);
    }

    @Override
    protected void doWork() {
        DataBrick<BeCommand> dataBrick = retrieveData();
        if (EMPTY_DATA_BRICK == dataBrick) {
            return;
        }

        try {
            BeCommand beCommand = decryptPayload(dataBrick);
            System.out.println("Received be command: " + beCommand);
            executeCommand(beCommand);
        } catch (IOException e) {
            System.out.println("Could not execute command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeCommand(BeCommand beCommand) throws IOException {
        switch (beCommand.getKey()) {
            case "go":
                switch (beCommand.getValue()) {
                    case "left":
                        turnOnGpio(24);
                        waitForExecution(50);
                        turnOffGpio(24);
                        imageProducer.doWork();
                        break;
                    case "right":
                        turnOnGpio(23);
                        waitForExecution(150);
                        turnOffGpio(23);
                        imageProducer.doWork();
                        break;
                    case "forward":
                        turnOnGpio(23);
                        turnOnGpio(24);
                        waitForExecution(100);
                        turnOffGpio(24);
                        waitForExecution(150);
                        turnOffGpio(23);
                        imageProducer.doWork();
                        break;
                    default:
                        System.out.println("Unknown 'go' direction received: " + beCommand.getValue());
                }
                break;
            default: 
                System.out.println("Unknown command received: " + beCommand);
        }

    }

    private void turnOnGpio(int code) throws IOException, FileNotFoundException {
        writeValueToGpio(code, (byte)'1');
    }
    
    
    private void turnOffGpio(int code) throws IOException, FileNotFoundException {
        writeValueToGpio(code, (byte)'0');
    }

    private void writeValueToGpio(int code, byte value) throws IOException, FileNotFoundException {
        try(FileOutputStream fos = new FileOutputStream(new File("/sys/class/gpio/gpio" + code + "/value"))) {
            fos.write(value);
        }
    }

    private void waitForExecution(long delay) throws IOException {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            stopBothGpio();
            //nothing more to do
            e.printStackTrace();
        }
    }
    
    private void stopBothGpio() throws IOException, FileNotFoundException {
        turnOffGpio(23);
        turnOffGpio(24);
    }
    
}
