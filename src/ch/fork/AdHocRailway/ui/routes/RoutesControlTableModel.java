package ch.fork.AdHocRailway.ui.routes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.routes.Route;

public class RoutesControlTableModel extends AbstractTableModel {

	private String[] columnNames = { "Route-Name", "State" };

	private List<Route> routes;

	public RoutesControlTableModel(Collection<Route> routes) {
		this.routes = new ArrayList<Route>();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getRowCount() {
		return routes.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Route actualRoute = routes.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return actualRoute.getName();
		case 1:
			return actualRoute.isEnabled();
		}
		return null;
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		}
		return null;
	}

	public void setRoutes(Collection<Route> routes) {
		this.routes = new ArrayList<Route>(routes);
		fireTableDataChanged();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		return false;
	}
}
