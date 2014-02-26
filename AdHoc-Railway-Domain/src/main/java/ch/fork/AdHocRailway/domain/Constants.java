/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Constants.java,v 1.1 2008/04/24 06:19:05 fork_ch Exp $
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

/**
 * Some constants used by everything.
 *
 * @author fork
 */
public interface Constants {
    public static final String ERR_INIT_FAILED = "Init failed";

    public static final String ERR_TERM_FAILED = "Term failed";

    public static final String ERR_NO_SESSION = "No session";

    public static final String ERR_NOT_CONNECTED = "Not connected";

    public static final String ERR_LOCKED = "Device locked";

    public static final String ERR_FAILED = "Command failed";

    public static final String ERR_INVALID_ADDRESS = "Invalid address";

    public static final String ERR_TOGGLE_FAILED = "Toggle of switch failed";

    public static final int DEFAULT_ACTIVATION_TIME = 50;

    public static final boolean INTERFACE_6051_CONNECTED = false;

    public static final int DEFAULT_LOCK_DURATION = 0;

    public static final String ERR_VERSION_NOT_SUPPORTED = "Version not supported";

    public static final int DEFAULT_BUS = 1;
}
