/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <Preferences.java>  -  <>
 * 
 * begin     : Apr 10, 2006
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

package ch.fork.RailControl.domain;

import java.util.ArrayList;
import java.util.List;

public class Preferences {

	private List<String> hostnames;
	private String hostname = "localhost";
	private int portnumber = 12345;
	
	private int defaultActivationTime = 50;
	private int defaultRoutingDelay = 250;
	
	private int locomotiveControlNumber = 5;
	
	private static Preferences instance = null;
	
	private Preferences() {
		hostnames = new ArrayList<String>();
		hostnames.add("localhost");
		hostnames.add("titan");
	}
	
	public static Preferences getInstance() {
		if(instance == null) {
			instance = new Preferences();
			return instance;
		} else {
			return instance;
		}
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("hostname: " +  hostnames + " ; ");
		b.append("portnumber: " + portnumber + " ; ");
		b.append("defaultActivationTime: " + defaultActivationTime + " ; ");
		b.append("defaultRoutingDelay: " + defaultRoutingDelay + " ; ");
		return b.toString();
	}
	public int getDefaultActivationTime() {
		return defaultActivationTime;
	}
	public void setDefaultActivationTime(int defaultActivationTime) {
		this.defaultActivationTime = defaultActivationTime;
	}
	public int getDefaultRoutingDelay() {
		return defaultRoutingDelay;
	}
	public void setDefaultRoutingDelay(int defaultRoutingDelay) {
		this.defaultRoutingDelay = defaultRoutingDelay;
	}
	
	public List<String> getHostnames() {
		return hostnames;
	}
	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}
	public int getPortnumber() {
		return portnumber;
	}
	public void setPortnumber(int portnumber) {
		this.portnumber = portnumber;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getLocomotiveControlNumber() {
		return locomotiveControlNumber;
	}

	public void setLocomotiveControlNumber(int locomotiveControlNumber) {
		this.locomotiveControlNumber = locomotiveControlNumber;
	}
	
}
