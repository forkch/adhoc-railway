/*------------------------------------------------------------------------
 * 
 * <./domain/locking/exception/LockingException.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:50 BST 2006
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


package ch.fork.AdHocRailway.domain.locking;

import ch.fork.AdHocRailway.domain.ControlException;

public class LockingException extends ControlException {

	public LockingException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LockingException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public LockingException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LockingException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
