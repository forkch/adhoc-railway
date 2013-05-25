package ch.fork.AdHocRailway.controllers.impl.brain;

public class BrainException extends RuntimeException {

	public BrainException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BrainException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
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
