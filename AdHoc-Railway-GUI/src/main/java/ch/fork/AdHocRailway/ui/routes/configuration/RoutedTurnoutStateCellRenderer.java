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

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.ui.tools.ImageTools;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RoutedTurnoutStateCellRenderer extends DefaultTableCellRenderer {

	private final TurnoutManager turnoutManager;

	public RoutedTurnoutStateCellRenderer(final TurnoutManager turnoutManager) {
		this.turnoutManager = turnoutManager;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8165753925040643894L;

	@Override
	public Component getTableCellRendererComponent(final JTable table,
			final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		final JLabel iconLabel = (JLabel) super.getTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		Turnout currentTurnout;
		iconLabel.setText("");
		currentTurnout = turnoutManager.getTurnoutByNumber(Integer
				.valueOf((Integer) table.getValueAt(row, 0)));
		if (currentTurnout == null) {
			return iconLabel;
		}
		final TurnoutState routedState = (TurnoutState) value;
		String stateString = "";
		switch (routedState) {
		case STRAIGHT:
			stateString = "straight";
			break;
		case LEFT:
		case RIGHT:
			stateString = "curved";
			break;
		case UNDEF:
			break;
		default:
			break;
		}
		if (currentTurnout.isDefaultLeft()) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_switch_left_"
							+ stateString + ".png"));
		} else if (currentTurnout.isDefaultRight()) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_switch_right_"
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
				iconLabel.setIcon(ImageTools
						.createImageIcon("switches/three_way_switch_left.png"));
				break;
			case RIGHT:
				iconLabel
						.setIcon(ImageTools
								.createImageIcon("switches/three_way_switch_right.png"));
				break;
			default:
				break;
			}
		}

		return iconLabel;
	}
}
