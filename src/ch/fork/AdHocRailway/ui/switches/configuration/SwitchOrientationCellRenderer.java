/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchOrientationCellRenderer.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:16 BST 2006
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
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.domain.switches.Switch.SwitchState;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchOrientationCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        if (value.equals(SwitchState.STRAIGHT)) {
            iconLabel.setIcon(ImageTools.createStraightState(iconLabel,
                SwitchWidget.class));
        } else {
            iconLabel.setIcon(ImageTools.createCurvedState(iconLabel,
                SwitchWidget.class));
        }
        return iconLabel;
    }
}
