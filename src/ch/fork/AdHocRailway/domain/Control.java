/*------------------------------------------------------------------------
 * 
 * <Control.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:54:02 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
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

package ch.fork.AdHocRailway.domain;

import de.dermoba.srcp.client.SRCPSession;

/**
 * Basic Control class. Each Control should inherit from this class, so that it
 * can check the session of its associated ControlObject and if its initialized.
 * 
 * @author fork
 * 
 */
public abstract class Control {

	protected SRCPSession session = null;

	public Control() {
		
	}
}
