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

package ch.fork.AdHocRailway.domain.locomotives;

public class LocomotivePersistenceException extends RuntimeException {

	public LocomotivePersistenceException() {
	}

	public LocomotivePersistenceException(String message) {
		super(message);
	}

	public LocomotivePersistenceException(Throwable cause) {
		super(cause);
	}

	public LocomotivePersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
