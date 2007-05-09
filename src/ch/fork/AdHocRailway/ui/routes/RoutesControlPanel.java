
package ch.fork.AdHocRailway.ui.routes;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.SortedSet;

import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.routes.Route;

public class RoutesControlPanel extends JPanel {

    private JPanel             routesPanel;
    private GridBagLayout      layout;
    private GridBagConstraints gbc;
    private int maxCols;

    public RoutesControlPanel() {
        super();
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout(5, 5));
        JPanel routesPanel = initRoutesPanel();
        add(routesPanel, BorderLayout.CENTER);
    }

    private JPanel initRoutesPanel() {
        routesPanel = new JPanel();
        layout = new GridBagLayout();
        gbc = new GridBagConstraints();
        routesPanel.setLayout(layout);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //maxCols = Preferences.getInstance().getIntValue(
        //    PreferencesKeys.SWITCH_CONTROLES);
        maxCols = 5;
        return routesPanel;
    }

    public void update(SortedSet<Route> routes) {
        int currentRow = 0;
        int currentCol = 0;
        gbc.gridx = currentCol;
        gbc.gridy = currentRow;
        routesPanel.removeAll();
        for (Route route : routes) {
            
            RouteWidget routeWidget = new RouteWidget(route);
            routesPanel.add(routeWidget);
            layout.setConstraints(routeWidget, gbc);
            if (currentCol == maxCols) {
                currentRow++;
                currentCol = 0;
            } else {
                currentCol++;
            }
            gbc.gridx = currentCol;
            gbc.gridy = currentRow;
        }
    }
}