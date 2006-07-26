package ch.fork.RailControl.ui.locomotives.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.fork.RailControl.domain.locomotives.DeltaLocomotive;
import ch.fork.RailControl.domain.locomotives.DigitalLocomotive;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveGroup;
import ch.fork.RailControl.domain.locomotives.NoneLocomotive;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.ui.ExceptionProcessor;

public class LocomotiveTableModel extends AbstractTableModel {

    private final String[] columnNames = { "Name", "Type", "Bus",
        "Address", "Image", "Desc" };


    private LocomotiveGroup locomotiveGroup;

    public LocomotiveTableModel() {
        super();
    }

    public LocomotiveTableModel(Collection<Locomotive> locomotives) {
        super();
        //this.locomotives = new ArrayList<Locomotive>(locomotives);
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

    public void setValueAt(Object value, int row, int col) {
        if (locomotiveGroup == null) {
            return;
        }
        List<Locomotive> locomotives = new ArrayList<Locomotive>(locomotiveGroup.getLocomotives());
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
                locomotiveOfThisRow = new DeltaLocomotive(
                    tmp.getSession(), tmp.getName(), tmp.getBus(), tmp
                        .getAddress(), tmp.getDesc());
            } else if (value.equals("DigitalLocomotive")) {
                locomotiveOfThisRow = new DigitalLocomotive(tmp
                    .getSession(), tmp.getName(), tmp.getBus(), tmp
                    .getAddress(), tmp.getDesc());
            }
            locomotives.remove(tmp);
            locomotives.add(locomotiveOfThisRow);
            break;
        case 2:
            locomotiveOfThisRow.setBus(Integer.parseInt(value.toString()));
            break;
        case 3:
            try {
                locomotiveOfThisRow.setAddress(Integer.parseInt(value
                    .toString()));
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

    public void setLocomotiveGroup(LocomotiveGroup selectedLocomotiveGroup) {
        locomotiveGroup = selectedLocomotiveGroup;
    }
}
