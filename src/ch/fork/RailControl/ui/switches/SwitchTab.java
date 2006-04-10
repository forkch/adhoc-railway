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

import javax.swing.*;

import ch.fork.RailControl.domain.switches.Switch;


import java.awt.*;
import java.util.*;

public class SwitchTab extends JPanel {

	private static final long serialVersionUID = 1L;
	private ArrayList switchWidgets;

    private int maxy;
    private int currentx;
    private int currenty;
    private GridBagLayout layout;
    private GridBagConstraints gbc;

    public SwitchTab(String title) {
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
        java.util.Random rnd = new Random();
        int value = rnd.nextInt(2);
        switch (value) {
            case 0:
                aSwitchWidget.setGreen();
                break;
            default:
                aSwitchWidget.setRed();
                break;
        }
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