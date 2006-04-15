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

public class SwitchGroupTab extends JPanel {

	private static final long serialVersionUID = 1L;
	private ArrayList switchWidgets;

    private int maxy;
    private int currentx;
    private int currenty;
    private GridBagLayout layout;
    private GridBagConstraints gbc;

    public SwitchGroupTab() {
        this.switchWidgets = new ArrayList();
        layout = new GridBagLayout();
        setLayout(layout);
        maxy = 10;
        currentx = 0;
        currenty = 0;
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = currentx;
        gbc.gridy = currenty;
    }

    @SuppressWarnings("unchecked")
	public void addSwitchWidget(SwitchWidget aSwitchWidget) {
        switchWidgets.add(aSwitchWidget);
        if(currenty == maxy) {
            currentx ++;
            currenty = 0;
        }
        gbc.gridx = currentx;
        gbc.gridy = currenty;
        layout.setConstraints(aSwitchWidget, gbc);
        add(aSwitchWidget);
        currenty ++;
    }
    
}
