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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.ImageTools;

public class TurnoutTypeCellRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel iconLabel = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		TurnoutType type = (TurnoutType) value;
		iconLabel.setText("");
		if (type.getTurnoutTypeEnum() == TurnoutTypes.DEFAULT) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == TurnoutTypes.DOUBLECROSS) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/double_cross_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == TurnoutTypes.THREEWAY) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/three_way_switch_small.png"));
		}

		return iconLabel;
	}
}
