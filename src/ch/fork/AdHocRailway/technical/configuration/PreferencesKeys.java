/*------------------------------------------------------------------------
 * 
 * <./domain/configuration/PreferencesKeys.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:37 BST 2006
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

package ch.fork.AdHocRailway.technical.configuration;

public interface PreferencesKeys {

    // GUI
    public static final String LOCOMOTIVE_CONTROLES = "LocomotiveControlesAmount";
    public static final String SWITCH_CONTROLES     = "SwitchControlesAmount";
    public static final String KEYBOARD_LAYOUT      = "KeyBoardLayout";
    public static final String FULLSCREEN           = "Fullscreen";
    // Digital Data
    public static final String ACTIVATION_TIME      = "DefaultActivationTime";
    public static final String ROUTING_DELAY        = "DefaultRoutingDelay";
    public static final String LOCK_DURATION        = "DefaultLockDuration";
    public static final String INTERFACE_6051       = "Interface6051";
    public static final String LOGGING              = "WriteLog";

    // Server
    public static final String HOSTNAME             = "Hostname";
    public static final String PORT                 = "Port";
    public static final String AUTOCONNECT          = "Autoconnect";
}
