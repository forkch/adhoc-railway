package ch.fork.AdHocRailway.domain.turnouts;

import com.google.gson.annotations.SerializedName;

public enum TurnoutOrientation {
    @SerializedName("NORTH")
    NORTH("North"),
    @SerializedName("SOUTH")
    SOUTH("South"),
    @SerializedName("WEST")
    WEST("West"),
    @SerializedName("EAST")
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
