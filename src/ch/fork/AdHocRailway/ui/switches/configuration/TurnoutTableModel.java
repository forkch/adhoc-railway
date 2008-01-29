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

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

public class TurnoutTableModel extends AbstractTableModel {
    private final String[]       columnNames = { "#", "Type", "Bus 1", "Bus 2", "Addr. 1",
        "Addr. 2", "Addr. 1 switched", "Addr. 2 switched", "Default State",
        "Orientation", "Desc"               };
    private TurnoutGroup          turnoutGroup;

    public TurnoutTableModel() {
    }
    
    public TurnoutTableModel(TurnoutGroup turnoutGroup) {
        super();
        this.turnoutGroup = turnoutGroup;
    }

    public int getRowCount() {
        if (turnoutGroup == null) {
            return 0;
        }
        return turnoutGroup.getTurnouts().size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    @SuppressWarnings("unchecked")
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (turnoutGroup == null) {
            return null;
        }
        List<Turnout> turnouts = new ArrayList(turnoutGroup.getTurnouts());
        Turnout turnoutOfThisRow = turnouts.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return turnoutOfThisRow.getNumber();
        case 1:
            return turnoutOfThisRow.getTurnoutType().getTurnoutTypeEnum();
        case 2:
            return turnoutOfThisRow.getBus1();
        case 3:
        	return turnoutOfThisRow.getBus2();
        case 4:
            return turnoutOfThisRow.getAddress1();
        case 5:
            if (turnoutOfThisRow.isThreeWay())
                return turnoutOfThisRow.getAddress2();
            else
                return "";
        case 6:
            return (Boolean) turnoutOfThisRow.isAddress1Switched();
        case 7:
            if (turnoutOfThisRow.isThreeWay())
                return (Boolean) turnoutOfThisRow.isAddress2Switched();
            else
                return false;
        case 8:
            return turnoutOfThisRow.getDefaultStateEnum();
        case 9:
            return turnoutOfThisRow.getOrientationEnum();
        case 10:
            return turnoutOfThisRow.getDescription();

        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isCellEditable(int row, int col) {
        List<Turnout> turnouts = new ArrayList(turnoutGroup.getTurnouts());
        Turnout turnoutsOfThisRow = turnouts.get(row);
        if (col == 3 && !turnoutsOfThisRow.isThreeWay()) {
            return false;
        }
        if (col == 5 && !turnoutsOfThisRow.isThreeWay()) {
            return false;
        }
        if (col == 7 && !turnoutsOfThisRow.isThreeWay()) {
            return false;
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
//        if (turnoutGroup == null) {
//            return;
//        }
//        List<Switch> switches = new ArrayList<Switch>(turnoutGroup.getSwitches());
//        Switch switchOfThisRow = switches.get(row);
//        Address[] oldAddresses = switchOfThisRow.getAddresses();
//        switch (col) {
//        case 0:
//            switchOfThisRow.setNumber(((Integer) value).intValue());
//            break;
//        case 1:
//            Switch tmp = switchOfThisRow;
//            if (value.equals("DefaultSwitch")) {
//                switchOfThisRow = new DefaultSwitch(tmp.getNumber(), tmp
//                    .getDesc(), tmp.getAddress(0));
//            } else if (value.equals("DoubleCrossSwitch")) {
//                switchOfThisRow = new DoubleCrossSwitch(tmp.getNumber(), tmp
//                    .getDesc(), tmp.getAddress(0));
//            } else if (value.equals("ThreeWaySwitch")) {
//                Address[] old = new Address[2];
//                old[0] = tmp.getAddress(0);
//                old[1] = new Address(Constants.DEFAULT_BUS, 0);
//                switchOfThisRow = new ThreeWaySwitch(tmp.getNumber(), tmp
//                    .getDesc(), old);
//            }
//            turnoutGroup.replaceSwitch(tmp, switchOfThisRow);
//            switchNumberToSwitch.remove(tmp.getNumber());
//            switchNumberToSwitch.put(switchOfThisRow.getNumber(),
//                switchOfThisRow);
//            tmp = null;
//            break;
//        case 2:
//            int bus = (((Integer) value).intValue());
//            switchOfThisRow.getAddress(0).setBus(bus);
//            if (switchOfThisRow.getAddresses().length == 2) {
//                switchOfThisRow.getAddress(1).setBus(bus);
//            }
//            break;
//        case 3:
//            oldAddresses[0].setAddress(((Integer) value).intValue());
//            switchOfThisRow.setAddresses(oldAddresses);
//            break;
//        case 4:
//            oldAddresses[1].setAddress(((Integer) value).intValue());
//            switchOfThisRow.setAddresses(oldAddresses);
//            break;
//        case 5:
//            oldAddresses[0].setAddressSwitched((Boolean) value);
//            switchOfThisRow.setAddresses(oldAddresses);
//            break;
//        case 6:
//            oldAddresses[1].setAddressSwitched((Boolean) value);
//            switchOfThisRow.setAddresses(oldAddresses);
//            break;
//        case 7:
//            switchOfThisRow.setDefaultState((SwitchState) value);
//            break;
//        case 8:
//            switchOfThisRow.setSwitchOrientation((SwitchOrientation) value);
//            break;
//        case 9:
//            switchOfThisRow.setDesc((String) value);
//            break;
//        default:
//        }
//        fireTableCellUpdated(row, col);
    }

    public TurnoutGroup getTurnoutGroup() {
        return turnoutGroup;
    }

    public void setTurnoutGroup(TurnoutGroup turnoutGroup) {
        this.turnoutGroup = turnoutGroup;
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Integer.class;
        case 1:
            return TurnoutTypes.class;
        case 2:
            return Integer.class;
        case 3:
            return Integer.class;
        case 4:
            return Integer.class;
        case 5:
            return Integer.class;
        case 6:
            return Boolean.class;
        case 7:
            return Boolean.class;
        case 8:
        	return TurnoutState.class;
        case 9:
            return TurnoutOrientation.class;
        case 10:
            return String.class;

        default:
            return Object.class;
        }
    }
    public Turnout getTurnoutAt(int rowIndex) {
    	if (turnoutGroup == null) {
			return null;
		}
		List<Turnout> turnouts = new ArrayList<Turnout>(
				turnoutGroup.getTurnouts());
		return turnouts.get(rowIndex);
    }
}
