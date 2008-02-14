/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/exception/LocomotiveException.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:55:05 BST 2006
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


package ch.fork.AdHocRailway.domain.locomotives;

import ch.fork.AdHocRailway.domain.ControlException;

public class LocomotiveException extends ControlException {

	public LocomotiveException() {
		// TODO Auto-generated constructor stub
	}

	public LocomotiveException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LocomotiveException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LocomotiveException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
