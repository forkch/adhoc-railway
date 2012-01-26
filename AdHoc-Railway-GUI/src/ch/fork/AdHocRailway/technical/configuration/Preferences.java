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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

public class Preferences implements PreferencesKeys {
	private static Logger logger = Logger.getLogger(Preferences.class);
	private Map<String, String> preferences;
	private List<String> hostnames;
	private static Preferences instance = null;
	private Properties props;
	private File configFile;
	private Map<String, KeyBoardLayout> keyBoardLayouts;

	private Preferences() {

		// Fill default values
		preferences = new HashMap<String, String>();
		hostnames = new ArrayList<String>();
		hostnames.add("localhost");
		setStringValue(HOSTNAME, "localhost");
		setIntValue(PORT, 4303);
		setIntValue(ACTIVATION_TIME, 50);
		setIntValue(ROUTING_DELAY, 150);
		setIntValue(LOCK_DURATION, 0);
		setIntValue(LOCOMOTIVE_CONTROLES, 4);
		setStringValue(KEYBOARD_LAYOUT, "Swiss German");
		setStringValue(KEYBOARD_LAYOUT + ".de_ch", "Swiss German" // Display
																	// name
				+ ";" // Base layout none
				+ ";SPACE:LocomotiveStop"
				+ ";A:Accelerate0;Y:Deccelerate0;Q:ToggleDirection0"
				+ ";S:Accelerate1;X:Deccelerate1;W:ToggleDirection1"
				+ ";D:Accelerate2;C:Deccelerate2;E:ToggleDirection2"
				+ ";F:Accelerate3;V:Deccelerate3;R:ToggleDirection3"
				+ ";G:Accelerate4;B:Deccelerate4;T:ToggleDirection4"
				+ ";H:Accelerate5;N:Deccelerate5;Z:ToggleDirection5"
				+ ";J:Accelerate6;M:Deccelerate6;U:ToggleDirection6"
				+ ";K:Accelerate7;COMMA:Deccelerate7;I:ToggleDirection7"
				+ ";L:Accelerate8;DECIMAL:Deccelerate8;O:ToggleDirection8"
				+ ";COLON:Accelerate9;MINUS:Deccelerate9;P:ToggleDirection9"
				+ ";PERIOD:RouteNumberEntered"
				+ ";DECIMAL:RouteNumberEntered"
				+ ";BACK_SLASH:EnableRoute"
				+ ";ENTER:DisableRoute"
				+ ";ADD:EnableRoute"
				+ ";BACK_SPACE:EnableRoute"
				+ ";DIVIDE:CurvedLeft"
				+ ";MULTIPLY:Straight"
				+ ";SUBTRACT:CurvedRight" + ";BACK_QUOTE:NextSelected");
		setStringValue(KEYBOARD_LAYOUT + ".en", "English" // Display name
				+ ";" // Base layout none
				+ ";SPACE:LocomotiveStop"
				+ ";A:Accelerate0;Z:Deccelerate0;Q:ToggleDirection0"
				+ ";S:Accelerate1;X:Deccelerate1;W:ToggleDirection1"
				+ ";D:Accelerate2;C:Deccelerate2;E:ToggleDirection2"
				+ ";F:Accelerate3;V:Deccelerate3;R:ToggleDirection3"
				+ ";G:Accelerate4;B:Deccelerate4;T:ToggleDirection4"
				+ ";H:Accelerate5;N:Deccelerate5;Y:ToggleDirection5"
				+ ";J:Accelerate6;M:Deccelerate6;U:ToggleDirection6"
				+ ";K:Accelerate7;COMMA:Deccelerate7;I:ToggleDirection7"
				+ ";L:Accelerate8;DECIMAL:Deccelerate8;O:ToggleDirection8"
				+ ";COLON:Accelerate9;MINUS:Deccelerate9;P:ToggleDirection9"
				+ ";PERIOD:RouteNumberEntered"
				+ ";DECIMAL:RouteNumberEntered"
				+ ";BACK_SLASH:EnableRoute"
				+ ";ENTER:DisableRoute"
				+ ";ADD:EnableRoute"
				+ ";BACK_SPACE:EnableRoute"
				+ ";DIVIDE:CurvedLeft"
				+ ";MULTIPLY:Straight"
				+ ";SUBTRACT:CurvedRight" + ";BACK_QUOTE:NextSelected");
		setBooleanValue(INTERFACE_6051, false);
		setIntValue(TURNOUT_CONTROLES, 5);
		setIntValue(ROUTE_CONTROLES, 5);
		setBooleanValue(LOGGING, true);
		setBooleanValue(FULLSCREEN, false);
		setBooleanValue(AUTOCONNECT, false);
		setBooleanValue(TABBED_TRACK, true);
		setBooleanValue(USE_DATABASE, false);
		setBooleanValue(USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES, false);
		setBooleanValue(OPEN_LAST_FILE, false);
		setIntValue(DEFAULT_TURNOUT_BUS, 1);
		setIntValue(DEFAULT_LOCOMOTIVE_BUS, 1);
		setBooleanValue(STOP_ON_DIRECTION_CHANGE, false);

		boolean found = findConfigFile();
		if (!found)
			logger.info("no config file found, using default values");
		else {
			logger.info("Config file found");
			props = new Properties();
			try {
				props.load(new FileInputStream(configFile));
				for (Object key : props.keySet()) {
					setStringValue(key.toString(),
							props.getProperty(key.toString()).toString());
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}

		keyBoardLayouts = new HashMap<String, KeyBoardLayout>();
		Map<KeyBoardLayout, String> pendingBaseLinks = new HashMap<KeyBoardLayout, String>();
		for (Object key : preferences.keySet()) {
			if (key.toString().startsWith(KEYBOARD_LAYOUT + ".")) {
				KeyBoardLayout layout = parseLayout(pendingBaseLinks,
						getStringValue(key.toString()));
				keyBoardLayouts.put(layout.getName(), layout);
			}
		}
		// Resolve references to base layouts
		for (Map.Entry<KeyBoardLayout, String> entry : pendingBaseLinks
				.entrySet()) {
			entry.getKey().setBase(keyBoardLayouts.get(entry.getValue()));
		}
	}

	private boolean findConfigFile() {
		configFile = new File("./adhocrailway.conf");
		if (configFile.exists()) {
			logger.info("found adhocrailway.conf in current directory");
			return true;
		}

		configFile = new File(System.getProperty("user.home") + File.separator
				+ ".adhocrailway.conf");
		if (configFile.exists()) {
			logger.info("found .adhocrailway.conf in user home directory");
			return true;
		}
		return false;
	}

	private KeyBoardLayout parseLayout(
			Map<KeyBoardLayout, String> pendingBaseLinks, String layoutDesc) {
		List<String> entries = new LinkedList<String>(Arrays.asList(layoutDesc
				.split(";")));
		String layoutName = entries.remove(0);
		KeyBoardLayout layout = new KeyBoardLayout(layoutName);
		String layoutBase = entries.remove(0);
		if (layoutBase.length() > 0) {
			pendingBaseLinks.put(layout, layoutBase);
		}
		while (entries.size() > 0) {
			String entry = entries.remove(0);
			String[] pair = entry.split(":", 2);
			KeyStroke keyStroke = KeyStroke.getKeyStroke(pair[0].trim());
			layout.addEntry(keyStroke, pair[1].trim());
		}
		return layout;
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

	public File getConfigFile() {
		return configFile;
	}

	public Set<String> getKeyBoardLayoutNames() {
		return keyBoardLayouts.keySet();
	}

	public KeyBoardLayout getKeyBoardLayout() {
		return keyBoardLayouts
				.get(getStringValue(PreferencesKeys.KEYBOARD_LAYOUT));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configFile == null) ? 0 : configFile.hashCode());
		result = prime * result
				+ ((hostnames == null) ? 0 : hostnames.hashCode());
		result = prime * result
				+ ((keyBoardLayouts == null) ? 0 : keyBoardLayouts.hashCode());
		result = prime * result
				+ ((preferences == null) ? 0 : preferences.hashCode());
		result = prime * result + ((props == null) ? 0 : props.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Preferences other = (Preferences) obj;
		if (configFile == null) {
			if (other.configFile != null)
				return false;
		} else if (!configFile.equals(other.configFile))
			return false;
		if (hostnames == null) {
			if (other.hostnames != null)
				return false;
		} else if (!hostnames.equals(other.hostnames))
			return false;
		if (keyBoardLayouts == null) {
			if (other.keyBoardLayouts != null)
				return false;
		} else if (!keyBoardLayouts.equals(other.keyBoardLayouts))
			return false;
		if (preferences == null) {
			if (other.preferences != null)
				return false;
		} else if (!preferences.equals(other.preferences))
			return false;
		if (props == null) {
			if (other.props != null)
				return false;
		} else if (!props.equals(other.props))
			return false;
		return true;
	}
}
