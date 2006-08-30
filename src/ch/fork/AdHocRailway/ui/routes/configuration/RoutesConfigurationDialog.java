
package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.TableResizer;

public class RoutesConfigurationDialog<E> extends ConfigurationDialog<RoutesConfiguration> {
    private RouteControl             routeControl;
    private List<Route>              routesWorkCopy;
    private ListListModel            routesListModel;
    private JList                    routeList;
    private JButton                  addRouteButton;
    private JButton                  removeRouteButton;
    private JPopupMenu               routesPopupMenu;
    private RoutedSwitchesTableModel routedSwitchesTableModel;
    private JPanel                   routeDetailPanel;
    private JTable                   routedSwitchesTable;

    public RoutesConfigurationDialog(JFrame parent) {
        super(parent, "Edit Routes");
        initGUI();
    }

    private void initGUI() {
        JPanel routesPanel = createRoutesPanel();
        JPanel routeDetailPanel = createRouteDetailPanel();
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(routesPanel, BorderLayout.WEST);
        mainPanel.add(routeDetailPanel, BorderLayout.CENTER);

        addMainComponent(mainPanel);
        pack();
        setVisible(true);
    }


    @Override
    public void createTempConfiguration() {
        this.routeControl = RouteControl.getInstance();
        this.routesWorkCopy = new ArrayList<Route>();
        for (Route r : routeControl.getRoutes().values()) {
            Route newRoute = (Route) r.clone();
            routesWorkCopy.add(newRoute);
        }
    }

    @Override
    public RoutesConfiguration getTempConfiguration() {
        return new RoutesConfiguration(routesWorkCopy);
    }

    private JPanel createRoutesPanel() {
        JPanel routesPanel = new JPanel(new BorderLayout());
        TitledBorder title = BorderFactory.createTitledBorder("Routes");
        routesPanel.setBorder(title);
        routesPanel.getInsets(new Insets(5, 5, 5, 5));
        routesListModel = new ListListModel<Route>(routesWorkCopy);
        routeList = new JList(routesListModel);
        routeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        routesPopupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add");
        JMenuItem removeItem = new JMenuItem("Remove");
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem moveUpItem = new JMenuItem("Move up");
        JMenuItem moveDownItem = new JMenuItem("Move down");

        routesPopupMenu.add(addItem);
        routesPopupMenu.add(removeItem);
        routesPopupMenu.add(renameItem);
        routesPopupMenu.add(new JSeparator());
        routesPopupMenu.add(moveUpItem);
        routesPopupMenu.add(moveDownItem);

        addRouteButton = new JButton("Add");
        removeRouteButton = new JButton("Remove");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addRouteButton);
        buttonPanel.add(removeRouteButton);

        routesPanel.add(routeList, BorderLayout.CENTER);
        routesPanel.add(buttonPanel, BorderLayout.SOUTH);

        /* Install ActionListeners */
        routeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateRouteDetailPanel();
            }

        });
        routeList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    routesPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        AddRouteAction addAction = new AddRouteAction();
        RemoveRouteAction removeAction = new RemoveRouteAction();
        RenameRouteAction renameAction = new RenameRouteAction();

        addItem.addActionListener(addAction);
        addRouteButton.addActionListener(addAction);

        removeItem.addActionListener(removeAction);
        removeRouteButton.addActionListener(removeAction);

        renameItem.addActionListener(renameAction);

        moveUpItem.addActionListener(new MoveRouteAction(true));
        moveDownItem.addActionListener(new MoveRouteAction(false));
        return routesPanel;
    }

    private JPanel createRouteDetailPanel() {
        routeDetailPanel = new JPanel(new BorderLayout());
        TitledBorder title = BorderFactory.createTitledBorder("Route");
        routeDetailPanel.setBorder(title);
        routeDetailPanel.getInsets(new Insets(5, 5, 5, 5));

        Route selectedRoute = (Route) routeList.getSelectedValue();
        routedSwitchesTableModel = new RoutedSwitchesTableModel();
        routedSwitchesTable = new JTable(routedSwitchesTableModel);
        JScrollPane tableScrollPane = new JScrollPane(routedSwitchesTable);
        routeDetailPanel.add(tableScrollPane, BorderLayout.CENTER);

        return routeDetailPanel;
    }

    private void updateRouteDetailPanel() {
        Route selectedRoute = (Route) (routeList.getSelectedValue());
        if (selectedRoute == null) {
            ((TitledBorder) routeDetailPanel.getBorder()).setTitle("Route");
        } else {
            ((TitledBorder) routeDetailPanel.getBorder()).setTitle("Route '"
                + selectedRoute.getName() + "'");
        }
        ((RoutedSwitchesTableModel) routedSwitchesTableModel)
            .setRoute(selectedRoute);
        TableResizer.adjustColumnWidths(routedSwitchesTable, 30);
        if (routedSwitchesTable.getRowCount() > 0) {
            TableResizer.adjustRowHeight(routedSwitchesTable);
        }
        pack();

    }


    private class AddRouteAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            String newRouteName = JOptionPane.showInputDialog(
                RoutesConfigurationDialog.this,
                "Enter the name of the new Route", "Add Route",
                JOptionPane.QUESTION_MESSAGE);
            Route newRoute = new Route(newRouteName);
            routesWorkCopy.add(newRoute);
            routesListModel.updated();
            routeList.setSelectedValue(newRoute, true);
        }

    }
    private class RemoveRouteAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            Route routeToDelete = (Route) (routeList
                .getSelectedValue());
            int response = JOptionPane.showConfirmDialog(
                RoutesConfigurationDialog.this, "Really remove Route '"
                    + routeToDelete.getName() + "' ?", "Remove Route",
                JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                routesWorkCopy.remove(routeToDelete);
                routesListModel.updated();
            }
        }

    }
    private class RenameRouteAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            Route routeToRename = (Route) (routeList
                .getSelectedValue());
            String newSectionName = JOptionPane.showInputDialog(
                RoutesConfigurationDialog.this, "Enter new name",
                "Rename Switch-Group", JOptionPane.QUESTION_MESSAGE);
            if (!newSectionName.equals("")) {
                routeToRename.setName(newSectionName);
                routesListModel.updated();
            }
            updateRouteDetailPanel();
        }

    }
    private class MoveRouteAction extends AbstractAction {
        private boolean up;

        public MoveRouteAction(boolean up) {
            this.up = up;
        }

        public void actionPerformed(ActionEvent e) {
            Route routeToMove = (Route) (routeList
                .getSelectedValue());
            int oldIndex = routesWorkCopy.indexOf(routeToMove);
            System.out.println(oldIndex);
            int newIndex = oldIndex;
            if (up) {
                if (oldIndex != 0) {
                    newIndex = oldIndex - 1;
                } else {
                    return;
                }
            } else {
                if (oldIndex != routesWorkCopy.size() - 1) {
                    newIndex = oldIndex + 1;
                } else {
                    return;
                }
            }
            routesWorkCopy.remove(oldIndex);
            routesWorkCopy.add(newIndex, routeToMove);
            routeList.setSelectedIndex(newIndex);
            routesListModel.updated();
            updateRouteDetailPanel();
        }

    }
}