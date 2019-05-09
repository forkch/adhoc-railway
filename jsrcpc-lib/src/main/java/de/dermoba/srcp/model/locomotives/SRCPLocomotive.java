/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPLocomotive.java,v 1.5 2011-12-19 10:32:39 andre_schenk Exp $
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
import de.dermoba.srcp.devices.GL;

public abstract class SRCPLocomotive {

	protected boolean initialized = false;

	protected SRCPLocomotiveDirection direction = SRCPLocomotiveDirection.FORWARD;

	protected int currentSpeed = 0;

	private GL gl;

	private SRCPSession session;

	protected int functionCount;

	protected boolean[] functions;

	protected String[] params;

	protected String protocol;

	protected int drivingSteps;

	protected int bus;

	protected int address;

	private double lastCommandAcknowledge;

	private SRCPLocomotive extendingLocomotive;

	private boolean preventDirectionToggle;

	public SRCPLocomotive() {
		this(0, 0);
	}

	public SRCPLocomotive(final int bus, final int address) {
		super();
		this.bus = bus;
		this.address = address;
	}

	public abstract boolean checkAddress();

	public boolean checkBus() {
		return bus > 0;
	}

	public boolean checkBusAddress() {
		return (checkBus() && checkAddress());
	}

	public int getBus() {
		return bus;
	}

	public void setBus(final int bus) {
		this.bus = bus;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(final int address) {
		this.address = address;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	protected void setCurrentSpeed(final int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public SRCPLocomotiveDirection getDirection() {
		return direction;
	}

	protected void setDirection(final SRCPLocomotiveDirection direction) {
		this.direction = direction;
	}

	public boolean[] getFunctions() {
		return functions;
	}

	public void setFunctions(final boolean[] functions) {
		if (functions.length < functionCount) {
			throw new IllegalArgumentException(
					"function count must not be smaller than " + functionCount);
		}
		this.functions = functions;
	}

	public GL getGL() {
		return this.gl;
	}

	protected void setGL(final GL gl) {
		this.gl = gl;
	}

	public SRCPSession getSession() {
		return this.session;
	}

	protected void setSession(final SRCPSession session) {
		this.gl = null;
        this.initialized = false;
		this.session = session;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(final boolean init) {
		initialized = init;
	}

	public String[] getParams() {
		return params;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getDrivingSteps() {
		return drivingSteps;
	}

	/**
	 * @return the lastCommandAcknowledge
	 */
	public double getLastCommandAcknowledge() {
		return lastCommandAcknowledge;
	}

	/**
	 * Set (usually by the {@link SRCPLocomotiveControl}) to the time stamp that
	 * the SRCP server returns as part of the command acknowledge message.
	 * 
	 * @param lastCommandAcknowledge
	 *            the lastCommandAcknowledge to set
	 */
	public void setLastCommandAcknowledge(final double lastCommandAcknowledge) {
		this.lastCommandAcknowledge = lastCommandAcknowledge;
	}

	public void setExtendingLocomotive(final SRCPLocomotive extendingLocomotive) {
		this.extendingLocomotive = extendingLocomotive;

	}

	public SRCPLocomotive getExtendingLocomotive() {
		return extendingLocomotive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + bus;
		result = prime * result
				+ ((protocol == null) ? 0 : protocol.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SRCPLocomotive other = (SRCPLocomotive) obj;
		if (address != other.address) {
			return false;
		}
		if (bus != other.bus) {
			return false;
		}
		if (protocol == null) {
			if (other.protocol != null) {
				return false;
			}
		} else if (!protocol.equals(other.protocol)) {
			return false;
		}
		return true;
	}

}
