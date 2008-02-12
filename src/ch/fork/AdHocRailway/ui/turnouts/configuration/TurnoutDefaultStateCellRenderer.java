/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchDefaultStateCellRenderer.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:26 BST 2006
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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.ui.ImageTools;

public class TurnoutDefaultStateCellRenderer
		extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel iconLabel =
				(JLabel) super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		if (table.getValueAt(row, 1).equals("ThreeWay")) {
			iconLabel.setText("N/A");
			return iconLabel;
		}
		iconLabel.setText("");
		if (value.equals(TurnoutState.STRAIGHT)) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_straight.png"));
		} else {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_curved.png"));
		}
		if (isSelected) {
			iconLabel.setBackground(table.getSelectionBackground());
			iconLabel.setForeground(table.getSelectionForeground());
		} else {
			iconLabel.setBackground(table.getBackground());
			iconLabel.setForeground(table.getForeground());
		}
		return iconLabel;
	}
}
