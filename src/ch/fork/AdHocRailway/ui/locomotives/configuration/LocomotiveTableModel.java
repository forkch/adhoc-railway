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
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.locomotives.DeltaLocomotive;
import ch.fork.AdHocRailway.domain.locomotives.DigitalLocomotive;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.NoneLocomotive;

public class LocomotiveTableModel extends AbstractTableModel {
    private final String[]  columnNames = { "Name", "Type", "Bus", "Address",
        "Image", "Desc"                };
    private Set<Locomotive> locomotives;
    private LocomotiveGroup locomotiveGroup;

    public LocomotiveTableModel(Set<Locomotive> locomotives) {
        super();
        this.locomotives = locomotives;
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
            return locomotives.get(rowIndex).getType();
        case 2:
            return locomotives.get(rowIndex).getAddress().getBus();
        case 3:
            return locomotives.get(rowIndex).getAddress().getAddress();
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

    public void setValueAt(Object value, int row, int col) {
        if (locomotiveGroup == null) {
            return;
        }
        List<Locomotive> locomotivesOfGroup = new ArrayList<Locomotive>(
            locomotiveGroup.getLocomotives());
        Locomotive locomotiveOfThisRow = locomotivesOfGroup.get(row);
        Address tmpAddress = locomotiveOfThisRow.getAddress();
        switch (col) {
        case 0:
            locomotiveOfThisRow.setName(value.toString());
            break;
        case 1:
            Locomotive tmp = locomotiveOfThisRow;
            Address newAddress = new Address(tmp.getAddress().getBus(), tmp
                .getAddress().getAddress());
            if (value.equals("NoneLocomotive")) {
                locomotiveOfThisRow = new NoneLocomotive();
            } else if (value.equals("DeltaLocomotive")) {
                locomotiveOfThisRow = new DeltaLocomotive(tmp.getName(),
                    newAddress, tmp.getDesc());
            } else if (value.equals("DigitalLocomotive")) {
                locomotiveOfThisRow = new DigitalLocomotive(tmp.getName(),
                    newAddress, tmp.getDesc());
            }
            locomotiveGroup.replaceLocomotive(tmp, locomotiveOfThisRow);
            locomotives.remove(tmp);
            locomotives.add(locomotiveOfThisRow);
            break;
        case 2:
            tmpAddress = locomotiveOfThisRow.getAddress();
            tmpAddress.setBus(Integer.parseInt(value.toString()));
            locomotiveOfThisRow.setAddress(tmpAddress);

            break;
        case 3:

            tmpAddress = locomotiveOfThisRow.getAddress();
            tmpAddress.setAddress(Integer.parseInt(value.toString()));
            locomotiveOfThisRow.setAddress(tmpAddress);

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
