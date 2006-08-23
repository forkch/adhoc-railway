/*------------------------------------------------------------------------
 * 
 * <./ui/switches/SwitchGroupPane.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:38 BST 2006
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

import java.awt.Component;
import java.awt.Container;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;

public class SwitchGroupPane extends JTabbedPane {
    private Collection<SwitchGroup> switchGroups;
    private JFrame                  frame;

    public SwitchGroupPane(JFrame frame) {
        super(JTabbedPane.BOTTOM);
        this.frame = frame;
    }

    public void update(Collection<SwitchGroup> switchGroups) {
        this.switchGroups = switchGroups;
        SwitchControl sc = SwitchControl.getInstance();
        for (Component switchGroupTabs : getComponents()) {
            for (Component switchWidget : ((Container) switchGroupTabs)
                .getComponents())
                if (switchWidget instanceof SwitchWidget) {
                    SwitchWidget sw = (SwitchWidget) switchWidget;
                    sc.removeSwitchChangeListener(sw);
                }
        }
        this.removeAll();
        int i = 1;
        for (SwitchGroup switchGroup : switchGroups) {
            SwitchGroupTab switchGroupTab = new SwitchGroupTab(switchGroup);
            JScrollPane switchGroupPane = new JScrollPane(switchGroupTab);
            switchGroupPane.getVerticalScrollBar().setUnitIncrement(10);
            switchGroupPane.getVerticalScrollBar().setBlockIncrement(10);
            add(switchGroupPane, "F" + i + ": " + switchGroup.getName());
            for (Switch aSwitch : switchGroup.getSwitches()) {
                SwitchWidget switchWidget = new SwitchWidget(aSwitch,
                    switchGroup, frame);
                switchGroupTab.addSwitchWidget(switchWidget);
            }
            i++;
        }
    }
}
