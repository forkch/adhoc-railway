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

import ch.fork.AdHocRailway.ui.ImageTools;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;

public class TurnoutDefaultStateComboBoxCellRenderer extends
		DefaultListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2928955312058895462L;

	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel iconLabel = (JLabel) super.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		if (value == null) {
			return iconLabel;
		}
		iconLabel.setText("");
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		if (value.equals(SRCPTurnoutState.STRAIGHT)) {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_straight.png"));
		} else {
			iconLabel.setIcon(ImageTools
					.createImageIcon("switches/default_curved.png"));
		}
		return iconLabel;
	}
}
