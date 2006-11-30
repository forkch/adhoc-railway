package ch.fork.AdHocRailway.ui.routes;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteChangeListener;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class RouteWidget extends JPanel implements RouteChangeListener {

    private Route        route;
    private RouteControl routeControl;
    private Frame        frame;
    private JLabel       nameLabel;
    private JLabel       iconLabel;
    private Icon         routeStopIcon;
    private Icon         routeStartIcon;
    private JProgressBar routingProgress;
    private JPanel       southPanel;
    private MouseAction  mouseAction;

    public RouteWidget(Route route, JFrame frame) {
        this.route = route;
        this.frame = frame;
        routeControl = RouteControl.getInstance();
        initGUI();
        routeControl.addRouteChangeListener(route, this);
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.LINE_AXIS));
        routeStopIcon = createImageIcon("icons/route_stop.png", "", this);
        routeStartIcon = createImageIcon("icons/route_start.png", "", this);
        nameLabel = new JLabel(route.getName());
        iconLabel = new JLabel(routeStopIcon);
        routingProgress =
            new JProgressBar(SwingConstants.HORIZONTAL, 0, route
                .getRouteItems().size());
        routingProgress.setPreferredSize(new Dimension((int) routingProgress
            .getPreferredSize().getWidth(), 8));
        routingProgress.setBorder(BorderFactory.createEmptyBorder());

        routingProgress.setBackground(Color.RED);
        routingProgress.setForeground(Color.GREEN);
        addMouseListener(new MouseAction());

        northPanel.add(Box.createHorizontalStrut(5));
        northPanel.add(nameLabel, BorderLayout.WEST);
        northPanel.add(Box.createGlue());
        northPanel.add(Box.createHorizontalStrut(5));
        northPanel.add(iconLabel, BorderLayout.EAST);
        northPanel.add(Box.createHorizontalStrut(5));

        southPanel = new JPanel();
        southPanel.add(routingProgress);
        add(northPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private class MouseAction extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            try {
                if (route.isRouting())
                    return;
                if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON1) {
                    if (route.isEnabled())
                        routeControl.disableRoute(route);
                    else
                        routeControl.enableRoute(route);
                    removeMouseListener(mouseAction);
                } else if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON3) {
                    displayRouteConfig();
                }
            } catch (SwitchException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
        }

        private void displayRouteConfig() {

        }
    }

    public void routeChanged(Route r) {
        if (route.equals(r)) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (route.isEnabled()) {
                        iconLabel.setIcon(routeStartIcon);
                        routingProgress.setForeground(Color.GREEN);
                    } else {
                        iconLabel.setIcon(routeStopIcon);
                        routingProgress.setBackground(Color.RED);
                    }
                    RouteWidget.this.revalidate();
                    RouteWidget.this.repaint();
                }
            });
        }
    }

    public void nextSwitchRouted() {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                routingProgress.setValue(routingProgress.getValue() + 1);
            }
        });
    }

    public void nextSwitchDerouted() {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                routingProgress.setValue(routingProgress.getValue() - 1);
            }
        });
    }
}