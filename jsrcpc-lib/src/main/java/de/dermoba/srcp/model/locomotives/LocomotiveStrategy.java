package de.dermoba.srcp.model.locomotives;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;
import de.dermoba.srcp.model.locking.SRCPLockControl;

public abstract class LocomotiveStrategy {

    public abstract void setSpeed(final SRCPLocomotive locomotive,
                                  SRCPLocomotiveDirection direction, final int speed, boolean[] functions) throws SRCPException;

    public abstract void initLocomotive(final SRCPLocomotive locomotive,
                                        final SRCPSession session, final SRCPLockControl lockControl)
            throws SRCPLocomotiveException;

    protected String setSpeedOnGl(final GL gl, final SRCPLocomotive locomotive,
                                  SRCPLocomotiveDirection direction, final int speed, final boolean[] functions) throws SRCPException {
        final int drivingSteps = locomotive.getDrivingSteps();
        if (speed < 0 || speed > drivingSteps) {
            return "";
        }
        String resp = gl.set(direction, speed, drivingSteps,
                functions);


        return resp;
    }

    public abstract boolean[] getEmergencyStopFunctions(
            final SRCPLocomotive locomotive, int emergencyStopFunction);

    public abstract void mergeFunctions(SRCPLocomotive locomotive, int address, boolean[] functions);

    public abstract void terminate(SRCPLocomotive locomotive) throws SRCPException;
}
