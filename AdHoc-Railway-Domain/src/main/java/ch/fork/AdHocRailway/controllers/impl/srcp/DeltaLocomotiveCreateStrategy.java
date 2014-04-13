package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import de.dermoba.srcp.model.locomotives.MMDeltaLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;

/**
 * Created by fork on 4/13/14.
 */
public class DeltaLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
    @Override
    public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {

        MMDeltaLocomotive sLocomotive = new MMDeltaLocomotive();

        updateSRCPLocomotive(sLocomotive, locomotive);
        return sLocomotive;
    }

}
