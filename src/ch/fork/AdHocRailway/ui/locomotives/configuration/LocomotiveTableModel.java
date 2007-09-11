/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/configuration/LocomotiveTableModel.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:55 BST 2006
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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class LocomotiveTableModel extends AbstractTableModel {
	private final String[] columnNames = { "Name", "Type", "Bus", "Address",
			"Image", "Desc" };

	private LocomotiveGroup locomotiveGroup;

	public LocomotiveTableModel(LocomotiveGroup locomotiveGroup) {
		super();
		this.locomotiveGroup = locomotiveGroup;
	}

	public int getRowCount() {
		if (locomotiveGroup == null) {
			return 0;
		}
		return locomotiveGroup.getLocomotives().size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Locomotive getLocomotiveAt(int rowIndex) {
		if (locomotiveGroup == null) {
			return null;
		}
		List<Locomotive> locomotives = new ArrayList<Locomotive>(
				locomotiveGroup.getLocomotives());
		return locomotives.get(rowIndex);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (locomotiveGroup == null) {
			return null;
		}
		List<Locomotive> locomotives = new ArrayList<Locomotive>(
				locomotiveGroup.getLocomotives());
		switch (columnIndex) {
		case 0:
			return locomotives.get(rowIndex).getName();
		case 1:
			return locomotives.get(rowIndex).getLocomotiveType().getTypeName();
		case 2:
			return locomotives.get(rowIndex).getBus();
		case 3:
			return locomotives.get(rowIndex).getAddress();
		case 4:
			return null;
		case 5:
			return locomotives.get(rowIndex).getDescription();
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		if (locomotiveGroup == null) {
			return;
		}
		List<Locomotive> locomotivesOfGroup = new ArrayList<Locomotive>(
				locomotiveGroup.getLocomotives());
		Locomotive locomotiveOfThisRow = locomotivesOfGroup.get(row);
		switch (col) {
		case 0:
			locomotiveOfThisRow.setName(value.toString());
			break;
		case 1:
			LocomotiveType lt = LocomotiveControl.getInstance()
					.getLocomotiveTypeByName(value.toString());
			locomotiveOfThisRow.setLocomotiveType(lt);
			break;
		case 2:
			locomotiveOfThisRow.setBus(Integer.parseInt(value.toString()));

			break;
		case 3:
			locomotiveOfThisRow.setAddress(Integer.parseInt(value.toString()));

			break;
		case 4:
			break;
		case 5:
			locomotiveOfThisRow.setDescription(value.toString());
			break;
		default:
			return;
		}
		fireTableCellUpdated(row, col);
	}

	public void setLocomotiveGroup(LocomotiveGroup selectedLocomotiveGroup) {
		locomotiveGroup = selectedLocomotiveGroup;
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		case 3:
			return Integer.class;
		case 4:
			return String.class;
		case 5:
			return String.class;

		default:
			return Object.class;
		}
	}
}
