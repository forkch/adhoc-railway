/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPTurnout.java,v 1.3 2010-11-17 14:04:55 mlipp Exp $
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
import de.dermoba.srcp.devices.GA;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class SRCPTurnout {

    protected int address1;

    protected boolean address1Switched;

    protected int address2;

    protected boolean address2Switched;

    protected int bus1;

    protected int bus2;

    protected SRCPTurnoutState defaultState;

    private GA ga;

    protected boolean initialized = false;

    protected String protocol;

    private SRCPSession session;

    private SRCPTurnout[] subTurnouts;

    protected SRCPTurnoutState turnoutState = SRCPTurnoutState.UNDEF;

    protected SRCPTurnoutTypes turnoutType;
    public final static int TURNOUT_STRAIGHT_PORT = 0;

    public final static int TURNOUT_CURVED_PORT = 1;
    public final static int TURNOUT_PORT_ACTIVATE = 1;

    public final static int TURNOUT_PORT_DEACTIVATE = 0;

    public SRCPTurnout() {
    }

    public SRCPTurnout(int address1, boolean address1Switched, int bus1,
                       SRCPTurnoutState defaultState, SRCPTurnoutTypes turnoutType) {
        super();
        this.address1 = address1;
        this.address1Switched = address1Switched;
        this.bus1 = bus1;
        this.defaultState = defaultState;
        this.turnoutType = turnoutType;
    }

    public SRCPTurnout(int address1, boolean address1Switched, int address2,
                       boolean address2Switched, int bus1, int bus2,
                       SRCPTurnoutState defaultState, SRCPTurnoutTypes turnoutType) {
        super();
        this.address1 = address1;
        this.address1Switched = address1Switched;
        this.address2 = address2;
        this.address2Switched = address2Switched;
        this.bus1 = bus1;
        this.bus2 = bus2;
        this.defaultState = defaultState;
        this.turnoutType = turnoutType;
    }

    public abstract Object clone();

    public abstract boolean checkAddress();

    public boolean checkBus() {
        switch (turnoutType) {
            case DEFAULT:
            case DOUBLECROSS:
            case CUTTER:
                return (bus1 > 0);
            case THREEWAY:
                return (bus2 > 0);
        }
        return false;
    }

    public boolean checkBusAddress() {
        return (checkBus() && checkAddress());
    }

    public int getAddress1() {
        return address1;
    }

    public int getAddress2() {
        return address2;
    }

    public int getBus1() {
        return bus1;
    }

    public int getBus2() {
        return bus2;
    }

    public SRCPTurnoutState getDefaultState() {
        return defaultState;
    }

    public GA getGA() {
        return this.ga;
    }

    public String getProtocol() {
        return protocol;
    }

    public SRCPSession getSession() {
        return this.session;
    }

    protected SRCPTurnout[] getSubTurnouts() {
        return subTurnouts;
    }

    public SRCPTurnoutState getTurnoutState() {
        return turnoutState;
    }

    public SRCPTurnoutTypes getTurnoutType() {
        return turnoutType;
    }

    public boolean isAddress1Switched() {
        return address1Switched;
    }

    public boolean isAddress2Switched() {
        return address2Switched;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isThreeWay() {
        return turnoutType.equals(SRCPTurnoutTypes.THREEWAY);
    }

    public boolean isCutter() {
        return turnoutType.equals(SRCPTurnoutTypes.CUTTER);
    }

    public void setAddress1(int address1) {
        this.address1 = address1;
    }

    public void setAddress1Switched(boolean address1Switched) {
        this.address1Switched = address1Switched;
    }

    public void setAddress2(int address2) {
        this.address2 = address2;
    }

    public void setAddress2Switched(boolean address2Switched) {
        this.address2Switched = address2Switched;
    }

    public void setBus1(int bus1) {
        this.bus1 = bus1;
    }

    public void setBus2(int bus2) {
        this.bus2 = bus2;
    }

    public void setDefaultState(SRCPTurnoutState defaultState) {
        this.defaultState = defaultState;
    }

    public void setGA(GA ga) {
        this.ga = ga;
    }

    public void setInitialized(boolean init) {
        initialized = init;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    protected void setSession(SRCPSession session) {
        if (session != this.session) {
            setGA(null);
            setInitialized(false);
        }
        this.session = session;
    }

    protected void setSubTurnouts(SRCPTurnout[] subTurnouts) {
        this.subTurnouts = subTurnouts;
    }

    protected void setTurnoutState(SRCPTurnoutState switchState) {
        this.turnoutState = switchState;
    }

    public void setTurnoutType(SRCPTurnoutTypes turnoutType) {
        this.turnoutType = turnoutType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(bus1)
                .append(address1)
                .append(address1Switched)
                .append(bus2)
                .append(address2)
                .append(address2Switched)
                .build();

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SRCPTurnout)) {
            return false;
        } else {
            SRCPTurnout rhs = (SRCPTurnout) obj;
            return new EqualsBuilder().append(bus1, rhs.getBus1())
                    .append(address1, rhs.getAddress1())
                    .append(address1Switched, rhs.isAddress1Switched())
                    .append(bus2, rhs.getBus2())
                    .append(address2, rhs.getAddress2())
                    .append(address2Switched, rhs.isAddress2Switched())
                    .build();
        }
    }

    public String toString() {
        return turnoutType.toString() + " [" + bus1 + "," + address1 + "],["
                + bus2 + "," + address2 + "]";
    }
}
