package de.dermoba.srcp.model.locomotives;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.Response;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;
import de.dermoba.srcp.model.SRCPAddress;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import org.apache.log4j.Logger;

public class DefaultLocomotiveStrategy extends LocomotiveStrategy {

    private static final Logger LOGGER = org.apache.log4j.Logger.getLogger(DefaultLocomotiveStrategy.class);

    @Override
    public void setSpeed(final SRCPLocomotive locomotive, SRCPLocomotiveDirection direction, final int speed,
                         boolean[] functions) throws SRCPException {
        if (functions == null) {
            functions = locomotive.getFunctions();
        }
        locomotive.getGL().setAddress(locomotive.getAddress());
        final String resp = setSpeedOnGl(locomotive.getGL(), locomotive, direction, speed,
                functions);

        if (resp == null || resp.equals("")) {
            return;
        }
        final Response r = new Response(resp);
        locomotive.setLastCommandAcknowledge(r.getTimestamp());

        locomotive.setCurrentSpeed(speed);
        locomotive.setFunctions(functions);
    }

    @Override
    public void initLocomotive(final SRCPLocomotive locomotive,
                               final SRCPSession session, final SRCPLockControl lockControl)
            throws SRCPLocomotiveException {


        if (!locomotive.isInitialized()) {
            initLocomotive(locomotive);
        }
    }

    private void initLocomotive(final SRCPLocomotive locomotive)
            throws SRCPLocomotiveException {
        try {
            final String[] params = locomotive.getParams();
            locomotive.getGL().init(locomotive.getAddress(),
                    locomotive.getProtocol(), params);
            locomotive.setInitialized(true);
        } catch (final SRCPException x) {
            LOGGER.warn("could not init loco", x);
            //throw new SRCPLocomotiveException(Constants.ERR_INIT_FAILED, x);
        }
    }

    @Override
    public boolean[] getEmergencyStopFunctions(final SRCPLocomotive locomotive,
                                               final int emergencyStopFunction) {
        if (locomotive instanceof MMDigitalLocomotive) {
            final boolean[] functions = new boolean[]{false, false, false,
                    false, false};
            if (emergencyStopFunction != -1
                    && emergencyStopFunction < functions.length) {
                functions[emergencyStopFunction] = true;
            }
            return functions;
        } else {
            return new boolean[]{true};
        }
    }

    @Override
    public void mergeFunctions(SRCPLocomotive locomotive, int address, boolean[] functions) {
        locomotive.setFunctions(functions);
    }

    @Override
    public void terminate(SRCPLocomotive locomotive) throws SRCPException {

        if (locomotive.isInitialized()) {
            locomotive.getGL().term();
            locomotive.setInitialized(false);
        }

    }
}
