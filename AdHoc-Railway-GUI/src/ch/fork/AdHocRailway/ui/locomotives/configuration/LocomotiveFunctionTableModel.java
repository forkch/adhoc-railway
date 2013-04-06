package ch.fork.AdHocRailway.ui.locomotives.configuration;

import javax.swing.ListModel;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

public class LocomotiveFunctionTableModel extends
		AbstractTableAdapter<LocomotiveFunction> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3279273929330645457L;
	private static final String[] COLUMNS = { "Function", "Description",
			"E. Stop", "Deactivation Delay" };

	public LocomotiveFunctionTableModel(final ListModel<?> listModel) {
		super(listModel, COLUMNS);
	}

	@Override
	public final Object getValueAt(final int rowIndex, final int columnIndex) {
		final LocomotiveFunction function = getRow(rowIndex);
		switch (columnIndex) {
		case 0:
			return function.getNumber();
		case 1:
			return function.getDescription();
		case 2:
			return function.isEmergencyBrakeFunction();
		case 3:
			return function.getDeactivationDelay();
		default:
			throw new IllegalStateException("Unknown column");
		}
	}

	@Override
	public void setValueAt(final Object value, final int row, final int col) {
		final LocomotiveFunction function = getRow(row);
		switch (col) {
		case 1:
			function.setDescription((String) value);
			break;
		case 2:
			function.setEmergencyBrakeFunction(((Boolean) value).booleanValue());
			break;
		case 3:
			function.setDeactivationDelay(((Integer) value).intValue());
			break;
		default:
			throw new IllegalStateException("Unknown column");
		}

		fireTableCellUpdated(row, col);
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		switch (col) {
		case 0:
			return false;
		case 1:
			return true;
		case 2:
			return true;
		case 3:
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getColumnClass(final int c) {
		return getValueAt(0, c).getClass();
	}

}
