package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;

public class RoutedSwitchesTableModel extends AbstractTableModel {

    private String[] columnNames = {"Switch-Group", "Switch-Number", "Routed-State"};
    
    private List<RouteItem> routeItems;

    public RoutedSwitchesTableModel() {
    }

    public int getRowCount() {
        if(routeItems == null) {
            return 0;
        }
        return routeItems.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        RouteItem actualRouteItem = routeItems.get(rowIndex);
        switch(columnIndex){ 
        case 0:
            return "";
        case 1:
            return actualRouteItem.getRoutedSwitch();
        case 2:
            return actualRouteItem.getRoutedSwitchState();
        }
        return null;
    }

    public void setRoute(Route route) {
        this.routeItems = route.getRouteItems();
    }
    
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
