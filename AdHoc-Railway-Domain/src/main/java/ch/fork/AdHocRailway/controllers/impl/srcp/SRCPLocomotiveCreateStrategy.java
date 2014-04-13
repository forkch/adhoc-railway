package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;

/**
 * Created by fork on 4/13/14.
 */
public abstract class SRCPLocomotiveCreateStrategy {

    public abstract SRCPLocomotive createSRCPLocomotive(Locomotive locomotive);

    public SRCPLocomotive updateSRCPLocomotive(SRCPLocomotive srcpLocomotive, Locomotive locomotive) {
        srcpLocomotive.setBus(locomotive.getBus());
        srcpLocomotive.setAddress(locomotive.getAddress1());
        return srcpLocomotive;
    }
}
