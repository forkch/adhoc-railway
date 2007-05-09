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

import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;

public class SwitchGroupPane extends JTabbedPane {
	private JFrame frame;

	public SwitchGroupPane(JFrame frame) {
		super(JTabbedPane.BOTTOM);
		this.frame = frame;
	}

	public void update(Collection<SwitchGroup> switchGroups) {
		SwitchControl sc = SwitchControl.getInstance();
		sc.removeAllSwitchChangeListener();

		this.removeAll();
		int i = 1;
		for (SwitchGroup switchGroup : switchGroups) {
			SwitchGroupTab switchGroupTab = new SwitchGroupTab();
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
