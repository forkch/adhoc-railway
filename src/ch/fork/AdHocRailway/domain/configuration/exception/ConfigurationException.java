package ch.fork.AdHocRailway.domain.configuration.exception;

public class ConfigurationException extends Exception {
    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String msg, Exception parent) {
        super(msg, parent);
    }
}
