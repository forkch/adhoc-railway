package ch.fork.AdHocRailway.controllers.impl.brain;

public class BrainException extends RuntimeException {

	private static final long serialVersionUID = -7774330643904156967L;

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
