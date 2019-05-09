package de.dermoba.srcp.model.locomotives;

import de.dermoba.srcp.devices.GL;

public class DoubleMMDigitalLocomotive extends MMLocomotive {

    public final static int DRIVING_STEPS = 14;
    public final static int FUNCTION_COUNT1 = 5;
    public final static int FUNCTION_COUNT2 = 5;
    private final int address2;
    protected String[] params2;
    private GL gl2;

    public DoubleMMDigitalLocomotive(final int bus, final int address, int address2) {
        this(bus, address, address2, DRIVING_STEPS);
    }

    public DoubleMMDigitalLocomotive(final int bus, final int address, int address2, final int drivingSteps) {
        super(bus, address);
        this.address2 = address2;

        protocol = "M";
        params[0] = "2";
        params[1] = "" + drivingSteps;
        params[2] = "" + FUNCTION_COUNT1;

        params2 = new String[3];
        params2[0] = "2";
        params2[1] = "" + drivingSteps;
        params2[2] = "" + FUNCTION_COUNT2;
        functionCount = FUNCTION_COUNT1 + FUNCTION_COUNT2;
        functions = new boolean[FUNCTION_COUNT1 + FUNCTION_COUNT2];
        this.drivingSteps = drivingSteps;
    }

    @Override
    public boolean checkAddress() {
        final boolean address1 = super.checkAddress();
        return address1
                && !(address2 < 0 || address2 > MMLocomotive.MAX_MM_LOCOMOTIVE_ADDRESS);
    }

    public int getAddress2() {
        return this.address2;
    }

    public GL getGL2() {
        return this.gl2;
    }

    public void setGL2(final GL gl2) {
        this.gl2 = gl2;
    }

    public String[] getParams2() {
        return this.params2;
    }

}
