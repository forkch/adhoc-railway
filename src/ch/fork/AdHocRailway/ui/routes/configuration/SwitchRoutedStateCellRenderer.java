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
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchRoutedStateCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Switch currentSwitch = (Switch) table.getValueAt(row, 0);
        SwitchState routedState = (SwitchState) value;
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
        if (currentSwitch instanceof DefaultSwitch) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/default_switch_" + stateString + ".png",
                "Default Switch", SwitchWidget.class));
        } else if (currentSwitch instanceof DoubleCrossSwitch) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/double_cross_switch_" + stateString + ".png",
                "Double Cross Switch", SwitchWidget.class));
        } else if (currentSwitch instanceof ThreeWaySwitch) {
            switch (routedState) {
            case STRAIGHT:
                iconLabel.setIcon(ImageTools.createImageIcon(
                    "icons/three_way_switch_straight.png", "Threeway Switch",
                    SwitchWidget.class));
                break;
            case LEFT:
                iconLabel.setIcon(ImageTools.createImageIcon(
                    "icons/three_way_switch_left.png", "Threeway Switch",
                    SwitchWidget.class));
                break;
            case RIGHT:
                iconLabel.setIcon(ImageTools.createImageIcon(
                    "icons/three_way_switch_right.png", "Threeway Switch",
                    SwitchWidget.class));
                break;
            }
        }
        return iconLabel;
    }
}
