/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteException.java 262 2013-03-17 20:47:56Z fork_ch $
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


public class RouteException extends RuntimeException{

	public RouteException() {
	}

	public RouteException(String message) {
		super(message);
	}

	public RouteException(String message, Throwable cause) {
		super(message, cause);
	}

	public RouteException(Throwable cause) {
		super(cause);
	}

}
