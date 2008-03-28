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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	private Properties			props;
	private File				configFile;

	private Preferences() {

		// Fill default values
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
		setBooleanValue(INTERFACE_6051, true);
		setIntValue(TURNOUT_CONTROLES, 5);
		setIntValue(ROUTE_CONTROLES, 5);
		setBooleanValue(LOGGING, true);
		setBooleanValue(FULLSCREEN, false);
		setBooleanValue(AUTOCONNECT, false);
		setBooleanValue(TABBED_TRACK, true);
		setBooleanValue(USE_DATABASE, false);

		// configFile =
		// new File(System.getProperty("user.home") + File.separator
		// + ".adhocrailway.conf");
		// if (configFile.exists()) {
		// props = new Properties();
		// try {
		// logger.info("found .adhocrailway.conf in users home directory");
		// props.load(new FileInputStream(configFile));
		// for (Object key : props.keySet()) {
		// setStringValue(key.toString(), props.getProperty(
		// key.toString()).toString());
		// }
		// return;
		// } catch (FileNotFoundException e) {
		//
		// } catch (IOException e) {
		// }
		// }
		configFile = new File("./adhocrailway.conf");
		if (configFile.exists()) {
			props = new Properties();
			try {
				logger.info("found adhocrailway.conf in current directory");
				props.load(new FileInputStream(configFile));
				for (Object key : props.keySet()) {
					setStringValue(key.toString(), props.getProperty(
							key.toString()).toString());
				}
				return;
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

	public void save() throws FileNotFoundException, IOException {
		if (props == null) {
			props = new Properties();
		}
		for (String key : preferences.keySet()) {
			props.setProperty(key, preferences.get(key));
		}
		props.store(new FileOutputStream(configFile), "");
		logger.info("Preferences saved to: " + configFile.getAbsolutePath());
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
