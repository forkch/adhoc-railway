package ch.fork.AdHocRailway.ui.routes.configuration;

import javax.swing.ListModel;

import ch.fork.AdHocRailway.domain.turnouts.RouteItem;

import com.jgoodies.binding.adapter.AbstractTableAdapter;

public class RouteItemTableModel extends AbstractTableAdapter<RouteItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7347063413543307040L;
	private static final String[] COLUMNS = { "Turnout Number",
			"Routed Turnout State" };

	RouteItemTableModel(ListModel<?> listModel) {
		super(listModel, COLUMNS);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		RouteItem routeItem = getRow(rowIndex);
		switch (columnIndex) {
		case 0:
			return routeItem.getTurnout().getNumber();
		case 1:
			return routeItem.getRoutedState();
		default:
			throw new IllegalStateException("Unknown column");
		}
	}

}
