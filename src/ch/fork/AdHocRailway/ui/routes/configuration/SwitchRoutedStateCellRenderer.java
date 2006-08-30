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


package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchRoutedStateCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        if (table.getValueAt(row, 1).equals("ThreeWaySwitch")) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/default_straight.png", "Default Switch",
                SwitchWidget.class));
        }
        if (value.equals(SwitchState.STRAIGHT)) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/default_straight.png", "Default Switch",
                SwitchWidget.class));
        } else {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/default_curved.png", "Default Switch",
                SwitchWidget.class));
        }
        return iconLabel;
    }
}
