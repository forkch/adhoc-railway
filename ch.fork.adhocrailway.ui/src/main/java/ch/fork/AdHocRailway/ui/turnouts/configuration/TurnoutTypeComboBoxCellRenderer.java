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

import ch.fork.AdHocRailway.model.turnouts.TurnoutType;
import ch.fork.AdHocRailway.ui.utils.ImageTools;

import javax.swing.*;
import java.awt.*;

public class TurnoutTypeComboBoxCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
        final JLabel iconLabel = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
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
        }
        return iconLabel;
    }
}
