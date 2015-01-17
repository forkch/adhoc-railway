package ch.fork.AdHocRailway.railway.srcp;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import de.dermoba.srcp.model.locomotives.DoubleMMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;

/**
 * Created by fork on 4/13/14.
 */
public class SimulatedMFXLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
    @Override
    public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {

        final DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = new DoubleMMDigitalLocomotive(locomotive.getBus(), locomotive.getAddress1(), locomotive.getAddress2());
        return doubleMMDigitalLocomotive;
    }
}
