package ch.fork.AdHocRailway.ui.locomotives.configuration;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import com.jgoodies.binding.adapter.AbstractTableAdapter;

import javax.swing.*;

/**
 * Describes how to present an Album in a JTable.
 */
public class LocomotiveTableModel extends AbstractTableAdapter<Locomotive> {

    /**
     *
     */
    private static final long serialVersionUID = -3279273929330645457L;
    private static final String[] COLUMNS = {"Name", "Image", "Type",
            "Address", "Desc"};

    public LocomotiveTableModel(ListModel<?> listModel) {
        super(listModel, COLUMNS);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Locomotive locomotive = getRow(rowIndex);
        switch (columnIndex) {
            case 0:
                return locomotive.getName();
            case 1:
                return locomotive.getImage();
            case 2:
                return locomotive.getType();
            case 3:
                return locomotive.getAddress1();
            case 4:
                return locomotive.getDesc();
            default:
                throw new IllegalStateException("Unknown column");
        }
    }

}