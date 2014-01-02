package ch.fork.AdHocRailway.domain.turnouts;

public enum TurnoutState {
    STRAIGHT("Straight"), LEFT("Left"), RIGHT("Right"), UNDEF("N/A");

    private final String humanName;

    private TurnoutState(final String humanName) {
        this.humanName = humanName;
    }

    public static TurnoutState fromString(final String string) {
        for (final TurnoutState ts : values()) {
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
