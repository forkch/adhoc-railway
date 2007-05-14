/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchGroupTab.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:32 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id: SwitchGroupTab.java 81 2006-11-28 17:47:01Z fork_ch $
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


package ch.fork.AdHocRailway.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class WidgetTab extends JPanel {
    private static final long       serialVersionUID = 1L;
    private int                     maxCols;
    private int                     currentRow;
    private int                     currentCol;
    private GridBagLayout           layout;
    private GridBagConstraints      gbc;

    public WidgetTab(int maxCols) {
		
    	this.maxCols = maxCols;
        layout = new GridBagLayout();
        setLayout(layout);
        currentRow = 0;
        currentCol = 0;
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = currentRow;
        gbc.gridy = currentCol;
    }

    public void addWidget(JPanel widget) {
        add(widget);
        if (currentCol == maxCols) {
            currentRow++;
            currentCol = 0;
        }
        gbc.gridx = currentCol;
        gbc.gridy = currentRow;
        layout.setConstraints(widget, gbc);
        currentCol++;
   }
}
