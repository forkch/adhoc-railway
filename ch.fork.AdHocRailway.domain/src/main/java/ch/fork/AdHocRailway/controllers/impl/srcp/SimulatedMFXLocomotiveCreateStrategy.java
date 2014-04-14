package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import de.dermoba.srcp.model.locomotives.DoubleMMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;

/**
 * Created by fork on 4/13/14.
 */
public class SimulatedMFXLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
    @Override
    public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {

        final DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = new DoubleMMDigitalLocomotive();
        updateSRCPLocomotive(doubleMMDigitalLocomotive, locomotive);
        return doubleMMDigitalLocomotive;
    }

    public SRCPLocomotive updateSRCPLocomotive(DoubleMMDigitalLocomotive doubleMMDigitalLocomotive, Locomotive locomotive) {
        doubleMMDigitalLocomotive.setBus(locomotive.getBus());
        doubleMMDigitalLocomotive.setAddress(locomotive.getAddress1());
        doubleMMDigitalLocomotive.setAddress2(locomotive.getAddress2());
        return doubleMMDigitalLocomotive;
    }
}
