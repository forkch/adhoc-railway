/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <DoubleCrossSwitch.java>  -  <>
 * 
 * begin     : Apr 15, 2006
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


public class DoubleCrossSwitch extends DefaultSwitch {

	public DoubleCrossSwitch(int pNumber, String pDesc) {
		super(pNumber, pDesc);
	}
	
	public DoubleCrossSwitch(int pNumber, String pDesc, int pBus, Address pAddress) {
		super(pNumber, pDesc, pBus, pAddress);
	}
	
}
