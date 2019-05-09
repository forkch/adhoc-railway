package de.dermoba.srcp.model.locomotives;

public class MfxLocomotive extends SRCPLocomotive {

    public final static int MAX_MFX_LOCOMOTIVE_ADDRESS = 511;
    public final static int FUNCTION_COUNT = 16;

    public static final String PROTOCOL = "X";
    private long mfxUid;

    public MfxLocomotive(final int bus, final int address, final long mfxUid) {
        this(bus, address, mfxUid, 127);
    }

    public MfxLocomotive(final int bus, final int address, final long mfxUid, final int drivingSteps) {
        super(bus, address);
        this.mfxUid = mfxUid;
        this.protocol = PROTOCOL;
        this.functionCount = FUNCTION_COUNT;
        this.functions = new boolean[FUNCTION_COUNT];
        this.drivingSteps = drivingSteps;
        params = new String[4];
        params[0] = "" + 1;
        params[1] = "" + drivingSteps;
        params[2] = "" + 16;
        params[3] = "" + mfxUid;
    }

    @Override
    public boolean checkAddress() {
        return !(address < 0 || address > MAX_MFX_LOCOMOTIVE_ADDRESS);
    }
}
