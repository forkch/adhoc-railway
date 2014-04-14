package ch.fork.AdHocRailway.manager;

import ch.fork.AdHocRailway.AdHocRailwayException;

public class ManagerException extends AdHocRailwayException {

    public ManagerException() {
        super();
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
