/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutException.java 262 2013-03-17 20:47:56Z fork_ch $
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


public class TurnoutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -356171641042648431L;

	public TurnoutException() {
		// TODO Auto-generated constructor stub
	}

	public TurnoutException(String msg) {
		super(msg);
	}

	public TurnoutException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public TurnoutException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
