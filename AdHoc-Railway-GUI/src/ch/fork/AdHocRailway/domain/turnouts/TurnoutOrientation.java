package ch.fork.AdHocRailway.domain.turnouts;

public enum TurnoutOrientation {
	NORTH, SOUTH, WEST, EAST;

	public static TurnoutOrientation fromString(String string) {
		for (TurnoutOrientation to : values()) {
			if (to.name().equalsIgnoreCase(string)) {
				return to;
			}
		}
		return null;
	}
};
