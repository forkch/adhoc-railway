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

package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ImageTools;

public class RoutedTurnoutStateCellRenderer extends DefaultTableCellRenderer {
	private static TurnoutPersistenceIface	persistence	= AdHocRailway
																.getInstance()
																.getTurnoutPersistence();

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel iconLabel = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		Turnout currentTurnout;
		iconLabel.setText("");
		try {
			currentTurnout = persistence.getTurnoutByNumber(Integer
					.valueOf((Integer) table.getValueAt(row, 0)));
			TurnoutState routedState = (TurnoutState) value;
			String stateString = "";
			switch (routedState) {
			case STRAIGHT:
				stateString = "straight";
				break;
			case LEFT:
			case RIGHT:
				stateString = "curved";
				break;
			}
			if (currentTurnout.isDefault()) {
				iconLabel.setIcon(ImageTools
						.createImageIcon("switches/default_switch_"
								+ stateString + ".png"));
			} else if (currentTurnout.isDoubleCross()) {
				iconLabel.setIcon(ImageTools
						.createImageIcon("switches/double_cross_switch_"
								+ stateString + ".png"));
			} else if (currentTurnout.isThreeWay()) {
				switch (routedState) {
				case STRAIGHT:
					iconLabel
							.setIcon(ImageTools
									.createImageIcon("switches/three_way_switch_straight.png"));
					break;
				case LEFT:
					iconLabel
							.setIcon(ImageTools
									.createImageIcon("switches/three_way_switch_left.png"));
					break;
				case RIGHT:
					iconLabel
							.setIcon(ImageTools
									.createImageIcon("switches/three_way_switch_right.png"));
					break;
				}
			}
		} catch (TurnoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return iconLabel;
	}
}
