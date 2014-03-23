/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotiveManagerException.java 267 2013-03-26 22:08:52Z fork_ch $
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

package ch.fork.AdHocRailway.manager.locomotives;

import ch.fork.AdHocRailway.manager.ManagerException;

public class LocomotiveManagerException extends ManagerException {

    public LocomotiveManagerException() {
    }

    public LocomotiveManagerException(final String message) {
        super(message);
    }

    public LocomotiveManagerException(final Throwable cause) {
        super(cause);
    }

    public LocomotiveManagerException(final String message,
                                      final Throwable cause) {
        super(message, cause);
    }

}
