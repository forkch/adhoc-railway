/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotivePersistenceException.java 176 2008-04-24 18:38:00Z fork_ch $
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

public class PersistenceException extends AdHocRailwayException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2345053574504535357L;

	public PersistenceException() {
		// TODO Auto-generated constructor stub
	}

	public PersistenceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public PersistenceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
