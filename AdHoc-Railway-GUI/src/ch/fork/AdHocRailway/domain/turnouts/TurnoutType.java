package ch.fork.AdHocRailway.domain.turnouts;

public enum TurnoutType {
	DEFAULT, DOUBLECROSS, THREEWAY, CUTTER, UNKNOWN;

	public static TurnoutType fromString(String string) {
		for (TurnoutType ts : values()) {
			if (ts.name().equalsIgnoreCase(string)) {
				return ts;
			}
		}
		return null;
	}
}
