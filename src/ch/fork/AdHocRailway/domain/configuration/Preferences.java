
package ch.fork.AdHocRailway.domain.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preferences implements PreferencesKeys{
    private Map<String, String> preferences;
    private List<String>        hostnames;
    private String              hostname                = "titan";
    private int                 portnumber              = 12345;
    private int                 defaultActivationTime   = 50;
    private int                 defaultRoutingDelay     = 250;
    private int                 locomotiveControlNumber = 5;
    private String              keyBoardLayout          = "Swiss German";
    private static Preferences  instance                = null;

    private Preferences() {
        preferences = new HashMap<String, String>();
        hostnames = new ArrayList<String>();
        hostnames.add("localhost");
        setStringValue(HOSTNAME, "localhost");
        setIntValue(PORT, 12345);
        setIntValue(ACTIVATION_TIME, 50);
        setIntValue(ROUTING_DELAY, 250);
        setIntValue(LOCK_DURATION, 0);
        setIntValue(LOCOMOTIVE_CONTROLES, 5);
        setStringValue(KEYBOARD_LAYOUT, "Swiss German");
        setStringValue(INTERFACE_6051, "Y");
        setIntValue(SWITCH_CONTROLES, 5);
        setBooleanValue(LOGGING, false);
        setBooleanValue(FULLSCREEN, false);
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

    public void setBooleanValue(String key, boolean value) {
        preferences.put(key, Boolean.toString(value));
    }

    public boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(preferences.get(key));
    }

    public List<String> getHostnames() {
        return hostnames;
    }

    public void setHostnames(List<String> hostnames) {
        this.hostnames = hostnames;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }
}
