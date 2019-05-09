package de.dermoba.srcp.model.locomotives;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.Response;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;
import de.dermoba.srcp.model.Constants;
import de.dermoba.srcp.model.SRCPAddress;
import de.dermoba.srcp.model.locking.SRCPLockControl;

import java.util.Arrays;

public class SimulatedMFXLocomotiveStrategy extends LocomotiveStrategy {

    @Override
    public void setSpeed(final SRCPLocomotive locomotive, SRCPLocomotiveDirection direction, final int speed,
                         boolean[] functions) throws SRCPException {


        final DoubleMMDigitalLocomotive doubleMM = (DoubleMMDigitalLocomotive) locomotive;
        if (functions == null) {
            functions = locomotive.getFunctions();
        }

        final boolean[] functions1 = Arrays.copyOfRange(functions, 0, 5);
        final boolean[] functions2 = Arrays.copyOfRange(functions, 5, 10);

        doubleMM.getGL().setAddress(doubleMM.getAddress());
        String resp = setSpeedOnGl(doubleMM.getGL(), doubleMM, doubleMM.direction, speed,
                functions1);

        if (resp == null || resp.equals("")) {
            return;
        }
        Response r = new Response(resp);
        locomotive.setLastCommandAcknowledge(r.getTimestamp());

        doubleMM.setCurrentSpeed(speed);
        doubleMM.setFunctions(functions);
        doubleMM.getGL2().setAddress(doubleMM.getAddress2());
        resp = setSpeedOnGl(doubleMM.getGL2(), doubleMM, doubleMM.direction, speed, functions2);

        r = new Response(resp);
        locomotive.setLastCommandAcknowledge(r.getTimestamp());
    }

    @Override
    public void initLocomotive(final SRCPLocomotive locomotive,
                               final SRCPSession session, final SRCPLockControl lockControl)
            throws SRCPLocomotiveException {

        final DoubleMMDigitalLocomotive doubleMM = (DoubleMMDigitalLocomotive) locomotive;
        if (doubleMM.getGL() == null) {
            final GL gl = new GL(session, locomotive.getBus());
            gl.setAddress(locomotive.getAddress());
            locomotive.setGL(gl);
            lockControl.registerControlObject(
                    "GL",
                    new SRCPAddress(locomotive.getBus(), locomotive
                            .getAddress()), locomotive);
        }
        if (doubleMM.getGL2() == null) {
            final GL gl2 = new GL(session, locomotive.getBus());
            gl2.setAddress(doubleMM.getAddress2());
            doubleMM.setGL2(gl2);
            lockControl
                    .registerControlObject(
                            "GL",
                            new SRCPAddress(locomotive.getBus(), doubleMM
                                    .getAddress2()), locomotive);
        }
        if (!locomotive.isInitialized()) {
            initLocomotive(doubleMM);
        }
    }

    private void initLocomotive(final DoubleMMDigitalLocomotive locomotive)
            throws SRCPLocomotiveException {
        try {
            final String[] params = locomotive.getParams();
            final String[] params2 = locomotive.getParams2();
            locomotive.getGL().init(locomotive.getAddress(),
                    locomotive.getProtocol(), params);
            locomotive.getGL2().init(locomotive.getAddress2(),
                    locomotive.getProtocol(), params2);
            locomotive.setInitialized(true);
        } catch (final SRCPException x) {
            throw new SRCPLocomotiveException(Constants.ERR_INIT_FAILED, x);
        }
    }

    @Override
    public boolean[] getEmergencyStopFunctions(final SRCPLocomotive locomotive,
                                               final int emergencyStopFunction) {
        final boolean[] functions = new boolean[]{false, false, false, false,
                false, false, false, false, false, false};
        if (emergencyStopFunction != -1) {
            functions[emergencyStopFunction] = true;
        }
        return functions;
    }

    @Override
    public void mergeFunctions(SRCPLocomotive locomotive, int address, boolean[] newFunctions) {

        DoubleMMDigitalLocomotive simulatedMfxLocomotive = (DoubleMMDigitalLocomotive) locomotive;
        int startOffset = 0;
        if (simulatedMfxLocomotive.getAddress2() == address) {
            startOffset = 5;
        }

        boolean[] mergedFunctions = Arrays.copyOf(locomotive.getFunctions(), locomotive.getFunctions().length);
        for (int i = startOffset; i < startOffset + newFunctions.length; i++) {
            mergedFunctions[i] = newFunctions[i - startOffset];
        }
        simulatedMfxLocomotive.setFunctions(mergedFunctions);
    }

    @Override
    public void terminate(SRCPLocomotive locomotive) throws SRCPException {
        final DoubleMMDigitalLocomotive doubleMM = (DoubleMMDigitalLocomotive) locomotive;
        doubleMM.getGL().term();
        doubleMM.getGL2().term();
        doubleMM.setInitialized(false);
    }
}
