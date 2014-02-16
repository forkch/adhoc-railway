package ch.fork.AdHocRailway.ui.bus.events;

public class InitProceededEvent {

	private final String message;

	public InitProceededEvent(final String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
