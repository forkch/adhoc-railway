package ch.fork.RailControl.domain.locomotives;

import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;

public class DeltaLocomotive extends Locomotive {

    private static final int DRIVING_STEPS = 14;

    private static final int STEPPING = 2;

    private static final int FUNCTIONCOUNT = 1;

    public DeltaLocomotive(String name, int bus, int address, String desc) {
        this(null, name, bus, address, desc);
    }

    public DeltaLocomotive(SRCPSession session, String name, int bus,
        int address, String desc) {
        super(session, name, bus, address, DRIVING_STEPS, desc,
            FUNCTIONCOUNT);
    }

    protected void increaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed()
            + STEPPING);
    }

    protected void decreaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed()
            - STEPPING);
    }

}
