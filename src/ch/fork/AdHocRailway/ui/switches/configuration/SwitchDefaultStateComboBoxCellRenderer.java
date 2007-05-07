/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchDefaultStateComboBoxCellRenderer.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:18 BST 2006
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

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.ui.ImageTools;

public class SwitchDefaultStateComboBoxCellRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //iconLabel.setPreferredSize(new Dimension(150, 38));
        if (value.equals(SwitchState.STRAIGHT)) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/default_straight.png"));
        } else {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "switches/default_curved.png"));
        }
        return iconLabel;
    }
}
