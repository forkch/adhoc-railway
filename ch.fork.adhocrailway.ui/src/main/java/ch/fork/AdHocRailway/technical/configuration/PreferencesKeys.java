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
    public static final String LOCOMOTIVE_CONTROLES = "LocomotiveControlesAmount";
    public static final String KEYBOARD_LAYOUT = "KeyBoardLayout";
    public static final String FULLSCREEN = "Fullscreen";
    public static final String TABBED_TRACK = "TabbedTrack";
    public static final String TABLET_MODE = "TabletMode";
    public static final String LAST_OPENED_FILE = "LastOpenedFile";
    public static final String USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES = "UseFixedTurnoutAndRouteGroupSizes";
    public static final String OPEN_LAST_FILE = "OpenLastFile";
    public static final String STOP_ON_DIRECTION_CHANGE = "StopOnDirectionChange";
    public static final String AUTOSAVE = "AutoSave";

    // Digital Data
    public static final String ACTIVATION_TIME = "DefaultActivationTime";
    public static final String ROUTING_DELAY = "DefaultRoutingDelay";
    public static final String CUTTER_SLEEP_TIME = "CutterSleepTime";
    public static final String INTERFACE_6051 = "Interface6051";
    public static final String LOGGING = "WriteLog";
    public static final String DEFAULT_TURNOUT_BUS = "DefaultTurnoutBus";
    public static final String DEFAULT_LOCOMOTIVE_BUS = "DefaultLocomotiveBus";
    public static final String NUMBER_OF_BOOSTERS = "NumberOfBoosters";
    public static final String RAILWAY_DEVICE = "RailwayDevice";

    public static final String ADHOC_BRAIN_PORT = "AdHocBrainPort";
    public static final String AUTOCONNECT_TO_RAILWAY = "RailwayAutoconnect";

    // SERVERS
    public static final String AUTO_DISCOVER = "AutoDiscoverServers";

    // SRCP Server
    public static final String SRCP_HOSTNAME = "SRCPHostname";
    public static final String SRCP_PORT = "SRCPPort";

    // AdHoc-Server Server
    public static final String USE_ADHOC_SERVER = "UseAdHocServer";
    public static final String ADHOC_SERVER_HOSTNAME = "AdHocServerHostname";
    public static final String ADHOC_SERVER_PORT = "AdHocServerPort";
    public static final String ADHOC_SERVER_COLLECTION = "AdHocServerCollection";


    public static final String TURNOUT_TESTER_BY_NUMBER = "TurnoutTesterByNumber";
}
