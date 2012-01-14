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
import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class TurnoutTypeComboBoxCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel iconLabel = (JLabel) super.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		TurnoutType type = (TurnoutType) value;
		iconLabel.setText("");
		if (type.getTurnoutTypeEnum() == SRCPTurnoutTypes.DEFAULT) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == SRCPTurnoutTypes.DOUBLECROSS) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/double_cross_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == SRCPTurnoutTypes.THREEWAY) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/three_way_switch_small.png"));
		} else if (type.getTurnoutTypeEnum() == SRCPTurnoutTypes.CUTTER) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/cutter_small.png"));
		}
		return iconLabel;
	}
}
