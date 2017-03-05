package ch.fork.AdHocRailway.model.locomotives;

public enum LocomotiveType {

    DELTA("delta", "Märklin Delta", 0, 14, 1),

    DIGITAL("digital", "Märklin Digital", 5, 14, 1),

    SIMULATED_MFX("simulated-mfx", "Simulated MFX (2x Märklin Digital)", 9, 14, 1),

    MFX("mfx", "Märklin mfx", 19, 127, 1),

    DCC("dcc", "DCC", 13, 27, 1);

    private final int functionCount;
    private final int drivingSteps;
    private final int stepping;
    private final String id;
    private final String humanName;

    LocomotiveType(final String id, final String humanName,
                   final int functionCount, final int drivingSteps, final int stepping) {
        this.id = id;
        this.humanName = humanName;
        this.functionCount = functionCount;
        this.drivingSteps = drivingSteps;
        this.stepping = stepping;

    }

    public static LocomotiveType fromString(final String string) {
        for (final LocomotiveType lt : values()) {
            if (lt.getId().equalsIgnoreCase(string)) {
                return lt;
            }
        }
        return null;
    }

    public int getFunctionCount() {
        return functionCount;
    }

    public int getDrivingSteps() {
        return drivingSteps;
    }

    public int getStepping() {
        return stepping;
    }

    public String getId() {
        return id;
    }

    public String getHumanName() {
        return humanName;
    }

    @Override
    public String toString() {
        return humanName;
    }

}
