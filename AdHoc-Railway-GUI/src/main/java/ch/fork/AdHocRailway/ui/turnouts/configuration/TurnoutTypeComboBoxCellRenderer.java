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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.ui.ImageTools;

public class TurnoutTypeComboBoxCellRenderer extends DefaultListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8179478248689107235L;

	@Override
	public Component getListCellRendererComponent(final JList<?> list,
			final Object value, final int index, final boolean isSelected,
			final boolean cellHasFocus) {
		final JLabel iconLabel = (JLabel) super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final TurnoutType type = (TurnoutType) value;
		iconLabel.setText("");
		switch (type) {
		case DEFAULT_LEFT:
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_switch_small_left.png"));
			break;
		case DEFAULT_RIGHT:
			iconLabel
					.setIcon(ImageTools
							.createImageIcon("switches/default_switch_small_right.png"));
			break;
		case DOUBLECROSS:
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/double_cross_switch_small.png"));
			break;
		case THREEWAY:
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/three_way_switch_small.png"));
			break;
		case CUTTER:
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/cutter_small.png"));
		}
		return iconLabel;
	}
}
