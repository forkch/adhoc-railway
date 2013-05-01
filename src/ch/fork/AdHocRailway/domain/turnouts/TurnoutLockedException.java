/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutLockedException.java 262 2013-03-17 20:47:56Z fork_ch $
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

public class TurnoutLockedException extends TurnoutException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1244808380520100590L;

	public TurnoutLockedException(String msg) {
		super(msg);
	}

	public TurnoutLockedException(String msg, Exception parent) {
		super(msg, parent);
	}
}
