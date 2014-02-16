package ch.fork.AdHocRailway;

public class AdHocRailwayException extends RuntimeException {

	private static final long serialVersionUID = -4952163296797146540L;

	public AdHocRailwayException() {
		super();
	}

	public AdHocRailwayException(final String message) {
		super(message);
	}

	public AdHocRailwayException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AdHocRailwayException(final Throwable cause) {
		super(cause);
	}

}
