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

import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.RouteItemState;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.utils.ImageTools;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RoutedTurnoutStateCellRenderer extends DefaultTableCellRenderer {

    private final TurnoutManager turnoutManager;

    public RoutedTurnoutStateCellRenderer(final TurnoutManager turnoutManager) {
        this.turnoutManager = turnoutManager;
    }


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
        final RouteItemState routedState = (RouteItemState) value;
        String stateString = "";
        switch (routedState) {
            case STRAIGHT:
                if (currentTurnout.getDefaultState() == TurnoutState.STRAIGHT) {
                    stateString = "straight";
                } else {
                    stateString = "curved";
                }

                break;
            case LEFT:
            case RIGHT:
                if (currentTurnout.getDefaultState() == TurnoutState.STRAIGHT) {
                    stateString = "curved";
                } else {
                    stateString = "straight";
                }
                break;
            case DEFAULT:
                if (currentTurnout.getDefaultState() == TurnoutState.STRAIGHT) {
                    stateString = "straight";
                } else {
                    stateString = "curved";
                }
                break;
            case NON_DEFAULT:

                if (currentTurnout.getDefaultState() == TurnoutState.STRAIGHT) {
                    stateString = "curved";
                } else {
                    stateString = "straight";
                }
                break;
            default:
                break;
        }
        if (currentTurnout.isDefaultLeft()) {
            iconLabel.setIcon(ImageTools
                    .createImageIconFromCustom("default_switch_left_"
                            + stateString + ".png"));
        } else if (currentTurnout.isDefaultRight()) {
            iconLabel.setIcon(ImageTools
                    .createImageIconFromCustom("default_switch_right_"
                            + stateString + ".png"));
        } else if (currentTurnout.isDoubleCross()) {
            iconLabel.setIcon(ImageTools
                    .createImageIconFromCustom("double_cross_switch_"
                            + stateString + ".png"));
        } else if (currentTurnout.isThreeWay()) {
            switch (routedState) {
                case STRAIGHT:
                    iconLabel
                            .setIcon(ImageTools
                                    .createImageIconFromCustom("three_way_switch_straight.png"));
                    break;
                case LEFT:
                    iconLabel.setIcon(ImageTools
                            .createImageIconFromCustom("three_way_switch_left.png"));
                    break;
                case RIGHT:
                    iconLabel
                            .setIcon(ImageTools
                                    .createImageIconFromCustom("three_way_switch_right.png"));
                    break;
                default:
                    break;
            }
        }

        return iconLabel;
    }
}
