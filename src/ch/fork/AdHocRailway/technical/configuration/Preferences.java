/*------------------------------------------------------------------------
 * 
 * <./domain/configuration/Preferences.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:14 BST 2006
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Preferences implements PreferencesKeys {
	private static Logger		logger		= Logger
													.getLogger(Preferences.class);
	private Map<String, String>	preferences;
	private List<String>		hostnames;
	private static Preferences	instance	= null;

	private Preferences() {

		preferences = new HashMap<String, String>();
		hostnames = new ArrayList<String>();
		hostnames.add("localhost");
		setStringValue(HOSTNAME, "localhost");
		setIntValue(PORT, 12345);
		setIntValue(ACTIVATION_TIME, 50);
		setIntValue(ROUTING_DELAY, 250);
		setIntValue(LOCK_DURATION, 0);
		setIntValue(LOCOMOTIVE_CONTROLES, 4);
		setStringValue(KEYBOARD_LAYOUT, "Swiss German");
		setStringValue(INTERFACE_6051, "N");
		setIntValue(TURNOUT_CONTROLES, 5);
		setIntValue(ROUTE_CONTROLES, 5);
		setBooleanValue(LOGGING, true);
		setBooleanValue(FULLSCREEN, false);
		setBooleanValue(AUTOCONNECT, false);
		setBooleanValue(TABBED_TRACK, true);
		setBooleanValue(USE_DATABASE, false);

		File currentDirConfig = new File("./adhocrailway.conf");
		File homeDirConfig = new File(System.getProperty("user.home")
				+ File.separator + ".adhocrailway.conf");

		if (currentDirConfig.exists()) {
			Properties props = new Properties();
			try {
				logger.info("found adhocrailway.conf in current directory");
				props.load(new FileInputStream(currentDirConfig));
				for (Object key : props.keySet()) {
					setStringValue(key.toString(), props.getProperty(
							key.toString()).toString());
				}
			} catch (FileNotFoundException e) {

			} catch (IOException e) {
			}

		}
		if (homeDirConfig.exists()) {
			Properties props = new Properties();
			try {
				logger.info("found .adhocrailway.conf in users home directory");
				props.load(new FileInputStream(homeDirConfig));
				for (Object key : props.keySet()) {
					setStringValue(key.toString(), props.getProperty(
							key.toString()).toString());
				}
			} catch (FileNotFoundException e) {

			} catch (IOException e) {
			}
		}
	}

	public static Preferences getInstance() {
		if (instance == null) {
			instance = new Preferences();
			return instance;
		} else {
			return instance;
		}
	}

	public void setStringValue(String key, String value) {
		preferences.put(key, value);
	}

	public String getStringValue(String key) {
		return preferences.get(key);
	}

	public void setIntValue(String key, int value) {
		setStringValue(key, Integer.toString(value));
	}

	public int getIntValue(String key) {
		return Integer.parseInt(preferences.get(key));
	}

	public void setBooleanValue(String key, boolean value) {
		setStringValue(key, Boolean.toString(value));
	}

	public boolean getBooleanValue(String key) {
		return Boolean.parseBoolean(preferences.get(key));
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
}
