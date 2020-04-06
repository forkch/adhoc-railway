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
        params = new String[21];    /* 2020-04-03 m2  params array-size changed from 4 to 21 */
        params[0] = "" + 1;
        params[1] = "" + drivingSteps;
        params[2] = "" + 16;
        params[3] = "" + mfxUid;
        params[4] = "\"loki\"";  /* Lok-Name */
        params[5] = "" + 0; /* F1 */
        params[6] = "" + 0; /* F2 */
        params[7] = "" + 0;
        params[8] = "" + 0;
        params[9] = "" + 0;
        params[10] = "" + 0;
        params[11] = "" + 0;
        params[12] = "" + 0;
        params[13] = "" + 0;
        params[14] = "" + 0;
        params[15] = "" + 0;
        params[16] = "" + 0;
        params[17] = "" + 0;
        params[18] = "" + 0;
        params[19] = "" + 0;    /* F15 */
        params[20] = "" + 0;    /* F16 */
    }

    @Override
    public boolean checkAddress() {
        return !(address < 0 || address > MAX_MFX_LOCOMOTIVE_ADDRESS);
    }
}
