package ch.fork.AdHocRailway.domain.turnouts;

import com.google.gson.annotations.SerializedName;

public enum TurnoutType {

    @SerializedName("DEFAULT_LEFT")
    DEFAULT_LEFT("Default Left"),
    @SerializedName("DEFAULT_RIGHT")
    DEFAULT_RIGHT("Default Right"),
    @SerializedName("DOUBLECROSS")
    DOUBLECROSS("Doublecross"),
    @SerializedName("TRHEEWAY")
    THREEWAY("Threeway"),
    @SerializedName("CUTTER")
    CUTTER("Cutter");

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
