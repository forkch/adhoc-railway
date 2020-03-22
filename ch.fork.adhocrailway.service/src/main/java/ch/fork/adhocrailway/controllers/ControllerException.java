package ch.fork.adhocrailway.controllers;

import ch.fork.adhocrailway.model.AdHocRailwayException;

/**
 * Created by fork on 3/23/14.
 */
public class ControllerException extends AdHocRailwayException {

    public ControllerException() {
    }

    public ControllerException(final String message) {
        super(message);
    }

    public ControllerException(final Throwable cause) {
        super(cause);
    }

    public ControllerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
