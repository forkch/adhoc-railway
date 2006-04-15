/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchTypeCellRenderer.java>  -  <>
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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class SwitchTypeCellEditor extends AbstractCellEditor implements TableCellEditor {

	private static Object[] values = {"DefaultSwitch", "DoubleCrossSwitch",
			"ThreeWaySwitch"};
	private JComboBox typeComboBox;
	public SwitchTypeCellEditor() {
		super();
		typeComboBox = new JComboBox(values);
		typeComboBox.setRenderer(new SwitchTypeComboBoxCellRenderer());
		typeComboBox.setPreferredSize(new Dimension(56,35));
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		return typeComboBox;
	}

	public Object getCellEditorValue() {
		System.out.println("here");
		return typeComboBox.getSelectedItem();
	}
}
