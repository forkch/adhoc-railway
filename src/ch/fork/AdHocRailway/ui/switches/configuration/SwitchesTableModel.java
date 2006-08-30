/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchesTableModel.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:08 BST 2006
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


package ch.fork.AdHocRailway.ui.switches.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchOrientation;

public class SwitchesTableModel extends AbstractTableModel {
    private final String[]       columnNames = { "#", "Type", "Bus", "Addr. 1",
        "Addr. 2", "Addr. 1 switched", "Addr. 2 switched", "Default State",
        "Orientation", "Desc"               };
    private SwitchGroup          switchGroup;
    private Map<Integer, Switch> switchNumberToSwitch;

    public SwitchesTableModel(Map<Integer, Switch> switchNumberToSwitch) {
        super();
        this.switchNumberToSwitch = switchNumberToSwitch;
    }

    public SwitchesTableModel(SwitchGroup switchGroup) {
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

    @SuppressWarnings("unchecked")
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (switchGroup == null) {
            return null;
        }
        List<Switch> switches = new ArrayList(switchGroup.getSwitches());
        Switch switchOfThisRow = switches.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return switchOfThisRow.getNumber();
        case 1:
            return switchOfThisRow.getType();
        case 2:
            return switchOfThisRow.getAddress(0).getBus();
        case 3:
            return switchOfThisRow.getAddress(0).getAddress();
        case 4:
            if (switchOfThisRow.getAddresses().length != 1) {
                return switchOfThisRow.getAddress(1).getAddress();
            } else {
                return "";
            }
        case 5:
            return (Boolean) switchOfThisRow.getAddress(0).isAddressSwitched();
        case 6:
            if (switchOfThisRow.getAddresses().length != 1) {
                return switchOfThisRow.getAddress(1).isAddressSwitched();
            } else {
                return false;
            }
        case 7:
            return switchOfThisRow.getDefaultState();
        case 8:
            return switchOfThisRow.getSwitchOrientation();
        case 9:
            return switchOfThisRow.getDesc();

        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isCellEditable(int row, int col) {
        List<Switch> switches = new ArrayList(switchGroup.getSwitches());
        Switch switchOfThisRow = switches.get(row);
        if (col == 4 && switchOfThisRow.getAddresses().length == 1) {
            return false;
        }
        if (col == 6 && switchOfThisRow.getAddresses().length == 1) {
            return false;
        }
        if (col == 7 && switchOfThisRow.getType().equals("ThreeWaySwitch")) {
            return false;
        }

        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        if (switchGroup == null) {
            return;
        }
        List<Switch> switches = new ArrayList<Switch>(switchGroup.getSwitches());
        Switch switchOfThisRow = switches.get(row);
        Address[] oldAddresses = switchOfThisRow.getAddresses();
        switch (col) {
        case 0:
            switchOfThisRow.setNumber(((Integer) value).intValue());
            break;
        case 1:
            Switch tmp = switchOfThisRow;
            if (value.equals("DefaultSwitch")) {
                switchOfThisRow = new DefaultSwitch(tmp.getNumber(), tmp
                    .getDesc(), tmp.getAddress(0));
            } else if (value.equals("DoubleCrossSwitch")) {
                switchOfThisRow = new DoubleCrossSwitch(tmp.getNumber(), tmp
                    .getDesc(), tmp.getAddress(0));
            } else if (value.equals("ThreeWaySwitch")) {
                Address[] old = new Address[2];
                old[0] = tmp.getAddress(0);
                old[1] = new Address(Constants.DEFAULT_BUS, 0);
                switchOfThisRow = new ThreeWaySwitch(tmp.getNumber(), tmp
                    .getDesc(), old);
            }
            switchGroup.replaceSwitch(tmp, switchOfThisRow);
            switchNumberToSwitch.remove(tmp.getNumber());
            switchNumberToSwitch.put(switchOfThisRow.getNumber(),
                switchOfThisRow);
            tmp = null;
            break;
        case 2:
            int bus = (((Integer) value).intValue());
            switchOfThisRow.getAddress(0).setBus(bus);
            if (switchOfThisRow.getAddresses().length == 2) {
                switchOfThisRow.getAddress(1).setBus(bus);
            }
            break;
        case 3:
            oldAddresses[0].setAddress(((Integer) value).intValue());
            switchOfThisRow.setAddresses(oldAddresses);
            break;
        case 4:
            oldAddresses[1].setAddress(((Integer) value).intValue());
            switchOfThisRow.setAddresses(oldAddresses);
            break;
        case 5:
            oldAddresses[0].setAddressSwitched((Boolean) value);
            switchOfThisRow.setAddresses(oldAddresses);
            break;
        case 6:
            oldAddresses[1].setAddressSwitched((Boolean) value);
            switchOfThisRow.setAddresses(oldAddresses);
            break;
        case 7:
            switchOfThisRow.setDefaultState((SwitchState) value);
            break;
        case 8:
            switchOfThisRow.setSwitchOrientation((SwitchOrientation) value);
            break;
        case 9:
            switchOfThisRow.setDesc((String) value);
            break;
        default:
        }
        fireTableCellUpdated(row, col);
    }

    public SwitchGroup getSwitchGroup() {
        return switchGroup;
    }

    public void setSwitchGroup(SwitchGroup switchGroup) {
        this.switchGroup = switchGroup;
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Integer.class;
        case 1:
            return String.class;
        case 2:
            return Integer.class;
        case 3:
            return Integer.class;
        case 4:
            return Integer.class;
        case 5:
            return Boolean.class;
        case 6:
            return Boolean.class;
        case 7:
            return SwitchState.class;
        case 8:
            return SwitchOrientation.class;
        case 9:
            return String.class;

        default:
            return Object.class;
        }
    }
}
