package ch.fork.AdHocRailway.ui.turnouts.configuration;

import javax.swing.ListModel;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

public class TurnoutTableModel extends AbstractTableAdapter<Turnout> {

	private static final String[] COLUMNS = { "#", "Type", "Bus 1", "Addr. 1",
			"Addr. 1 switched", "Bus 2", "Addr. 2", "Addr. 2 switched",
			"Default State", "Orientation", "Desc" };

	public TurnoutTableModel(ListModel listModel) {
		super(listModel, COLUMNS);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Turnout turnout = getRow(rowIndex);
		switch (columnIndex) {
		case 0:
			return turnout.getNumber();
		case 1:
			return turnout.getTurnoutType();
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

}