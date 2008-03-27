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

package ch.fork.AdHocRailway.technical.configuration;

public interface PreferencesKeys {

	// GUI
	public static final String	LOCOMOTIVE_CONTROLES	= "LocomotiveControlesAmount";
	public static final String	TURNOUT_CONTROLES		= "SwitchControlesAmount";
	public static final String	ROUTE_CONTROLES			= "RouteControlesAmount";
	public static final String	KEYBOARD_LAYOUT			= "KeyBoardLayout";
	public static final String	FULLSCREEN				= "Fullscreen";
	public static final String	TABBED_TRACK			= "TabbedTrack";
	public static final String	LAST_OPENED_FILE		= "LastOpenedFile";

	// Digital Data
	public static final String	ACTIVATION_TIME			= "DefaultActivationTime";
	public static final String	ROUTING_DELAY			= "DefaultRoutingDelay";
	public static final String	LOCK_DURATION			= "DefaultLockDuration";
	public static final String	INTERFACE_6051			= "Interface6051";
	public static final String	LOGGING					= "WriteLog";

	// Server
	public static final String	HOSTNAME				= "Hostname";
	public static final String	PORT					= "Port";
	public static final String	AUTOCONNECT				= "Autoconnect";

	// Database
	public static final String	USE_DATABASE			= "UseDatabase";
	public static final String	DATABASE_HOST			= "DatabaseHost";
	public static final String	DATABASE_NAME			= "DatabaseName";
	public static final String	DATABASE_USER			= "DatabaseUser";
	public static final String	DATABASE_PWD			= "DatabasePWD";
}
