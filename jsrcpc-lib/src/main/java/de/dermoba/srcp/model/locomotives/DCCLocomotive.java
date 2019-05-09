package de.dermoba.srcp.model.locomotives;

public class DCCLocomotive extends MMLocomotive {

    public final static int MAX_DCC_LOCOMOTIVE_ADDRESS = 10239;
    public final static int DRIVING_STEPS = 127;
    public final static int FUNCTION_COUNT = 13;

    public DCCLocomotive(final int bus, final int address) {
        this(bus, address, DRIVING_STEPS);
    }

    public DCCLocomotive(final int bus, final int address, final int drivingSteps) {
        super(bus, address);
        this.functionCount = FUNCTION_COUNT;
        this.functions = new boolean[FUNCTION_COUNT];
        this.drivingSteps = drivingSteps;
        this.protocol = "N";
        params[0] = "1";
        params[1] = "" + drivingSteps;
        params[2] = "" + FUNCTION_COUNT;
    }

    @Override
    public boolean checkAddress() {
        return !(address < 0 || address > MAX_DCC_LOCOMOTIVE_ADDRESS);
    }
}
