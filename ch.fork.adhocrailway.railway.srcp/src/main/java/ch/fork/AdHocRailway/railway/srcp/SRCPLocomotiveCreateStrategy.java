package ch.fork.AdHocRailway.railway.srcp;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import de.dermoba.srcp.model.locomotives.*;

abstract class SRCPLocomotiveCreateStrategy {

    public abstract SRCPLocomotive createSRCPLocomotive(Locomotive locomotive);

    SRCPLocomotive updateSRCPLocomotive(SRCPLocomotive srcpLocomotive, Locomotive locomotive) {
        srcpLocomotive.setBus(locomotive.getBus());
        srcpLocomotive.setAddress(locomotive.getAddress1());
        return srcpLocomotive;
    }

    static class DCCLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
        @Override
        public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {
            return new DCCLocomotive(locomotive.getBus(), locomotive.getAddress1(), LocomotiveType.DCC.getDrivingSteps());
        }
    }

    static class DeltaLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
        @Override
        public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {
            return new MMDeltaLocomotive(locomotive.getBus(), locomotive.getAddress1(), LocomotiveType.DELTA.getDrivingSteps());
        }

    }

    static class DigitalLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
        @Override
        public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {
            return new MMDigitalLocomotive(locomotive.getBus(), locomotive.getAddress1(), LocomotiveType.DIGITAL.getDrivingSteps());
        }
    }

    static class MFXLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
        @Override
        public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {
            return new MfxLocomotive(locomotive.getBus(), locomotive.getAddress1(), locomotive.getMfxUUID(), LocomotiveType.MFX.getDrivingSteps());
        }
    }

    static class SimulatedMFXLocomotiveCreateStrategy extends SRCPLocomotiveCreateStrategy {
        @Override
        public SRCPLocomotive createSRCPLocomotive(Locomotive locomotive) {

            return new DoubleMMDigitalLocomotive(locomotive.getBus(), locomotive.getAddress1(), locomotive.getAddress2(), LocomotiveType.SIMULATED_MFX.getDrivingSteps());
        }
    }
}
