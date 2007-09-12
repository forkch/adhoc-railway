package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.routes.RouteItemOld;
import ch.fork.AdHocRailway.domain.routes.RouteOld;

public class RoutedSwitchesTableModel extends AbstractTableModel {

	private String[] columnNames = { "Switch-Number", "Routed-State" };

	private Set<RouteItemOld> routeItems;

	public RoutedSwitchesTableModel() {
	}

	public int getRowCount() {
		if (routeItems == null) {
			return 0;
		}
		return routeItems.size();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
        List<RouteItemOld> routeItemsArrayList = new ArrayList<RouteItemOld>(routeItems);
		RouteItemOld actualRouteItem = routeItemsArrayList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return actualRouteItem.getRoutedSwitch();
		case 1:
			return actualRouteItem.getRoutedSwitchState();
		}
		return null;
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) {
        List<RouteItemOld> routeItemsArrayList = new ArrayList<RouteItemOld>(routeItems);
		RouteItemOld actualRouteItem = routeItemsArrayList.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return;
		case 1:
			SwitchState state = (SwitchState) value;
			actualRouteItem.setRoutedSwitchState(state);
		}
		return;
	}

	public void setRoute(RouteOld route) {
		this.routeItems = route.getRouteItems();
		fireTableDataChanged();
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public boolean isCellEditable(int row, int column) {
		switch (column) {
		case 0:
			return false;
		case 1:
			return true;
		}
		return false;
	}
}
