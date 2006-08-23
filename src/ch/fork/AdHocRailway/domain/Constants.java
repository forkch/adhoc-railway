/*------------------------------------------------------------------------
 * 
 * <Constants.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:53:57 BST 2006
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


package ch.fork.AdHocRailway.domain;

/**
 * Some constants used by everything.
 * 
 * @author fork
 * 
 */
public interface Constants {
    public static final String ERR_INIT_FAILED           = "Init failed";
    public static final String ERR_TERM_FAILED           = "Term failed";
    public static final String ERR_REINIT_FAILED         = "ReInit failed";
    public static final String ERR_NO_SESSION            = "No session";
    public static final String ERR_NOT_CONNECTED         = "Not connected";
    public static final String ERR_LOCKED                = "Device locked";
    public static final String ERR_FAILED                = "Command failed";
    public static final String ERR_INVALID_ADDRESS       = "Invalid address";


    public static final String ERR_VERSION_NOT_SUPPORTED = "Version not supported";

    public static final int    DEFAULT_BUS               = 1;
}
