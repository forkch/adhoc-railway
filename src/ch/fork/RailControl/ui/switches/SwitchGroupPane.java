/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchGroupControl.java>  -  <>
 * 
 * begin     : Apr 15, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
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

package ch.fork.RailControl.ui.switches;

import java.util.List;

import javax.swing.JTabbedPane;

import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;
public class SwitchGroupPane extends JTabbedPane {

	private List<SwitchGroup> switchGroups;
	public SwitchGroupPane(List<SwitchGroup> switchGroups) {
		super(JTabbedPane.BOTTOM);
		this.switchGroups = switchGroups;
	}
	
	public void update(List<SwitchGroup> switchGroups) {
		this.switchGroups = switchGroups;
		this.removeAll();
		for(SwitchGroup switchGroup : switchGroups) {
			SwitchGroupTab switchGroupTab = new SwitchGroupTab(switchGroup);
			add(switchGroupTab, switchGroup.getName());
			for(Switch aSwitch : switchGroup.getSwitches()) {
				SwitchWidget switchWidget = new SwitchWidget(aSwitch);
				switchGroupTab.addSwitchWidget(switchWidget);
			}
		}
		revalidate();
		repaint();
	}
}