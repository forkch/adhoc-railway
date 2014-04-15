package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.model.AdHocRailwayException;

/**
 * Created by fork on 4/15/14.
 */
public class AdHocServiceException extends AdHocRailwayException {

    public AdHocServiceException() {
        super();
    }

    public AdHocServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AdHocServiceException(final String message) {
        super(message);
    }

    public AdHocServiceException(final Throwable cause) {
        super(cause);
    }
}
