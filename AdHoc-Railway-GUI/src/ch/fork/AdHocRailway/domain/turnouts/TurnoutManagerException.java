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

package ch.fork.AdHocRailway.domain.turnouts;

import ch.fork.AdHocRailway.domain.ManagerException;

public class TurnoutManagerException extends ManagerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1593444055818073341L;

	public TurnoutManagerException() {
	}

	public TurnoutManagerException(final String message) {
		super(message);
	}

	public TurnoutManagerException(final Throwable cause) {
		super(cause);
	}

	public TurnoutManagerException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
