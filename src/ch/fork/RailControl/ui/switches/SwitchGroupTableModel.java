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

package ch.fork.RailControl.ui.switches;

import javax.swing.table.AbstractTableModel;

import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;
public class SwitchGroupTableModel extends AbstractTableModel {

	private final String[] columnNames = {"Switch #", "Type", "Address", "Bus",
			"Desc"};
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
		Switch switchOfThisRow = switchGroup.getSwitches().get(rowIndex);
		switch (columnIndex) {
			case 0 :
				return switchOfThisRow.getNumber();
			case 1 :
				return switchOfThisRow.getType();
			case 2 :
				return switchOfThisRow.getAddress();
			case 3 :
				return switchOfThisRow.getBus();
			case 4 :
				return switchOfThisRow.getDesc();
			default :
				return null;
		}
	}
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public void setValueAt(Object value, int row, int col) {
		if (switchGroup == null) {
			return;
		}
		Switch switchOfThisRow = switchGroup.getSwitches().get(row);
		switch (col) {
			case 0 :
				switchOfThisRow.setNumber(Integer.parseInt((String)value));
				break;
			case 1 :
				break;
			case 2 :
				switchOfThisRow.setAddress(new Address((String)value));
				break;
			case 3 :
				switchOfThisRow.setBus(Integer.parseInt((String)value));
				break;
			case 4 :
				switchOfThisRow.setDesc((String)value);
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
