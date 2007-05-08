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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.switches.canvas.Segment7;

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
