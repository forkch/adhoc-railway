package ch.fork.AdHocRailway.controllers;

public enum RailwayDevice {
    SRCP, ADHOC_BRAIN, NULL_DEVICE;

    private RailwayDevice() {

    }

    public static RailwayDevice fromString(final String railwayDevice) {
        for (final RailwayDevice device : values()) {
            if (device.name().equalsIgnoreCase(railwayDevice)) {
                return device;
            }
        }
        return NULL_DEVICE;
    }
}
