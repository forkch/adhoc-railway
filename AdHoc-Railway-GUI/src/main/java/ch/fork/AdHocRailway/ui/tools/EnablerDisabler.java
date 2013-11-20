/**
 * Diese Klasse ermoeglicht es ganze Swing-Componenten-Baeume zu
 * deaktivieren/aktivieren.
 * 
 * Version 0.1 23.01.2008
 * 
 * Copyright (C) Siemens Schweiz AG 2008, All Rights Reserved, Confidential
 */
package ch.fork.AdHocRailway.ui.tools;

import java.awt.*;

/**
 * Diese Klasse ermoeglicht es ganze Swing-Componenten-Baeume zu
 * deaktivieren/aktivieren.
 * 
 * @author Benjamin Mueller <benjamin.b.mueller@siemens.com>
 * 
 */
public class EnablerDisabler {

	public static void setEnable(boolean enable, Component comp) {
		comp.setEnabled(enable);
		if (comp instanceof Container) {
			for (Component child : ((Container) comp).getComponents()) {
				setEnable(enable, child);
			}
		}
	}
}
