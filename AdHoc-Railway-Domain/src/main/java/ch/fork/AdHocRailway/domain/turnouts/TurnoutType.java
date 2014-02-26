package ch.fork.AdHocRailway.domain.turnouts;

public enum TurnoutType {
    DEFAULT_LEFT("Default Left"), DEFAULT_RIGHT("Default Right"), DOUBLECROSS(
            "Doublecross"), THREEWAY("Threeway"), CUTTER("Cutter");

    private final String humanName;

    private TurnoutType(final String humanName) {
        this.humanName = humanName;
    }

    public static TurnoutType fromString(final String string) {
        for (final TurnoutType ts : values()) {
            if (ts.name().equalsIgnoreCase(string)) {
                return ts;
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
