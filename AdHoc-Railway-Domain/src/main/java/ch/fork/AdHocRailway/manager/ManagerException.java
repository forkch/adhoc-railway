package ch.fork.AdHocRailway.manager;

public class ManagerException extends RuntimeException {

    public ManagerException() {
        super();
    }

    public ManagerException(final String message, final Throwable cause,
                            final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ManagerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ManagerException(final String message) {
        super(message);
    }

    public ManagerException(final Throwable cause) {
        super(cause);
    }

}
