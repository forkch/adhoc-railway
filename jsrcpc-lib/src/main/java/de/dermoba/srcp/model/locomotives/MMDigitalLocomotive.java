package de.dermoba.srcp.model.locomotives;

public class MMDigitalLocomotive extends MMLocomotive {

    public final static int DRIVING_STEPS = 14;
    public final static int FUNCTION_COUNT = 5;

    public MMDigitalLocomotive(final int bus, final int address) {
        this(bus, address, DRIVING_STEPS);

    }

    public MMDigitalLocomotive(final int bus, final int address, final int drivingSteps) {
        super(bus, address);

        protocol = "M";
        params[0] = "2";
        params[1] = "" + drivingSteps;
        params[2] = "" + FUNCTION_COUNT;
        functionCount = FUNCTION_COUNT;
        functions = new boolean[FUNCTION_COUNT];
        this.drivingSteps = drivingSteps;
    }

}
