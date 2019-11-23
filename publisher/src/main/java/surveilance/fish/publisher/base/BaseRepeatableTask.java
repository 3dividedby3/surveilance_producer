package surveilance.fish.publisher.base;

import static surveilance.fish.publisher.App.SECOND;

import java.io.IOException;
import java.util.Map;

public abstract class BaseRepeatableTask {
    
    private final int repeatTaskDelay;
    
    protected BaseRepeatableTask(Map<String, String> properties) {
        repeatTaskDelay = SECOND * Integer.valueOf(getRepeatTaskDelay(properties));
    }
    
    public final void start() throws InterruptedException {
        new Thread(() -> repeatDoWork()).start();
        System.out.println("Started repeatable task: " + getClass().getName());
    }
    
    protected abstract String getRepeatTaskDelay(Map<String, String> properties);
    protected abstract void doWork() throws IOException;

    private void repeatDoWork() {
        while(true) {
            try {
                doWork();
            } catch(Throwable t) {
                System.out.println("Error while repeating work: " + t.getMessage());
                t.printStackTrace();
            }
            
            try {
                Thread.sleep(repeatTaskDelay);
            } catch (InterruptedException e) {
                //just ignore it...
                e.printStackTrace();
            }
        }
    }
}
