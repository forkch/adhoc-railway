package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import de.dermoba.srcp.model.locomotives.MMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;

/**
 * Created by fork on 4/13/14.
 */
public class DigitalLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
    @Override
    public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {

        MMDigitalLocomotive sLocomotive = new MMDigitalLocomotive();
        updateSRCPLocomotive(sLocomotive, locomotive);
        return sLocomotive;
    }
}
