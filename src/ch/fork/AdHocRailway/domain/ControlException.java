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

package ch.fork.AdHocRailway.domain;

public class ControlException extends RuntimeException {

	public ControlException() {
		super();
	}

	public ControlException(String message) {
		super(message);
	}

	public ControlException(String message, Throwable cause) {
		super(message, cause);
	}

	public ControlException(Throwable cause) {
		super(cause);
	}

}
