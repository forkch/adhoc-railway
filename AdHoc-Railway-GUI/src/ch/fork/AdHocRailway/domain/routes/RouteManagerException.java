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

package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.ManagerException;

public class RouteManagerException extends ManagerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6775132028782515680L;

	public RouteManagerException() {
	}

	public RouteManagerException(final String message) {
		super(message);
	}

	public RouteManagerException(final Throwable cause) {
		super(cause);
	}

	public RouteManagerException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
