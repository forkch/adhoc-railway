/*------------------------------------------------------------------------
 *
 * copyright : (C) 2008 by Benjamin Mueller
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPLocomotiveControl.java,v 1.9 2012-03-15 06:22:50 fork_ch Exp $
 *
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package de.dermoba.srcp.model.locomotives;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;
import de.dermoba.srcp.devices.listener.GLInfoListener;
import de.dermoba.srcp.model.*;
import de.dermoba.srcp.model.locking.SRCPLockChangeListener;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Controls all actions which can be performed on Locomotives.
 *
 * @author fork
 */
public class SRCPLocomotiveControl implements GLInfoListener, Constants {
    @SuppressWarnings("rawtypes")
    private static final Map<Class, LocomotiveStrategy> LOCOMOTIVE_STRATEGIES = new HashMap<Class, LocomotiveStrategy>();
    private static Logger LOGGER = Logger
            .getLogger(SRCPLocomotiveControl.class);
    private static SRCPLocomotiveControl instance;

    static {
        final DefaultLocomotiveStrategy defaultStrategy = new DefaultLocomotiveStrategy();
        final SimulatedMFXLocomotiveStrategy simulatedMFXStrategy = new SimulatedMFXLocomotiveStrategy();
        LOCOMOTIVE_STRATEGIES.put(MMDigitalLocomotive.class, defaultStrategy);
        LOCOMOTIVE_STRATEGIES.put(MMDeltaLocomotive.class, defaultStrategy);
        LOCOMOTIVE_STRATEGIES.put(MfxLocomotive.class, defaultStrategy);
        LOCOMOTIVE_STRATEGIES.put(DCCLocomotive.class, defaultStrategy);
        LOCOMOTIVE_STRATEGIES.put(DoubleMMDigitalLocomotive.class,
                simulatedMFXStrategy);
    }

    private final List<SRCPLocomotiveChangeListener> listeners;
    private final SRCPLockControl lockControl = SRCPLockControl.getInstance();
    private final List<SRCPLocomotive> SRCP_LOCOMOTIVE_CACHE;
    private final Map<SRCPAddress, SRCPLocomotive> BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE;
    private SRCPSession session;

    private SRCPLocomotiveControl() {
        LOGGER.info("SRCPLocomotiveControl loaded");
        listeners = new ArrayList<SRCPLocomotiveChangeListener>();
        SRCP_LOCOMOTIVE_CACHE = new ArrayList<SRCPLocomotive>();
        BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE = new HashMap<SRCPAddress, SRCPLocomotive>();
    }

    public static SRCPLocomotiveControl getInstance() {
        if (instance == null) {
            instance = new SRCPLocomotiveControl();
        }
        return instance;
    }

    public void toggleDirection(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);
        if (locomotive.currentSpeed > 0) {
            return;
        }
        switch (locomotive.direction) {
            case REVERSE:
                locomotive.setDirection(SRCPLocomotiveDirection.FORWARD);
                break;
            case FORWARD:
            case UNDEF:
            default:
                locomotive.setDirection(SRCPLocomotiveDirection.REVERSE);
                break;

        }
        setSpeed(locomotive, 0, locomotive.getFunctions());
        informListeners(locomotive);
    }

    public SRCPLocomotiveDirection getDirection(final SRCPLocomotive locomotive) {
        if (locomotive == null) {
            return SRCPLocomotiveDirection.UNDEF;
        }
        return locomotive.getDirection();
    }

    public int getCurrentSpeed(final SRCPLocomotive locomotive) {
        if (locomotive == null) {
            return 0;
        }
        return locomotive.getCurrentSpeed();
    }

    public void setSpeed(final SRCPLocomotive locomotive, final int speed,
                         final boolean[] functions) throws
            SRCPModelException {

        System.out.println(locomotive.getDirection());
        checkLocomotive(locomotive);
        try {

            sendLocoInitIfNeeded(locomotive);

            final LocomotiveStrategy strategy = LOCOMOTIVE_STRATEGIES
                    .get(locomotive.getClass());

            switch (locomotive.direction) {
                case FORWARD:
                case REVERSE:
                    break;
                case EMERGENCY_STOP:
                    locomotive.setDirection(SRCPLocomotiveDirection.FORWARD);
                    break;
                case UNDEF:
                    locomotive.setDirection(SRCPLocomotiveDirection.FORWARD);
                    break;
            }

            strategy.setSpeed(locomotive, locomotive.direction, speed, functions);
            //informListeners(locomotive);
        } catch (final SRCPDeviceLockedException x) {
            throw new SRCPLocomotiveLockedException(ERR_LOCKED);
        } catch (final SRCPException x) {
            throw new SRCPLocomotiveException(ERR_FAILED, x);
        }

    }

    public void increaseSpeed(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);
        final int newSpeed = locomotive.getCurrentSpeed() + 1;

        setSpeed(locomotive, newSpeed, locomotive.getFunctions());

    }

    public void decreaseSpeed(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);
        final int newSpeed = locomotive.getCurrentSpeed() - 1;

        setSpeed(locomotive, newSpeed, locomotive.getFunctions());
    }

    public void increaseSpeedStep(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);
        final int newSpeed = locomotive.getCurrentSpeed() + 1;

        setSpeed(locomotive, newSpeed, locomotive.getFunctions());
    }

    public void decreaseSpeedStep(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);
        final int newSpeed = locomotive.getCurrentSpeed() - 1;
        setSpeed(locomotive, newSpeed, locomotive.getFunctions());
    }

    public void setFunctions(final SRCPLocomotive locomotive,
                             final boolean[] functions) throws
            SRCPModelException {
        checkLocomotive(locomotive);
        setSpeed(locomotive, locomotive.getCurrentSpeed(), functions);
    }

    public boolean[] getFunctions(final SRCPLocomotive locomotive) {
        if (locomotive == null) {
            return new boolean[0];
        }

        return locomotive.getFunctions();
    }

    public void emergencyStop(final SRCPLocomotive locomotive,
                              final int emergencyStopFunction) throws
            SRCPModelException {

        checkLocomotive(locomotive);
        try {

            sendLocoInitIfNeeded(locomotive);

            final LocomotiveStrategy strategy = LOCOMOTIVE_STRATEGIES
                    .get(locomotive.getClass());

            strategy.setSpeed(locomotive, SRCPLocomotiveDirection.EMERGENCY_STOP, 0, locomotive.getFunctions());
            //informListeners(locomotive);
        } catch (final SRCPDeviceLockedException x) {
            throw new SRCPLocomotiveLockedException(ERR_LOCKED);
        } catch (final SRCPException x) {
            throw new SRCPLocomotiveException(ERR_FAILED, x);
        }
    }

    private void sendLocoInitIfNeeded(SRCPLocomotive locomotive) throws SRCPLocomotiveException {

        if (locomotive.getGL() == null) {
            final GL gl = new GL(session, locomotive.getBus());
            gl.setAddress(locomotive.getAddress());
            locomotive.setGL(gl);
            lockControl.registerControlObject(
                    "GL",
                    new SRCPAddress(locomotive.getBus(), locomotive
                            .getAddress()), locomotive);
        }

        if (!locomotive.isInitialized()) {
            final LocomotiveStrategy strategy = LOCOMOTIVE_STRATEGIES.get(locomotive
                    .getClass());
            strategy.initLocomotive(locomotive, session, lockControl);
        }
    }

    public void terminate(final SRCPLocomotive locomotive) throws SRCPModelException {
        checkLocomotive(locomotive);

        try {
            final LocomotiveStrategy strategy = LOCOMOTIVE_STRATEGIES
                    .get(locomotive.getClass());
            strategy.terminate(locomotive);

        } catch (final SRCPDeviceLockedException x) {
            throw new SRCPLocomotiveLockedException(ERR_LOCKED);
        } catch (final SRCPException x) {
            throw new SRCPLocomotiveException(ERR_FAILED, x);
        }

    }

    public void GLinit(final double timestamp, final int bus,
                       final int address, final String protocol, final String[] params) {
        LOGGER.debug("GLinit( " + bus + " , " + address + " , " + protocol
                + " , " + Arrays.toString(params) + " )");
        final SRCPLocomotive locomotive = BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE
                .get(new SRCPAddress(bus, address));
        if (locomotive == null) {
            // ignore unknown locomotive
            return;
        }
        try {
            locomotive.setInitialized(true);
            checkLocomotive(locomotive);
        } catch (final SRCPModelException e1) {
        }
    }

    public void GLset(final double timestamp, final int bus, final int address,
                      final SRCPLocomotiveDirection drivemode, final int v,
                      final int vMax, final boolean[] functions) {

        LOGGER.debug("GLset( " + bus + " , " + address + " , " + drivemode
                + " , " + v + " , " + vMax + " , " + Arrays.toString(functions)
                + " )");
        final SRCPLocomotive locomotive = BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE
                .get(new SRCPAddress(bus, address));
        try {
            checkLocomotive(locomotive);
        } catch (final SRCPModelException e1) {
            // ignore unknown locomotive
        }
        // Update locomotive if known and if info is newer than our own.
        if (locomotive != null //&& timestamp > locomotive.getLastCommandAcknowledge()
                ) {
            if (drivemode != SRCPLocomotiveDirection.EMERGENCY_STOP) {
            // only store non EMERGENCY_STOP states
                locomotive.setDirection(drivemode);

            }
            locomotive.setCurrentSpeed(v);
            LOCOMOTIVE_STRATEGIES.get(locomotive.getClass()).mergeFunctions(locomotive, address, functions);
            LOGGER.info("merged functions:" + locomotive.getFunctions());
            informListeners(locomotive);
        }
    }

    public void GLterm(final double timestamp, final int bus, final int address) {
        LOGGER.debug("GLterm( " + bus + " , " + address + " )");

        final SRCPLocomotive locomotive = BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE
                .get(new SRCPAddress(bus, address));
        try {
            checkLocomotive(locomotive);
        } catch (final SRCPModelException e1) {
            // ignore unknown locomotive
        }
        if (locomotive != null) {
            locomotive.setGL(null);
            lockControl.unregisterControlObject("GL", new SRCPAddress(bus,
                    address));
            locomotive.setInitialized(false);
        }
    }

    public void addLocomotiveChangeListener(
            final SRCPLocomotiveChangeListener l,
            final SRCPLockChangeListener lockListener) {
        listeners.add(l);
        lockControl.addLockChangeListener(lockListener);
    }

    public void removeLocomotiveChangeListener(
            final SRCPLocomotiveChangeListener l,
            final SRCPLockChangeListener lockListener) {
        listeners.remove(l);
        lockControl.removeLockChangeListener(lockListener);
    }

    public void removeAllLocomotiveChangeListener() {
        listeners.clear();
    }

    private void informListeners(final SRCPLocomotive changedLocomotive) {
        for (final SRCPLocomotiveChangeListener l : listeners) {
            l.locomotiveChanged(changedLocomotive);
        }

    }

    private void checkLocomotive(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        if (locomotive == null) {
            return;
        }
        if (!locomotive.checkAddress()) {
            throw new InvalidAddressException();
        }

        if (locomotive.getSession() == null && session == null) {
            throw new NoSessionException();
        }
        if (locomotive.getSession() == null && session != null) {
            locomotive.setSession(session);
        }

        if (!SRCP_LOCOMOTIVE_CACHE.contains(locomotive)) {
            SRCP_LOCOMOTIVE_CACHE.add(locomotive);
            BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE.put(new SRCPAddress(locomotive.getBus(), locomotive.getAddress()), locomotive);
            if (locomotive instanceof DoubleMMDigitalLocomotive) {
                final DoubleMMDigitalLocomotive doubleMMDigitalLocomotive = (DoubleMMDigitalLocomotive) locomotive;
                BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE.put(new SRCPAddress(doubleMMDigitalLocomotive.getBus(), doubleMMDigitalLocomotive.getAddress2()), locomotive);
            }
        }
    }

    public boolean acquireLock(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);

        try {
            return lockControl.acquireLock(
                    "GL",
                    new SRCPAddress(locomotive.getBus(), locomotive
                            .getAddress())
            );
        } catch (final SRCPDeviceLockedException e) {
            throw new SRCPLocomotiveLockedException(Constants.ERR_LOCKED, e);
        }
    }

    public boolean releaseLock(final SRCPLocomotive locomotive)
            throws SRCPModelException {

        checkLocomotive(locomotive);
        try {
            return lockControl.releaseLock(
                    "GL",
                    new SRCPAddress(locomotive.getBus(), locomotive
                            .getAddress())
            );
        } catch (final SRCPDeviceLockedException e) {
            throw new SRCPLocomotiveLockedException(Constants.ERR_LOCKED, e);
        }
    }

    public boolean isLocked(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);

        return lockControl.isLocked("GL", new SRCPAddress(locomotive.getBus(),
                locomotive.getAddress()));
    }

    public boolean isLockedByMe(final SRCPLocomotive locomotive)
            throws SRCPModelException {
        checkLocomotive(locomotive);

        final int sessionID = lockControl.getLockingSessionID("GL",
                new SRCPAddress(locomotive.getBus(), locomotive.getAddress()));
        return sessionID == session.getCommandChannelID();
    }

    public SRCPSession getSession() {
        return session;
    }

    public void setSession(final SRCPSession session) {
        this.session = session;
        SRCP_LOCOMOTIVE_CACHE.clear();
        BUS_ADDRESS_TO_SRCP_LOCOMOTIVE_CACHE.clear();
        if (session != null) {
            session.getInfoChannel().addGLInfoListener(this);
        }
        for (final SRCPLocomotive l : SRCP_LOCOMOTIVE_CACHE) {
            l.setSession(session);
        }

    }

}
