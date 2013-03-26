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
	private static final String[] COLUMNS = { "Number", "Function",
			"Emergency Stop" };

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
		default:
			throw new IllegalStateException("Unknown column");
		}
	}
}
