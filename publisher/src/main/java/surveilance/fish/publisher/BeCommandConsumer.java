package surveilance.fish.publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import surveilance.fish.model.BeCommand;
import surveilance.fish.model.DataBrick;
import surveilance.fish.publisher.base.BaseConsumer;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.RsaDecrypter;

public class BeCommandConsumer extends BaseConsumer<BeCommand> {

    public static final String PROP_BE_COMMAND_PRODUCER_GET_DATA_DELAY = "be.command.producer.get.data.delay";
    public static final String PROP_BE_COMMAND_PRODUCER_URL = "be.command.producer.url";
    
    private ImageProducer imageProducer;

    public BeCommandConsumer(Map<String, String> properties, AesDecrypter aesDecrypter, RsaDecrypter rsaDecrypter, ImageProducer imageProducer, AuthCookieUpdater authCookieUpdater) {
        super(properties, aesDecrypter, rsaDecrypter, authCookieUpdater);
        this.imageProducer = imageProducer;
    }

    @Override
    protected String getRepeatTaskDelay(Map<String, String> properties) {
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
            BeCommand beCommand = decryptPayload(dataBrick, new TypeReference<BeCommand>() {});
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
                        leftOperation();
                        break;
                    case "right":
                        rightOperation();
                        break;
                    case "forward":
                        forwardOperation();
                        break;
                    default:
                        System.out.println("Unknown 'go' direction received: " + beCommand.getValue());
                }
                break;
            default: 
                System.out.println("Unknown command received: " + beCommand);
        }

    }

    private void leftOperation() throws IOException {
        turnOnGpio(24);
        waitForExecution(100);
        turnOffGpio(24);
        imageProducer.doWork();
    }

    private void rightOperation() throws IOException {
        turnOnGpio(23);
        waitForExecution(100);
        turnOffGpio(23);
        imageProducer.doWork();
    }

    private void forwardOperation() throws IOException {
        turnOnGpio(23);
        turnOnGpio(24);
        waitForExecution(100);
        turnOffGpio(24);
        waitForExecution(100);
        turnOffGpio(23);
        imageProducer.doWork();
    }

    private void turnOnGpio(int code) {
        writeValueToGpio(code, (byte)'1');
    }
    
    
    private void turnOffGpio(int code) {
        writeValueToGpio(code, (byte)'0');
    }

    private void writeValueToGpio(int code, byte value) {
        try(FileOutputStream fos = new FileOutputStream(new File("/sys/class/gpio/gpio" + code + "/value"))) {
            fos.write(value);
        } catch (IOException exc) {
            System.out.println("Could not write value [" + value + "] to gpio: " + code);
            exc.printStackTrace();
        }
    }

    private void waitForExecution(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            stopBothGpio();
            //nothing more to do
            e.printStackTrace();
        }
    }
    
    private void stopBothGpio() {
        turnOffGpio(23);
        turnOffGpio(24);
    }
    
}
