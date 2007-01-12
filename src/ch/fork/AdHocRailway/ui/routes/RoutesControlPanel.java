
package ch.fork.AdHocRailway.ui.routes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.SortedSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.switches.canvas.Segment7;

public class RoutesControlPanel extends JPanel {

    private RouteControl       routeControl;
    private Segment7           seg1;
    private Segment7           seg2;
    private Segment7           seg3;

    private JFrame             frame;
    private JPanel             routesPanel;
    private GridBagLayout      layout;
    private GridBagConstraints gbc;
    private int maxCols;

    public RoutesControlPanel(JFrame frame) {
        super();
        this.frame = frame;
        initGUI();
        routeControl = RouteControl.getInstance();
    }

    private void initGUI() {
        setLayout(new BorderLayout(5, 5));
        JPanel segmentPanelNorth = initSegmentPanel();
        JPanel routesPanel = initRoutesPanel();
        //add(segmentPanelNorth, BorderLayout.NORTH);
        add(routesPanel, BorderLayout.CENTER);
    }

    private JPanel initSegmentPanel() {
        JPanel segmentPanelNorth = new JPanel(new FlowLayout(
            FlowLayout.TRAILING, 5, 0));
        seg1 = new Segment7();
        seg2 = new Segment7();
        seg3 = new Segment7();

        segmentPanelNorth.setBackground(new Color(0, 0, 0));
        segmentPanelNorth.add(seg3);
        segmentPanelNorth.add(seg2);
        segmentPanelNorth.add(seg1);
        JPanel p = new JPanel(new BorderLayout());
        p.add(segmentPanelNorth, BorderLayout.WEST);
        return p;
    }

    private JPanel initRoutesPanel() {
        routesPanel = new JPanel();
        layout = new GridBagLayout();
        gbc = new GridBagConstraints();
        routesPanel.setLayout(layout);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        maxCols = Preferences.getInstance().getIntValue(
            PreferencesKeys.SWITCH_CONTROLES);
        return routesPanel;
    }

    public void update(SortedSet<Route> routes) {
        int currentRow = 0;
        int currentCol = 0;
        gbc.gridx = currentCol;
        gbc.gridy = currentRow;
        routesPanel.removeAll();
        for (Route route : routes) {
            
            RouteWidget routeWidget = new RouteWidget(route, frame);
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