package ch.fork.AdHocRailway.domain.exception;

public class ControlException extends Exception {

    public ControlException() {
        super();
    }

    public ControlException(String message) {
        super(message);
    }

    public ControlException(String message, Throwable cause) {
        super(message, cause);
    }

    public ControlException(Throwable cause) {
        super(cause);
    }

}
