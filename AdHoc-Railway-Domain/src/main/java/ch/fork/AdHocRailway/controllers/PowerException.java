package ch.fork.AdHocRailway.controllers;

public class PowerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3071725012759374362L;

	public PowerException() {
		super();
	}

	public PowerException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PowerException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PowerException(final String message) {
		super(message);
	}

	public PowerException(final Throwable cause) {
		super(cause);
	}

}
