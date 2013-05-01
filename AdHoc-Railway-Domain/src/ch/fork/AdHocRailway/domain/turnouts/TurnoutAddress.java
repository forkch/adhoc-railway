/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutAddress.java 153 2008-03-27 17:44:48Z fork_ch $
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

package ch.fork.AdHocRailway.domain.turnouts;

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

public class TurnoutAddress {

	// Fields

	private int		address;

	private int		bus;

	private boolean	switched;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TurnoutAddress t = (TurnoutAddress) o;

		if (address != t.getAddress())
			return false;
		if (bus != t.getBus())
			return false;
		if (switched != t.isSwitched())
			return false;

		return true;
	}

	public int hashCode() {
		return address + bus;
	}

	public String toString() {

		return "[" + bus + "|" + address + "|" + switched + "]";
	}

	// Constructors

	/** default constructor */
	public TurnoutAddress() {
	}

	/** full constructor */
	public TurnoutAddress(int address, int bus, boolean switched) {
		this.address = address;
		this.bus = bus;
		this.switched = switched;
	}

	// Property accessors

	public int getAddress() {
		return this.address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getBus() {
		return this.bus;
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

	public boolean isSwitched() {
		return this.switched;
	}

	public void setSwitched(boolean switched) {
		this.switched = switched;
	}

}
