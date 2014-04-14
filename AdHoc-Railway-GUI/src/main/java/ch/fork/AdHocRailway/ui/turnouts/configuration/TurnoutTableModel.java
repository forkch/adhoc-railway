package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import com.jgoodies.binding.adapter.AbstractTableAdapter;

import javax.swing.*;

public class TurnoutTableModel extends AbstractTableAdapter<Turnout> {

    private static final String[] COLUMNS = {"#", "Type", "Bus 1", "Addr. 1",
            "Addr. 1 inv.", "Bus 2", "Addr. 2", "Addr. 2 inv.",
            "Default State", "Orientation", "Desc"};

    public TurnoutTableModel(final ListModel<?> listModel) {
        super(listModel, COLUMNS);
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final Turnout turnout = getRow(rowIndex);
        switch (columnIndex) {
            case 0:
                return turnout.getNumber();
            case 1:
                return turnout.getType();
            case 2:
                return turnout.getBus1();
            case 3:
                return turnout.getAddress1();
            case 4:
                return Boolean.valueOf(turnout.isAddress1Switched());
            case 5:
                return turnout.getBus2();
            case 6:
                return turnout.getAddress2();
            case 7:
                return Boolean.valueOf(turnout.isAddress2Switched());
            case 8:
                return turnout.getDefaultState();
            case 9:
                return turnout.getOrientation();
            case 10:
                return turnout.getDescription();
            default:
                throw new IllegalStateException("Unknown column");
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 2:
            case 3:
            case 5:
            case 6:
                return Integer.class;
            case 4:
            case 7:
                return Boolean.class;
            case 1:
                return TurnoutType.class;
            case 8:
                return TurnoutState.class;
            case 9:
                return TurnoutOrientation.class;
            case 10:
                return String.class;
        }
        return null;

    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

}