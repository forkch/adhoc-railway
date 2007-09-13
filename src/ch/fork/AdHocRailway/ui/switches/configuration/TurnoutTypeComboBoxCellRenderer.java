/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchTypeComboBoxCellRenderer.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:14 BST 2006
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


package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.ImageTools;

public class TurnoutTypeComboBoxCellRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(56, 38));
        if (value == TurnoutTypes.DEFAULT) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/default_switch_small.png"));
        } else if (value == TurnoutTypes.DOUBLECROSS) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/double_cross_switch_small.png"));
        } else if (value == TurnoutTypes.THREEWAY) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/three_way_switch_small.png"));
        }
        return iconLabel;
    }
}
