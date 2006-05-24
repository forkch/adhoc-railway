package ch.fork.RailControl.domain.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preferences {

    private Map<String, String> preferences;

    private List<String> hostnames;

    private String hostname = "titan";

    private int portnumber = 12345;

    private int defaultActivationTime = 50;

    private int defaultRoutingDelay = 250;

    private int locomotiveControlNumber = 5;

    private String keyBoardLayout = "Swiss German";

    private static Preferences instance = null;

    private Preferences() {
        preferences = new HashMap<String, String>();
        hostnames = new ArrayList<String>();
        hostnames.add("localhost");
        hostnames.add("titan");
        hostnames.add("192.168.1.38");
        hostnames.add("192.168.1.100");
        setStringValue("Hostname", "192.168.1.100");
        setIntValue("Portnumber", 12345);
        setIntValue("DefaultActivationTime", 50);
        setIntValue("DefaultRoutingDelay", 250);
        setIntValue("LocomotiveControlesAmount", 5);
        setStringValue("KeyBoardLayout", "Swiss German");
    }

    public static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
            return instance;
        } else {
            return instance;
        }
    }

    public void setStringValue(String key, String value) {
        preferences.put(key, value);
    }

    public String getStringValue(String key) {
        return preferences.get(key);
    }

    public void setIntValue(String key, int value) {
        preferences.put(key, Integer.toString(value));
    }

    public int getIntValue(String key) {
        return Integer.parseInt(preferences.get(key));
    }

    public List<String> getHostnames() {
        return hostnames;
    }

    public void setHostnames(List<String> hostnames) {
        this.hostnames = hostnames;
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<GuiConfiguration>\n");

        for (String key : preferences.keySet()) {
            sb.append("<GuiConfigParameter ");
            sb.append(" name=\""
                + key + "\"");
            sb.append(" value=\""
                + preferences.get(key) + "\"");
            sb.append("/>\n");
        }
        sb.append("</GuiConfiguration>\n");
        return sb.toString();
    }
}
