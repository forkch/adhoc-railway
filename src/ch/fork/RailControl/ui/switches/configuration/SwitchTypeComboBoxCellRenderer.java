/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchTypeComboBoxCellRenderer.java>  -  <>
 * 
 * begin     : Apr 15, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
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

package ch.fork.RailControl.ui.switches.configuration;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ch.fork.RailControl.ui.ImageTools;
import ch.fork.RailControl.ui.switches.SwitchWidget;
public class SwitchTypeComboBoxCellRenderer implements ListCellRenderer {

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(new Dimension(56,38));
		if (value.equals("DefaultSwitch")) {
			iconLabel.setIcon(ImageTools.createDefaultSwitch(iconLabel,
					SwitchWidget.class));
		} else if (value.equals("DoubleCrossSwitch")) {
			iconLabel.setIcon(ImageTools.createDoubleCrossSwitch(iconLabel,
					SwitchWidget.class));
		} else if (value.equals("ThreeWaySwitch")) {
			iconLabel.setIcon(ImageTools.createThreeWaySwitch(iconLabel,
					SwitchWidget.class));
		}
		return iconLabel;

	}
}
