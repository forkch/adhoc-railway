package ch.fork.AdHocRailway.domain.switches.exception;

public class SwitchException extends Exception {

    public SwitchException(String msg) {
        super(msg);
    }

    public SwitchException(String msg, Exception parent) {
        super(msg, parent);
    }
}
