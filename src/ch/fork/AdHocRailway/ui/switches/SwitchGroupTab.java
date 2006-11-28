/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchGroupTab.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:32 BST 2006
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


package ch.fork.AdHocRailway.ui.switches;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class SwitchGroupTab extends JPanel {
    private static final long       serialVersionUID = 1L;
    private int                     maxCols;
    private int                     currentRow;
    private int                     currentCol;
    private GridBagLayout           layout;
    private GridBagConstraints      gbc;

    public SwitchGroupTab() {
        layout = new GridBagLayout();
        setLayout(layout);
        maxCols = Preferences.getInstance().getIntValue(
            PreferencesKeys.SWITCH_CONTROLES);
        currentRow = 0;
        currentCol = 0;
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = currentRow;
        gbc.gridy = currentCol;
    }

    public void addSwitchWidget(SwitchWidget aSwitchWidget) {
        add(aSwitchWidget);
        if (currentCol == maxCols) {
            currentRow++;
            currentCol = 0;
        }
        gbc.gridx = currentCol;
        gbc.gridy = currentRow;
        layout.setConstraints(aSwitchWidget, gbc);
        currentCol++;
   }
}
