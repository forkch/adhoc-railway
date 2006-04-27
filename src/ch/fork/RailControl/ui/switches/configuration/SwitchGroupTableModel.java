/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchGroupTableModel.java>  -  <>
 * 
 * begin     : Apr 11, 2006
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

package ch.fork.RailControl.ui.switches.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.Switch.SwitchOrientation;
import ch.fork.RailControl.domain.switches.Switch.SwitchState;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.ui.ExceptionProcessor;
public class SwitchGroupTableModel extends AbstractTableModel {

	private final String[] columnNames = {"Switch #", "Type", "Bus", "Address",
			"Default State", "Orientation", "Desc"};
	private SwitchGroup switchGroup;

	public SwitchGroupTableModel() {
		super();
	}

	public SwitchGroupTableModel(SwitchGroup switchGroup) {
		super();
		this.switchGroup = switchGroup;
	}

	public int getRowCount() {
		if (switchGroup == null) {
			return 0;
		}
		return switchGroup.getSwitches().size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (switchGroup == null) {
			return null;
		}
		
		List<Switch> switches = new ArrayList(switchGroup.getSwitches());
		Switch switchOfThisRow = switches.get(rowIndex);
		switch (columnIndex) {
			case 0 :
				return switchOfThisRow.getNumber();
			case 1 :
				return switchOfThisRow.getType();
			case 2 :
				return switchOfThisRow.getBus();
			case 3 :
				return switchOfThisRow.getAddress();
			case 4 :
				return switchOfThisRow.getDefaultState();
			case 5 :
				return switchOfThisRow.getSwitchOrientation();
			case 6 :
				return switchOfThisRow.getDesc();
			default :
				return null;
		}
	}
	public boolean isCellEditable(int row, int col) {
		List<Switch> switches = new ArrayList(switchGroup.getSwitches());
		Switch switchOfThisRow = switches.get(row);
		if(col == 4 && switchOfThisRow.getType().equals("ThreeWaySwitch")) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void setValueAt(Object value, int row, int col) {
		if (switchGroup == null) {
			return;
		}
		ArrayList arrayList = new ArrayList(switchGroup.getSwitches());
		List<Switch> switches = arrayList;
		Switch switchOfThisRow = switches.get(row);
		switch (col) {
			case 0 :
				switchOfThisRow.setNumber(Integer.parseInt((String) value));
				break;
			case 1 :
				Switch tmp = switchOfThisRow;
				if (value.equals("DefaultSwitch")) {
					switchOfThisRow = new DefaultSwitch(tmp.getNumber(), tmp
							.getDesc(), tmp.getBus(), tmp.getAddress());
				} else if (value.equals("DoubleCrossSwitch")) {
					switchOfThisRow = new DoubleCrossSwitch(tmp.getNumber(),
							tmp.getDesc(), tmp.getBus(), tmp.getAddress());
				} else if (value.equals("ThreeWaySwitch")) {
					switchOfThisRow = new ThreeWaySwitch(tmp.getNumber(), tmp
							.getDesc(), tmp.getBus(), tmp.getAddress());
				}
				switchOfThisRow.setSession(tmp.getSession());
				switchGroup.replaceSwitch(tmp, switchOfThisRow);
				tmp = null;
				break;
			case 2 :
				switchOfThisRow.setBus(Integer.parseInt((String) value));
				break;
			case 3 :
				try {
					switchOfThisRow.setAddress((Address)value);
				} catch (SwitchException e) {
					ExceptionProcessor.getInstance().processException(e);
				}
				break;
			case 4 :
					switchOfThisRow.setDefaultState((SwitchState)value);
				break;
			case 5 :
				switchOfThisRow.setSwitchOrientation((SwitchOrientation) value);
				break;
			case 6 :
				switchOfThisRow.setDesc((String) value);
				break;
			default :
		}
		fireTableCellUpdated(row, col);
	}

	public SwitchGroup getSwitchGroup() {
		return switchGroup;
	}

	public void setSwitchGroup(SwitchGroup switchGroup) {
		this.switchGroup = switchGroup;
	}

}
