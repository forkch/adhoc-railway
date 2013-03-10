package ch.fork.AdHocRailway.ui.locomotives.configuration;

import javax.swing.ListModel;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

/**
 * Describes how to present an Album in a JTable.
 */
public class LocomotiveTableModel extends AbstractTableAdapter<Locomotive> {

	private static final String[] COLUMNS = { "Name", "Image", "Type",
			"Address", "Desc" };

	public LocomotiveTableModel(ListModel listModel) {
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
			return locomotive.getLocomotiveType();
		case 3:
			return locomotive.getAddress();
		case 4:
			return locomotive.getDescription();
		default:
			throw new IllegalStateException("Unknown column");
		}
	}

}