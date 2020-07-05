package surveilance.fish.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import surveilance.fish.common.exc.SurveilanecException;
import surveilance.fish.server.sensor.TempHumServlet;

public class SupportServer {
    
    public static final String PROP_SUPPORT_SERVER_PORT = "support.server.port";
    public static final String PROP_SUPPORT_SERVER_TEMPHUM_LOCATION = "support.server.temphum.location";
    
    private final int supportServerPort;
    private final SensorFileSaver[] fileSavers;

    public SupportServer(int supportServerPort, SensorFileSaver... fileSavers) {
        this.supportServerPort = supportServerPort;
        this.fileSavers = fileSavers;
    }
    
    public void start() throws SurveilanecException {
        System.out.println("[support server] Starting on port: " + supportServerPort);
        Server server = new Server(supportServerPort);
        
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        for(SensorFileSaver sensorFileSaver : fileSavers) {
            ServletHolder holderTempHumServlet = new ServletHolder(new TempHumServlet(sensorFileSaver));
            handler.addServletWithMapping(holderTempHumServlet, "/sensor/temphum/" + sensorFileSaver.getId());
        }

        try {
            server.start();
        } catch (Exception e) {
            throw new SurveilanecException("[support server] Could not start", e);
        }
        System.out.println("[support server] Started");
    }
}