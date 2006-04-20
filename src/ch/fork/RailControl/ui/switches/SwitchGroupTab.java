package ch.fork.RailControl.ui.switches;
/*------------------------------------------------------------------------
 * 
 * <src/TrackSwitchTab.java>  -  <desc>
 * 
 * begin     : Sun May 15 18:05:53 CEST 2005
 * copyright : (C)  by Benjamin Mueller 
 * email     : akula@akula.ch
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JPanel;

import ch.fork.RailControl.domain.switches.SwitchControl;

public class SwitchGroupTab extends JPanel {

	private static final long serialVersionUID = 1L;
	private ArrayList switchWidgets;

    private int maxCols;
    private int currentRow;
    private int currentCol;
    private GridBagLayout layout;
    private GridBagConstraints gbc;

    public SwitchGroupTab() {
        this.switchWidgets = new ArrayList();
        layout = new GridBagLayout();
        setLayout(layout);
        maxCols = 7;
        currentRow = 0;
        currentCol = 0;
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = currentRow;
        gbc.gridy = currentCol;
    }

    @SuppressWarnings("unchecked")
	public void addSwitchWidget(SwitchWidget aSwitchWidget) {
        add(aSwitchWidget);
        switchWidgets.add(aSwitchWidget);
        if(currentCol == maxCols) {
            currentRow ++;
            currentCol = 0;
        }
        gbc.gridx = currentCol;
        gbc.gridy = currentRow;
        layout.setConstraints(aSwitchWidget, gbc);
        currentCol ++;
    }
    
}
