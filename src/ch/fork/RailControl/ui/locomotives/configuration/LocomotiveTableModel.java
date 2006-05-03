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

package ch.fork.RailControl.ui.locomotives.configuration;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.fork.RailControl.domain.locomotives.DeltaLocomotive;
import ch.fork.RailControl.domain.locomotives.DigitalLocomotive;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.NoneLocomotive;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.ui.ExceptionProcessor;

public class LocomotiveTableModel extends AbstractTableModel {

	private final String[] columnNames = { "Name", "Type", "Bus", "Address",
			"Image", "Desc" };

	private List<Locomotive> locomotives;

	public LocomotiveTableModel() {
		super();
	}

	public LocomotiveTableModel(List<Locomotive> locomotives) {
		super();
		this.locomotives = locomotives;
	}

	public int getRowCount() {
		return locomotives.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return locomotives.get(rowIndex).getName();
		case 1:
			return locomotives.get(rowIndex).getType();
		case 2:
			return locomotives.get(rowIndex).getBus();
		case 3:
			return locomotives.get(rowIndex).getAddress();
		case 4:
			return null;
		case 5:
			return locomotives.get(rowIndex).getDesc();
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	@SuppressWarnings("unchecked")
	public void setValueAt(Object value, int row, int col) {
		Locomotive locomotiveOfThisRow = locomotives.get(row);
		switch (col) {
		case 0:
			locomotiveOfThisRow.setName(value.toString());
			break;
		case 1:
			Locomotive tmp = locomotiveOfThisRow;
			if (value.equals("NoneLocomotive")) {
				locomotiveOfThisRow = new NoneLocomotive();
			} else if (value.equals("DeltaLocomotive")) {
				locomotiveOfThisRow = new DeltaLocomotive(tmp.getSession(), tmp
						.getName(), tmp.getBus(), tmp.getAddress(), tmp.getDesc());
			} else if (value.equals("DigitalLocomotive")) {
				locomotiveOfThisRow = new DigitalLocomotive(tmp.getSession(), tmp
						.getName(), tmp.getBus(), tmp.getAddress(), tmp.getDesc());
			}
			locomotives.remove(tmp);
			locomotives.add(locomotiveOfThisRow);
			break;
		case 2:
			locomotiveOfThisRow.setBus(Integer.parseInt(value.toString()));
			break;
		case 3:
			try {
					locomotiveOfThisRow.setAddress(Integer.parseInt(value.toString()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (LocomotiveException e) {
					ExceptionProcessor.getInstance().processException(e);
				}
			break;
		case 4:
			break;
		case 5:
			locomotiveOfThisRow.setDesc(value.toString());
			break;
		default:
			return;
		}
		fireTableCellUpdated(row, col);
	}
}
