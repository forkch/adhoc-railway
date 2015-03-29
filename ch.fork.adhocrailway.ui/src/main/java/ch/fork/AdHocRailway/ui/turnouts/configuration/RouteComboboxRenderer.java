package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.model.turnouts.Route;

import javax.swing.*;
import java.awt.*;

/**
 * Created by fork on 29.03.15.
 */
public class RouteComboboxRenderer implements ListCellRenderer<Route> {

    public Component getListCellRendererComponent(JList<? extends Route> list, Route value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel routeName = new JLabel();
        if (value != null) {
            routeName.setText(value.getName() + " - " + value.getOrientation() + " [#" + value.getNumber() + "]");
        } else {
            routeName.setText("");
        }
        return routeName;
    }
}
