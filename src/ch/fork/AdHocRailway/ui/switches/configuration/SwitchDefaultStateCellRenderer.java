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


package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.ui.ImageTools;

public class SwitchDefaultStateCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (table.getValueAt(row, 1).equals("ThreeWaySwitch")) {
            iconLabel.setText("N/A");
            return iconLabel;
        }
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
