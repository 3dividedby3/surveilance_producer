package surveilance.fish.server.sensor;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.persistence.simple.FileSaver;
import surveilance.fish.server.SensorFileSaver;

public class TempHumServlet extends HttpServlet {

    private static final long serialVersionUID = 1500136876413405519L;

    public static final String REQUEST_PARAM_TEMPERATURE = "temperature";
    public static final String REQUEST_PARAM_HUMIDITY = "humidity";

    private final SensorFileSaver sensorFileSaver;
    
    public TempHumServlet(SensorFileSaver sensorFileSaver) {
        this.sensorFileSaver = sensorFileSaver;
    }
    
    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) {
        int temperature = Integer.valueOf(request.getParameter(REQUEST_PARAM_TEMPERATURE));
        int humidity = Integer.valueOf(request.getParameter(REQUEST_PARAM_HUMIDITY));
        System.out.println("[TempHumServlet][" + sensorFileSaver.getId()
            + "] Received data - temperature: [" + temperature + "], humidity: [" + humidity + "]");
        
        long dataReadTime = System.currentTimeMillis();
        SensorData sensorData = new SensorData(dataReadTime, temperature, humidity);
        
        sensorFileSaver.saveData(sensorData);
    }
}
