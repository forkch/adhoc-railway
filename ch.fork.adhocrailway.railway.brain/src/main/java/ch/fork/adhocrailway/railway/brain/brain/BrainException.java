package ch.fork.adhocrailway.railway.brain.brain;

import ch.fork.adhocrailway.model.AdHocRailwayException;

public class BrainException extends AdHocRailwayException {

    public BrainException() {
        super();
    }

    public BrainException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BrainException(final String message) {
        super(message);
    }

    public BrainException(final Throwable cause) {
        super(cause);
    }

}
