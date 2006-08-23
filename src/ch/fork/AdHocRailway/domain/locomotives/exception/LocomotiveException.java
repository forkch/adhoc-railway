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


package ch.fork.AdHocRailway.domain.locomotives.exception;

import ch.fork.AdHocRailway.domain.exception.ControlException;

public class LocomotiveException extends ControlException {
    public LocomotiveException(String msg) {
        super(msg);
    }

    public LocomotiveException(String msg, Exception parent) {
        super(msg, parent);
    }
}
