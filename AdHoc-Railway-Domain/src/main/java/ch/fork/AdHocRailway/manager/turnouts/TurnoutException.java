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

package ch.fork.AdHocRailway.manager.turnouts;

public class TurnoutException extends RuntimeException {


	public TurnoutException() {
	}

	public TurnoutException(final String msg) {
		super(msg);
	}

	public TurnoutException(final Throwable cause) {
		super(cause);
	}

	public TurnoutException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
