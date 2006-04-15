/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <Address.java>  -  <>
 * 
 * begin     : Apr 14, 2006
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

package ch.fork.RailControl.domain.switches;

import java.util.StringTokenizer;

public class Address {

	private int address1;
	private int address2;


	public Address(int address1) {
		this(address1, 0);
	}
	public Address(int address1, int address2) {
		this.address1 = address1;
		this.address2 = address2;
	}

	public Address(String address) {
		StringTokenizer token = new StringTokenizer(address, ",");
		address1 = Integer.parseInt(token.nextToken().trim());
		address2 = Integer.parseInt(token.nextToken().trim());
	}
	public int getAddress1() {
		return address1;
	}

	public void setAddress1(int address1) {
		this.address1 = address1;
	}

	public int getAddress2() {
		return address2;
	}

	public void setAddress2(int address2) {
		this.address2 = address2;
	}
	public boolean equals(Address address) {
		if(address.getAddress1() == address1 && address.getAddress2() == address2) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return address1 + ", " + address2;
	}

}
