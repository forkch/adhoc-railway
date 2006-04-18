/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchTypeCellRenderer.java>  -  <>
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

package ch.fork.RailControl.ui.switches;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.ui.ImageTools;

public class SwitchTypeCellRenderer implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel iconLabel = new JLabel();
		if (value.equals("DefaultSwitch")) {
			iconLabel.setIcon(ImageTools.createDefaultSwitch(iconLabel,
					DefaultSwitch.class));
		} else if (value.equals("DoubleCrossSwitch")) {
			iconLabel.setIcon(ImageTools.createDoubleCrossSwitch(iconLabel,
					DoubleCrossSwitch.class));
		} else if (value.equals("ThreeWaySwitch")) {
			iconLabel.setIcon(ImageTools.createThreeWaySwitch(iconLabel,
					DoubleCrossSwitch.class));
		}
		return iconLabel;
	}
}
