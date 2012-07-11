/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

package ch.fork.AdHocRailway.domain;

/**
 * An Address of a control object consists of a bus and an address. Additionally
 * the port of this address can be 'turned' by the software (usefull for
 * switches)
 * 
 * @author fork
 * 
 */
public class Address {
	private int		bus;
	private int		address;
	private boolean	addressSwitched	= false;

	public Address(int bus, int address) {
		this.bus = bus;
		this.address = address;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(bus).hashCode() * 1000
				+ Integer.valueOf(address).hashCode();
	}

	@Override
	public boolean equals(Object anAddress) {
		if (anAddress instanceof Address) {
			Address ad = (Address) anAddress;
			if (ad.bus == bus && ad.address == address
					&& ad.addressSwitched == addressSwitched) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return " bus: " + bus + " address: " + address + " : "
				+ addressSwitched;
	}

	public boolean isAddressSwitched() {
		return addressSwitched;
	}

	public void setAddressSwitched(boolean addressSwitched) {
		this.addressSwitched = addressSwitched;
	}

	public int getBus() {
		return bus;
	}

	public void setBus(int bus) {
		this.bus = bus;
	}

}
