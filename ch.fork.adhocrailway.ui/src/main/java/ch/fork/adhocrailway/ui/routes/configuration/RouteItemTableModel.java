package ch.fork.adhocrailway.ui.routes.configuration;

import ch.fork.adhocrailway.model.turnouts.RouteItem;
import com.jgoodies.binding.adapter.AbstractTableAdapter;

import javax.swing.*;

public class RouteItemTableModel extends AbstractTableAdapter<RouteItem> {

    private static final String[] COLUMNS = {"Turnout Number",
            "Routed Turnout State"};

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
                return routeItem.getState();
            default:
                throw new IllegalStateException("Unknown column");
        }
    }

}
