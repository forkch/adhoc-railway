/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <DeltaLocomotive.java>  -  <>
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

public class DeltaLocomotive extends Locomotive {
	
	private static final int DRIVING_STEPS = 14;

	public DeltaLocomotive(SRCPSession session, String name, int bus,
			int address) throws SRCPException {
		super(session, name, bus, address, DRIVING_STEPS);
		// TODO Auto-generated constructor stub
	}

}
