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

package ch.fork.adhocrailway.ui.turnouts.configuration;

import ch.fork.adhocrailway.model.turnouts.TurnoutType;
import ch.fork.adhocrailway.ui.utils.ImageTools;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TurnoutTypeCellRenderer extends DefaultTableCellRenderer {


    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        final JLabel iconLabel = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        final TurnoutType type = (TurnoutType) value;
        iconLabel.setText("");
        switch (type) {
            case DEFAULT_LEFT:
                iconLabel.setIcon(ImageTools
                        .createImageIconFromCustom("default_switch_small_left.png"));
                break;
            case DEFAULT_RIGHT:
                iconLabel
                        .setIcon(ImageTools
                                .createImageIconFromCustom("default_switch_small_right.png"));
                break;
            case DOUBLECROSS:
                iconLabel.setIcon(ImageTools
                        .createImageIconFromCustom("double_cross_switch_small.png"));
                break;
            case THREEWAY:
                iconLabel.setIcon(ImageTools
                        .createImageIconFromCustom("three_way_switch_small.png"));
                break;
            case CUTTER:
                iconLabel.setIcon(ImageTools
                        .createImageIconFromCustom("cutter_small.png"));
                break;
            default:
                break;
        }

        return iconLabel;
    }
}
