/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchesControlPanel.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:29 BST 2006
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

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;

public class SwitchesControlPanel extends JPanel {
    private SwitchControl   switchControl;
    private SwitchGroupPane switchGroupPane;
    
    private JFrame          frame;

    public SwitchesControlPanel(JFrame frame) {
        this.frame = frame;
        
        switchControl = SwitchControl.getInstance();
        initGUI();
    }

    public void update(Collection<SwitchGroup> switchGroups) {
        switchGroupPane.update(switchGroups);
    }

    private void initGUI() {
        setLayout(new BorderLayout(5,5));
        switchGroupPane = new SwitchGroupPane(frame);
        add(switchGroupPane, BorderLayout.CENTER);
        
    }

}
