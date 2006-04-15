/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <Locomotive.java>  -  <>
 * 
 * begin     : Apr 8, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
 * language  : java
 * version   : $Id$
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

package ch.fork.RailControl.domain.locomotives;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;

public abstract class Locomotive {
	private String name;
	private int address;
	private int bus;
	private int direction;
	private final int PROTOCOL_VERSION = 2;
	private final String PROTOCOL = "M";
	private int drivingSteps;
	private int currentSpeed;

	private SRCPSession session;
	private GL gl;

	public Locomotive(SRCPSession session, String name, int bus, int address, int drivingSteps)
			throws SRCPException {
		this.session = session;
		this.name = name;
		this.bus = bus;
		this.address = address;
		this.drivingSteps = drivingSteps;
		gl = new GL(session);
		String[] params = new String[3];
		params[0] = Integer.toString(PROTOCOL_VERSION);
		params[1] = Integer.toString(drivingSteps);
		params[2] = Integer.toString(0);
		
		//gl.init(bus, address, PROTOCOL, params);
		//gl.get();
        //TODO: immediately a get to determine state (direction)!!!!
	}

	public void setSpeed(double speedInPercent) throws SRCPException {
		int newSpeed = (int)(drivingSteps * speedInPercent);
		gl.set(Integer.toString(direction),newSpeed, drivingSteps, null);
		currentSpeed = newSpeed;
		gl.get();
	}
	
	public void increaseSpeed() throws SRCPException {
		int newSpeed = currentSpeed +1;
		gl.set(Integer.toString(direction),newSpeed, drivingSteps, null);
		currentSpeed ++;
	}
}