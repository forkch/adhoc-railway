/*------------------------------------------------------------------------
 *
 * copyright : (C) 2008 by Benjamin Mueller
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPTurnoutControl.java,v 1.8 2011-12-18 09:43:06 andre_schenk Exp $
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

package de.dermoba.srcp.model.turnouts;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;
import de.dermoba.srcp.devices.listener.GAInfoListener;
import de.dermoba.srcp.model.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRCPTurnoutControl implements GAInfoListener {
    private static final Logger LOGGER = Logger.getLogger(SRCPTurnoutControl.class);
    private static SRCPTurnoutControl instance;

    private final List<SRCPTurnoutChangeListener> listeners = new ArrayList<SRCPTurnoutChangeListener>();

    private final List<SRCPTurnout> srcpTurnouts = new ArrayList<SRCPTurnout>();

    private final Map<SRCPAddress, SRCPTurnout> addressTurnoutCache = new HashMap<SRCPAddress, SRCPTurnout>();
    private final Map<SRCPAddress, SRCPTurnout> addressThreewayCache = new HashMap<SRCPAddress, SRCPTurnout>();
    private SRCPTurnout lastChangedTurnout;

    private SRCPTurnoutState previousState;
    private SRCPSession session;
    private boolean interface6051Connected = Constants.INTERFACE_6051_CONNECTED;
    private int turnoutActivationTime = Constants.DEFAULT_ACTIVATION_TIME;
    private int cuttersleepTime = Constants.DEFAULT_CUTTER_REPETITION_SLEEP;

    private SRCPTurnoutControl() {
        LOGGER.info("SRCPTurnoutControl loaded");
    }

    public static SRCPTurnoutControl getInstance() {
        if (instance == null) {
            instance = new SRCPTurnoutControl();
        }
        return instance;
    }

    public void setSession(final SRCPSession session) {
        this.session = session;
        srcpTurnouts.clear();
        addressTurnoutCache.clear();
        addressThreewayCache.clear();
        if (session != null) {
            session.getInfoChannel().addGAInfoListener(this);
        }

        for (final SRCPTurnout st : srcpTurnouts) {
            st.setSession(session);
        }
    }

    /**
     * Returns the port to activate according to the addressSwitched flag.
     *
     * @param wantedPort The port to 'convert'
     * @return The 'converted' port
     */
    int getPort(final SRCPTurnout turnout, final int wantedPort) {
        if (!turnout.isAddress1Switched()) {
            return wantedPort;
        } else {
            if (wantedPort == SRCPTurnout.TURNOUT_STRAIGHT_PORT) {
                return SRCPTurnout.TURNOUT_CURVED_PORT;
            } else {
                return SRCPTurnout.TURNOUT_STRAIGHT_PORT;
            }
        }
    }

    public void toggle(final SRCPTurnout turnout) throws
            SRCPModelException {
        checkTurnout(turnout);
        initTurnout(turnout);
        if (turnout.isThreeWay()) {
            toggleThreeWay(turnout);
            lastChangedTurnout = turnout;
        } else if (turnout.isCutter()) {
            enableCutter(turnout);
        } else {
            previousState = turnout.getTurnoutState();
            switch (previousState) {
                case STRAIGHT:
                    setCurvedLeft(turnout);
                    break;
                case RIGHT:
                case LEFT:
                    setStraight(turnout);
                    break;
                case UNDEF:
                    setDefaultState(turnout);
            }
        }
        lastChangedTurnout = turnout;
    }

    private void toggleThreeWay(final SRCPTurnout turnout)
            throws SRCPModelException {
        // turnout has already been initialized
        final SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
        for (final SRCPTurnout t : subTurnouts) {
            checkTurnout(t);
        }
        switch (turnout.getTurnoutState()) {
            case LEFT:
                setStraightThreeWay(turnout);
                break;
            case STRAIGHT:
                setCurvedRightThreeWay(turnout);
                break;
            case RIGHT:
                setCurvedLeftThreeWay(turnout);
                break;
            case UNDEF:
                setStraightThreeWay(turnout);
                break;
        }
        // informListeners(turnout);
        lastChangedTurnout = turnout;
    }

    public void setDefaultState(final SRCPTurnout turnout)
            throws SRCPModelException {
        checkTurnout(turnout);
        initTurnout(turnout);
        previousState = turnout.getTurnoutState();
        if (turnout.isThreeWay()) {
            setDefaultStateThreeWay(turnout);
        } else if (turnout.isCutter()) {
            // noop
        } else {
            switch (turnout.getDefaultState()) {
                case STRAIGHT:
                    setStraight(turnout);
                    break;
                case LEFT:
                case RIGHT:
                    setCurvedLeft(turnout);
                    break;
                case UNDEF:
                    break;
                default:
                    break;
            }
        }
        // informListeners(turnout);
        lastChangedTurnout = turnout;
    }

    private void setDefaultStateThreeWay(final SRCPTurnout turnout)
            throws SRCPModelException {
        setStraightThreeWay(turnout);
    }

    public void setNonDefaultState(final SRCPTurnout turnout)
            throws SRCPModelException {
        checkTurnout(turnout);
        previousState = turnout.getTurnoutState();
        if (turnout.isThreeWay()) {
            setNonDefaultStateTheeWay(turnout);
        } else if (turnout.isCutter()) {
            enableCutter(turnout);
        } else {
            switch (turnout.getDefaultState()) {
                case STRAIGHT:
                    setCurvedLeft(turnout);
                    break;
                case LEFT:
                case RIGHT:
                    setStraight(turnout);
                    break;
                case UNDEF:
                    break;
                default:
                    break;
            }
        }
        lastChangedTurnout = turnout;
    }

    private void setNonDefaultStateTheeWay(final SRCPTurnout turnout) {
        // do nothing
    }

    public void setStraight(final SRCPTurnout turnout)
            throws SRCPModelException {
        checkTurnout(turnout);
        initTurnout(turnout);
        LOGGER.info("checked turnout");
        previousState = turnout.getTurnoutState();
        if (turnout.isThreeWay()) {
            setStraightThreeWay(turnout);
            return;
        }
        final GA ga = turnout.getGA();
        ga.setAddress(turnout.getAddress1());
        try {
            ga.set(getPort(turnout, SRCPTurnout.TURNOUT_STRAIGHT_PORT),
                    SRCPTurnout.TURNOUT_PORT_ACTIVATE, turnoutActivationTime);
            turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);

            lastChangedTurnout = turnout;
        } catch (final SRCPDeviceLockedException x1) {
            throw new SRCPTurnoutLockedException(Constants.ERR_LOCKED, x1);
        } catch (final SRCPException e) {
            LOGGER.error(e);
            throw new SRCPTurnoutException(Constants.ERR_TOGGLE_FAILED, e);
        }
    }


    public void enableCutter(final SRCPTurnout cutter)
            throws SRCPModelException {
        checkTurnout(cutter);
        LOGGER.info("checked cutter");
        previousState = cutter.getTurnoutState();
        final GA ga = cutter.getGA();
        ga.setAddress(cutter.getAddress1());
        try {
            int reps = 1;
            for (int i = 0; i < reps; i++) {
                ga.set(getPort(cutter, SRCPTurnout.TURNOUT_CURVED_PORT),
                        SRCPTurnout.TURNOUT_PORT_ACTIVATE, turnoutActivationTime);
                if (reps > 1) {
                    try {
                        Thread.sleep(cuttersleepTime);
                    } catch (InterruptedException e) {
                        throw new SRCPTurnoutException("failed to sleep");
                    }
                }
            }

            cutter.setTurnoutState(SRCPTurnoutState.STRAIGHT);

            lastChangedTurnout = cutter;
        } catch (final SRCPDeviceLockedException x1) {
            throw new SRCPTurnoutLockedException(Constants.ERR_LOCKED, x1);
        } catch (final SRCPException e) {
            LOGGER.error(e);
            throw new SRCPTurnoutException(Constants.ERR_TOGGLE_FAILED, e);
        }
    }

    private void setStraightThreeWay(final SRCPTurnout turnout)
            throws SRCPModelException {
        // turnout has already been initialized
        final SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
        for (final SRCPTurnout t : subTurnouts) {
            checkTurnout(t);
        }
        setStraight(subTurnouts[0]);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setStraight(subTurnouts[1]);
        turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
        lastChangedTurnout = turnout;
    }

    public void setCurvedLeft(final SRCPTurnout turnout)
            throws SRCPModelException {
        checkTurnout(turnout);
        initTurnout(turnout);
        previousState = turnout.getTurnoutState();
        if (turnout.isThreeWay()) {
            setCurvedLeftThreeWay(turnout);
            return;
        }
        final GA ga = turnout.getGA();
        ga.setAddress(turnout.getAddress1());
        try {

            ga.set(getPort(turnout, SRCPTurnout.TURNOUT_CURVED_PORT),
                    SRCPTurnout.TURNOUT_PORT_ACTIVATE, turnoutActivationTime);

            turnout.setTurnoutState(SRCPTurnoutState.LEFT);
            lastChangedTurnout = turnout;
        } catch (final SRCPDeviceLockedException x1) {
            throw new SRCPTurnoutLockedException(Constants.ERR_LOCKED, x1);
        } catch (final SRCPException e) {
            LOGGER.error(e);
            throw new SRCPTurnoutException(Constants.ERR_TOGGLE_FAILED, e);
        }
    }

    private void setCurvedLeftThreeWay(final SRCPTurnout turnout)
            throws SRCPModelException {
        // turnout has already been initialized
        final SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
        for (final SRCPTurnout t : subTurnouts) {
            checkTurnout(t);
        }

        setCurvedLeft(subTurnouts[0]);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setStraight(subTurnouts[1]);
        turnout.setTurnoutState(SRCPTurnoutState.LEFT);
        // informListeners(turnout);
        lastChangedTurnout = turnout;
    }

    public void setCurvedRight(final SRCPTurnout turnout)
            throws SRCPModelException {
        checkTurnout(turnout);
        initTurnout(turnout);
        previousState = turnout.getTurnoutState();
        if (turnout.isThreeWay()) {
            setCurvedRightThreeWay(turnout);
            return;
        }
        setCurvedLeft(turnout);

        // informListeners(turnout);
        lastChangedTurnout = turnout;
    }

    private void setCurvedRightThreeWay(final SRCPTurnout turnout)
            throws SRCPModelException {
        // turnout has already been initialized
        final SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
        for (final SRCPTurnout t : subTurnouts) {
            checkTurnout(t);
        }

        setStraight(subTurnouts[0]);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setCurvedLeft(subTurnouts[1]);
        turnout.setTurnoutState(SRCPTurnoutState.RIGHT);
        // informListeners(turnout);
        lastChangedTurnout = turnout;
    }

    public SRCPTurnoutState getTurnoutState(final SRCPTurnout turnout) {
        return turnout.getTurnoutState();
    }

    @Override
    public void GAset(final double timestamp, final int bus, final int address,
                      final int port, final int value) {
        LOGGER.debug("GAset(" + bus + " , " + address + " , " + port + " , "
                + value + " )");
        final SRCPTurnout turnout = getTurnoutByAddressBus(bus, address);
        if (turnout == null) {
            LOGGER.warn("GAset for a turnout which the srcp-server knows but not me");
            return;
        }
        try {
            if (!turnout.isInitialized()) {
                LOGGER.warn("GAset for a for me not initialized turnout");
                initTurnout(turnout);
            }
            checkTurnout(turnout);

            if (value == 0) {
                // wait for activation
                return;
            }
            // a port has been activated
            if (turnout.isThreeWay()) {
                portChangedThreeway(turnout, address, port);
            } else {
                portChanged(turnout, port);
            }

            informListeners(turnout);
        } catch (final SRCPModelException e) {
            e.printStackTrace();
        }
    }

    private void portChanged(final SRCPTurnout turnout, final int port) {
        if (turnout.isCutter()) {
            turnout.setTurnoutState(SRCPTurnoutState.LEFT);
            return;
        }

        if (port == getPort(turnout, SRCPTurnout.TURNOUT_STRAIGHT_PORT)) {
            turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
        } else if (port == getPort(turnout, SRCPTurnout.TURNOUT_CURVED_PORT)) {
            turnout.setTurnoutState(SRCPTurnoutState.LEFT);
        }
    }

    private void portChangedThreeway(final SRCPTurnout turnout,
                                     final int address, final int port) {
        final SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
        for (final SRCPTurnout subTurnout : subTurnouts) {
            if (subTurnout.getAddress1() == address) {
                portChanged(subTurnout, port);
            }
        }
        if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.STRAIGHT
                && subTurnouts[1].getTurnoutState() == SRCPTurnoutState.STRAIGHT) {
            turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
        } else if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.LEFT
                && subTurnouts[1].getTurnoutState() == SRCPTurnoutState.STRAIGHT) {
            turnout.setTurnoutState(SRCPTurnoutState.LEFT);
        } else if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.STRAIGHT
                && subTurnouts[1].getTurnoutState() == SRCPTurnoutState.LEFT) {
            turnout.setTurnoutState(SRCPTurnoutState.RIGHT);
        } else if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.LEFT
                && subTurnouts[1].getTurnoutState() == SRCPTurnoutState.LEFT) {
            turnout.setTurnoutState(SRCPTurnoutState.UNDEF);
        }
    }

    @Override
    public void GAinit(final double timestamp, final int bus,
                       final int address, final String protocol, final String[] params) {
        LOGGER.debug("GAinit(" + bus + " , " + address + " , " + protocol
                + " , " + params + " )");

        final SRCPTurnout turnout = getTurnoutByAddressBus(bus, address);
        if (turnout == null) {
            // i don't know this turnout
            return;
        }
        try {
            checkTurnout(turnout);
            informListeners(turnout);
        } catch (final SRCPModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void GAterm(final double timestamp, final int bus, final int address) {
        LOGGER.debug("GAterm( " + bus + " , " + address + " )");
        final SRCPTurnout turnout = getTurnoutByAddressBus(bus, address);
        try {
            checkTurnout(turnout);
            if (turnout != null) {
                turnout.setGA(null);
                turnout.setInitialized(false);
                informListeners(turnout);
            }
        } catch (final SRCPModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addTurnoutChangeListener(
            final SRCPTurnoutChangeListener listener) {
        listeners.add(listener);
    }

    public void removeTurnoutChangeListener(
            final SRCPTurnoutChangeListener listener) {
        listeners.remove(listener);
    }

    public void removeAllTurnoutChangeListener() {
        listeners.clear();
    }

    void informListeners(final SRCPTurnout changedTurnout) {

        for (final SRCPTurnoutChangeListener scl : listeners) {
            scl.turnoutChanged(changedTurnout, changedTurnout.getTurnoutState());
        }
        LOGGER.debug("turnoutChanged(" + changedTurnout + ")");

    }

    public void addTurnout(final SRCPTurnout turnout) {
        srcpTurnouts.add(turnout);
        if (turnout.isThreeWay()) {
            addressTurnoutCache.put(
                    new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
                            turnout.getBus2(), turnout.getAddress2()), turnout);
            addressThreewayCache.put(
                    new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
                            0, 0), turnout);
            addressThreewayCache.put(new SRCPAddress(0, 0, turnout.getBus2(),
                    turnout.getAddress2()), turnout);
        } else {
            addressTurnoutCache.put(
                    new SRCPAddress(turnout.getBus1(), turnout.getAddress1()),
                    turnout);
        }
    }

    public void removeTurnout(final SRCPTurnout turnout) {
        if (turnout == null) {
            return;
        }
        srcpTurnouts.remove(turnout);
        addressTurnoutCache.remove(new SRCPAddress(turnout.getBus1(), turnout
                .getAddress1(), turnout.getBus2(), turnout.getAddress2()));
        if (turnout.isThreeWay()) {

            addressTurnoutCache.remove(new SRCPAddress(turnout.getBus1(),
                    turnout.getAddress1(), 0, 0));
            addressTurnoutCache.remove(new SRCPAddress(0, 0, turnout.getBus2(),
                    turnout.getAddress2()));
        }
    }

    void checkTurnout(final SRCPTurnout turnout) throws SRCPModelException {
        LOGGER.debug("checkTurnout(" + turnout + ")");
        if (turnout == null) {
            return;
        }
        if (!turnout.checkBusAddress()) {
            throw new InvalidAddressException(
                    "Turnout has an invalid address or bus");
        }

        if (!srcpTurnouts.contains(turnout)) {
            srcpTurnouts.add(turnout);
            addressTurnoutCache.put(
                    new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
                            turnout.getBus2(), turnout.getAddress2()), turnout);
            if (turnout.isThreeWay()) {
                addressThreewayCache.put(new SRCPAddress(turnout.getBus1(),
                        turnout.getAddress1(), 0, 0), turnout);
                addressThreewayCache.put(
                        new SRCPAddress(0, 0, turnout.getBus2(), turnout
                                .getAddress2()), turnout);
            }
        }

        if (turnout.getSession() == null && session == null) {
            throw new NoSessionException();
        }
        if (turnout.getSession() == null && session != null) {
            turnout.setSession(session);
        }
    }

    private void initTurnout(final SRCPTurnout turnout)
            throws SRCPTurnoutException {

        if (!turnout.isInitialized()) {
            if (turnout.isThreeWay()) {
                initTurnoutThreeWay(turnout);
            } else {
                try {
                    final GA ga = new GA(session, turnout.getBus1());
                    if (interface6051Connected) {
                        ga.init(turnout.getAddress1(), turnout.getProtocol());
                    } else {
                        String[] params = new String[1];
                        switch (turnout.getTurnoutType()) {

                            case DEFAULT:
                            case DOUBLECROSS:
                            case THREEWAY:
                                params[0] = "1";
                                break;
                            case CUTTER:
                                params[0] = "2";
                                break;
                        }
                        ga.init(turnout.getAddress1(), turnout.getProtocol(), params);
                        ga.setAddress(turnout.getAddress1());
                    }

                    turnout.setGA(ga);
                    turnout.setInitialized(true);
                } catch (final SRCPException e) {
                    LOGGER.error(e);
                    throw new SRCPTurnoutException(Constants.ERR_INIT_FAILED, e);
                }
            }
        }
    }

    private void initTurnoutThreeWay(final SRCPTurnout turnout)
            throws SRCPTurnoutException {
        final SRCPTurnout turnout1 = (SRCPTurnout) turnout.clone();
        final SRCPTurnout turnout2 = (SRCPTurnout) turnout.clone();

        turnout1.setBus1(turnout.getBus1());
        turnout1.setAddress1(turnout.getAddress1());
        turnout1.setAddress1Switched(turnout.isAddress1Switched());
        turnout1.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
        turnout1.setSession(turnout.getSession());

        turnout2.setBus1(turnout.getBus2());
        turnout2.setAddress1(turnout.getAddress2());
        turnout2.setAddress1Switched(turnout.isAddress2Switched());
        turnout2.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
        turnout2.setSession(turnout.getSession());

        srcpTurnouts.add(turnout1);
        srcpTurnouts.add(turnout2);
        initTurnout(turnout1);
        initTurnout(turnout2);

        final SRCPTurnout[] turnouts = new SRCPTurnout[]{turnout1, turnout2};
        turnout.setSubTurnouts(turnouts);

    }

    public void undoLastChange() throws
            SRCPModelException {
        if (lastChangedTurnout == null) {
            return;
        }
        switch (previousState) {

            case STRAIGHT:
                setStraight(lastChangedTurnout);
                break;
            case LEFT:
                setCurvedLeft(lastChangedTurnout);
                break;
            case RIGHT:
                setCurvedRight(lastChangedTurnout);
                break;
            case UNDEF:
                setStraight(lastChangedTurnout);
                break;
        }
        // informListeners(lastChangedTurnout);

        lastChangedTurnout = null;
        previousState = null;
    }

    public void previousDeviceToDefault() throws
            SRCPModelException {
        if (lastChangedTurnout == null) {
            return;
        }
        setDefaultState(lastChangedTurnout);
    }

    private SRCPTurnout getTurnoutByAddressBus(final int bus, final int address) {
        final SRCPAddress key1 = new SRCPAddress(bus, address, 0, 0);
        final SRCPTurnout lookup1 = addressTurnoutCache.get(key1);
        if (lookup1 != null) {
            return lookup1;
        }
        final SRCPAddress key2 = new SRCPAddress(0, 0, bus, address);
        final SRCPTurnout lookup2 = addressTurnoutCache.get(key2);
        if (lookup2 != null) {
            return lookup2;
        }
        final SRCPTurnout threewayLookup1 = addressThreewayCache.get(key1);
        if (threewayLookup1 != null) {
            return threewayLookup1;
        }

        final SRCPTurnout threewayLookup2 = addressThreewayCache.get(key2);
        if (threewayLookup2 != null) {
            return threewayLookup2;
        }

        return null;
    }

    public boolean isInterface6051Connected() {
        return interface6051Connected;
    }

    public void setInterface6051Connected(final boolean interface6051Connected) {
        this.interface6051Connected = interface6051Connected;
    }

    public int getTurnoutActivationTime() {
        return turnoutActivationTime;
    }

    public void setTurnoutActivationTime(final int turnoutActivationTime) {
        this.turnoutActivationTime = turnoutActivationTime;
    }

    public int getCutterSleepTime() {
        return cuttersleepTime;
    }

    public void setCutterSleepTime(final int cuttersleepTime) {
        this.cuttersleepTime = cuttersleepTime;
    }

    public void setTurnoutWithAddress(final int address,
                                      final SRCPTurnoutState state) throws SRCPException {
        final GA ga = new GA(session, 1);
        ga.setAddress(address);
        if (state == SRCPTurnoutState.STRAIGHT) {
            ga.set(1, 1, 1000);
        } else {
            ga.set(0, 1, 1000);
        }
    }
}
