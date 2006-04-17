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
public class Preferences {

	private String hostname = "titan";
	private int portnumber = 12345;
	
	private int defaultActivationTime = 50;
	private int defaultRoutingDelay = 250;
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("hostname: " +  hostname + " ; ");
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
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPortnumber() {
		return portnumber;
	}
	public void setPortnumber(int portumber) {
		this.portnumber = portnumber;
	}
}
