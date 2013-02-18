package ch.fork.AdHocRailway.domain.turnouts;

public enum TurnoutState {
	STRAIGHT, LEFT, RIGHT, UNDEF;

	public static TurnoutState fromString(String string) {
		for (TurnoutState ts : values()) {
			if (ts.name().equalsIgnoreCase(string)) {
				return ts;
			}
		}
		return null;
	}
}
