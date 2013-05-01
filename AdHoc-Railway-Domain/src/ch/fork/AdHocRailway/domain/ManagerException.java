package ch.fork.AdHocRailway.domain;

public class ManagerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

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
