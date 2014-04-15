package ch.fork.AdHocRailway.model.turnouts;

public enum TurnoutOrientation {
    NORTH("North"),
    SOUTH("South"),
    WEST("West"),
    EAST("East");

    private final String humanName;

    private TurnoutOrientation(final String humanName) {
        this.humanName = humanName;
    }

    public static TurnoutOrientation fromString(final String string) {
        for (final TurnoutOrientation to : values()) {
            if (to.name().equalsIgnoreCase(string)) {
                return to;
            }
        }
        return null;
    }

    public String getHumanName() {
        return humanName;
    }

    @Override
    public String toString() {
        return humanName;
    }
}
