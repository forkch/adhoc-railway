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

import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.tools.ImageTools;

import javax.swing.*;
import java.awt.*;

public class TurnoutDefaultStateComboBoxCellRenderer extends
        DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(final JList<?> list,
                                                  final Object value, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
        final JLabel iconLabel = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            return iconLabel;
        }
        iconLabel.setText("");
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (value.equals(TurnoutState.STRAIGHT)) {
            iconLabel.setIcon(ImageTools
                    .createImageIconFromCustom("default_straight.png"));
        } else {
            iconLabel.setIcon(ImageTools
                    .createImageIconFromCustom("default_curved.png"));
        }
        return iconLabel;
    }
}
