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
	private static final Logger LOGGER = Logger.getLogger(Preferences.class);
	private Map<String, String> preferences;
	private List<String> hostnames;
	private static Preferences instance = null;
	private Properties props;
	private File configFile;
	private final Map<String, KeyBoardLayout> keyBoardLayouts;

	private Preferences() {

		// Fill default values
		preferences = new HashMap<String, String>();
		hostnames = new ArrayList<String>();
		hostnames.add("localhost");
		setStringValue(SRCP_HOSTNAME, "localhost");
		setIntValue(SRCP_PORT, 4303);
		setBooleanValue(SRCP_AUTOCONNECT, false);
		setStringValue(ADHOC_SERVER_HOSTNAME, "localhost");
		setIntValue(ADHOC_SERVER_PORT, 3000);
		setIntValue(ACTIVATION_TIME, 50);
		setIntValue(ROUTING_DELAY, 150);
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
				+ ";SUBTRACT:CurvedRight"
				+ ";BACK_QUOTE:NextSelected"
				+ ";F1:ToggleBooster0"
				+ ";F2:ToggleBooster1"
				+ ";F3:ToggleBooster2"
				+ ";F4:ToggleBooster3"
				+ ";F5:ToggleBooster4"
				+ ";F6:ToggleBooster5"
				+ ";F7:ToggleBooster6"
				+ ";F8:ToggleBooster7"
				+ ";ESCAPE:TurnOffAllBooster");
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
				+ ";SUBTRACT:CurvedRight"
				+ ";BACK_QUOTE:NextSelected"
				+ ";F1:ToggleBooster0"
				+ ";F2:ToggleBooster1"
				+ ";F3:ToggleBooster2"
				+ ";F4:ToggleBooster3"
				+ ";F5:ToggleBooster4"
				+ ";F6:ToggleBooster5"
				+ ";F7:ToggleBooster6"
				+ ";F8:ToggleBooster7"
				+ ";ESCAPE:TurnOffAllBooster");
		setBooleanValue(INTERFACE_6051, false);
		setBooleanValue(LOGGING, true);
		setBooleanValue(FULLSCREEN, false);
		setBooleanValue(TABBED_TRACK, true);
		setBooleanValue(USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES, false);
		setBooleanValue(OPEN_LAST_FILE, false);
		setIntValue(DEFAULT_TURNOUT_BUS, 1);
		setIntValue(DEFAULT_LOCOMOTIVE_BUS, 1);
		setBooleanValue(STOP_ON_DIRECTION_CHANGE, false);

		final boolean found = findConfigFile();
		if (!found) {
			LOGGER.info("no config file found, using default values");
		} else {
			LOGGER.info("Config file found");
			props = new Properties();
			try {
				props.load(new FileInputStream(configFile));
				for (final Object key : props.keySet()) {
					setStringValue(key.toString(),
							props.getProperty(key.toString()).toString());
				}
			} catch (final FileNotFoundException e) {
			} catch (final IOException e) {
			}
		}

		keyBoardLayouts = new HashMap<String, KeyBoardLayout>();
		final Map<KeyBoardLayout, String> pendingBaseLinks = new HashMap<KeyBoardLayout, String>();
		for (final Object key : preferences.keySet()) {
			if (key.toString().startsWith(KEYBOARD_LAYOUT + ".")) {
				final KeyBoardLayout layout = parseLayout(pendingBaseLinks,
						getStringValue(key.toString()));
				keyBoardLayouts.put(layout.getName(), layout);
			}
		}
		// Resolve references to base layouts
		for (final Map.Entry<KeyBoardLayout, String> entry : pendingBaseLinks
				.entrySet()) {
			entry.getKey().setBase(keyBoardLayouts.get(entry.getValue()));
		}
	}

	private boolean findConfigFile() {
		configFile = new File("./adhocrailway.conf");
		if (configFile.exists()) {
			LOGGER.info("found adhocrailway.conf in current directory");
			return true;
		}

		configFile = new File(System.getProperty("user.home") + File.separator
				+ ".adhocrailway.conf");
		if (configFile.exists()) {
			LOGGER.info("found .adhocrailway.conf in user home directory");
			return true;
		}
		return false;
	}

	private KeyBoardLayout parseLayout(
			final Map<KeyBoardLayout, String> pendingBaseLinks,
			final String layoutDesc) {
		final List<String> entries = new LinkedList<String>(
				Arrays.asList(layoutDesc.split(";")));
		final String layoutName = entries.remove(0);
		final KeyBoardLayout layout = new KeyBoardLayout(layoutName);
		final String layoutBase = entries.remove(0);
		if (layoutBase.length() > 0) {
			pendingBaseLinks.put(layout, layoutBase);
		}
		while (entries.size() > 0) {
			final String entry = entries.remove(0);
			final String[] pair = entry.split(":", 2);
			final KeyStroke keyStroke = KeyStroke.getKeyStroke(pair[0].trim());
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
		for (final String key : preferences.keySet()) {
			props.setProperty(key, preferences.get(key));
		}
		props.store(new FileOutputStream(configFile), "");
		LOGGER.info("Preferences saved to: " + configFile.getAbsolutePath());
	}

	public void setStringValue(final String key, final String value) {
		preferences.put(key, value);
	}

	public String getStringValue(final String key) {
		if (preferences.containsKey(key)) {
			return preferences.get(key);
		}
		return "";
	}

	public void setIntValue(final String key, final int value) {
		setStringValue(key, Integer.toString(value));
	}

	public int getIntValue(final String key) {
		if (preferences.containsKey(key)) {
			return Integer.parseInt(preferences.get(key));
		}
		return 0;
	}

	public void setBooleanValue(final String key, final boolean value) {
		setStringValue(key, Boolean.toString(value));
	}

	public boolean getBooleanValue(final String key) {
		if (preferences.containsKey(key)) {
			return Boolean.parseBoolean(preferences.get(key));
		}
		return false;
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(final List<String> hostnames) {
		this.hostnames = hostnames;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public void setPreferences(final Map<String, String> preferences) {
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Preferences other = (Preferences) obj;
		if (configFile == null) {
			if (other.configFile != null) {
				return false;
			}
		} else if (!configFile.equals(other.configFile)) {
			return false;
		}
		if (hostnames == null) {
			if (other.hostnames != null) {
				return false;
			}
		} else if (!hostnames.equals(other.hostnames)) {
			return false;
		}
		if (keyBoardLayouts == null) {
			if (other.keyBoardLayouts != null) {
				return false;
			}
		} else if (!keyBoardLayouts.equals(other.keyBoardLayouts)) {
			return false;
		}
		if (preferences == null) {
			if (other.preferences != null) {
				return false;
			}
		} else if (!preferences.equals(other.preferences)) {
			return false;
		}
		if (props == null) {
			if (other.props != null) {
				return false;
			}
		} else if (!props.equals(other.props)) {
			return false;
		}
		return true;
	}
}
