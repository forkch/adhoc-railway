/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.ui.routes.configuration;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.RouteManagerListener;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.utils.GlobalKeyShortcutHelper;
import ch.fork.AdHocRailway.ui.utils.ImageTools;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.utils.ThreeDigitDisplay;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.common.collect.ArrayListModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class RoutesConfigurationDialog extends JDialog implements
        RouteManagerListener {

    private final RouteManager routeManager;
    private final RouteContext ctx;
    public StringBuffer enteredNumberKeys;
    public ThreeDigitDisplay digitDisplay;
    protected boolean okPressed;
    private JList<?> routeGroupList;
    private JButton addRouteGroupButton;
    private JButton removeRouteGroupButton;
    private JList<?> routesList;
    private JButton addRouteButton;
    private JButton removeRouteButton;
    private SelectionInList<RouteGroup> routeGroupModel;
    private JButton okButton;
    private SelectionInList<Route> routesModel;
    private PanelBuilder builder;
    private RouteGroupConfigPanel routeGroupConfig;
    private com.jgoodies.common.collect.ArrayListModel<RouteGroup> routeGroups;
    private com.jgoodies.common.collect.ArrayListModel<Route> routes;
    private JButton editGroupButton;
    private JButton duplicateRouteButton;

    public RoutesConfigurationDialog(final JFrame parent, final RouteContext ctx) {
        super(parent, "Edit Routes", true);
        this.ctx = ctx;
        routeManager = ctx.getRouteManager();
        initGUI();
    }

    private void initGUI() {
        buildPanel();
        initShortcuts();
        routeManager.addRouteManagerListener(this);
        routesUpdated(routeManager.getAllRouteGroups());
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void buildPanel() {
        initComponents();
        initEventHandling();

        final FormLayout layout = new FormLayout(
                "pref, 5dlu, pref, 5dlu",
                "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref, 3dlu, pref");
        builder = new PanelBuilder(layout);
        layout.setColumnGroups(new int[][]{{1, 3}});
        builder.setDefaultDialogBorder();
        final CellConstraints cc = new CellConstraints();

        builder.addSeparator("Route Groups", cc.xyw(1, 1, 1));

        builder.add(new JScrollPane(routeGroupList), cc.xy(1, 3));
        builder.add(routeGroupConfig, cc.xy(1, 5));
        builder.add(buildRouteGroupButtonBar(), cc.xy(1, 7));

        builder.addSeparator("Routes", cc.xyw(3, 1, 1));

        builder.add(new JScrollPane(routesList), cc.xywh(3, 3, 1, 3));
        builder.add(buildRouteButtonBar(), cc.xy(3, 7));

        builder.add(buildMainButtonBar(), cc.xyw(1, 9, 4));

        add(builder.getPanel());
    }

    private Component buildRouteGroupButtonBar() {
        return ButtonBarFactory.buildCenteredBar(addRouteGroupButton,
                editGroupButton, removeRouteGroupButton);
    }

    private Component buildRouteButtonBar() {
        return ButtonBarFactory.buildCenteredBar(addRouteButton,
                duplicateRouteButton, removeRouteButton);
    }

    private Component buildMainButtonBar() {
        return ButtonBarFactory.buildRightAlignedBar(okButton);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        routeGroups = new ArrayListModel<RouteGroup>();
        routeGroupModel = new SelectionInList<RouteGroup>(
                (ListModel<?>) routeGroups);

        routeGroupList = BasicComponentFactory.createList(routeGroupModel);
        routeGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        routeGroupList.setCellRenderer(new RouteGroupListCellRenderer());

        routeGroupConfig = new RouteGroupConfigPanel();

        addRouteGroupButton = new JButton(new AddRouteGroupAction());
        editGroupButton = new JButton(new EditRouteGroupAction());
        removeRouteGroupButton = new JButton(new RemoveRouteGroupAction());

        routes = new ArrayListModel<Route>();
        routesModel = new SelectionInList<Route>();
        routesModel.setList(routes);
        routesList = new JList<Object>();
        routesList.setModel(routesModel);
        routesList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        routesList.setCellRenderer(new RouteListCellRenderer());

        addRouteButton = new JButton(new AddRouteAction());
        duplicateRouteButton = new JButton(new DuplicateRouteAction());
        removeRouteButton = new JButton(new RemoveRouteAction());

        okButton = new JButton("OK",
                ImageTools.createImageIconFromIconSet("dialog-ok-apply.png"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                okPressed = true;
                setVisible(false);
                routeManager
                        .removeRouteManagerListenerInNextEvent(RoutesConfigurationDialog.this);
            }

        });
    }

    private void initEventHandling() {
        routeGroupList
                .addListSelectionListener(new RouteGroupSelectionHandler());

        routesList.addListSelectionListener(new RouteSelectionHandler());
        routesList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {
                    new EditRouteAction().actionPerformed(null);
                }
            }

        });
        SwingUtils.addEscapeListener(this);

    }

    private void initShortcuts() {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, new AddRouteGroupAction());
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, new AddRouteAction());
    }

    @Override
    public void routesUpdated(final SortedSet<RouteGroup> updatedRouteGroups) {
        routeGroups.addAll(updatedRouteGroups);
    }

    @Override
    public void routeRemoved(final Route route) {
        if (route.getRouteGroup().equals(routeGroupModel.getSelection())) {
            routes.remove(route);
        }
    }

    @Override
    public void routeAdded(final Route route) {
        if (route.getRouteGroup().equals(routeGroupModel.getSelection())) {
            routes.add(route);
        }
    }

    @Override
    public void routeUpdated(final Route route) {
        if (route.getRouteGroup().equals(routeGroupModel.getSelection())) {
            routes.remove(route);
            routes.add(route);
        }
    }

    @Override
    public void routeGroupAdded(final RouteGroup routeGroup) {
        routeGroups.add(routeGroup);
        routeGroupModel.setSelection(routeGroup);
    }

    @Override
    public void routeGroupRemoved(final RouteGroup routeGroup) {
        RouteGroup selection = routeGroupModel.getSelection();
        if (selection != null && selection.equals(routeGroup)) {
            routes.clear();
        }
        routeGroups.remove(routeGroup);

    }

    @Override
    public void routeGroupUpdated(final RouteGroup routeGroup) {
        routeGroups.remove(routeGroup);
        routeGroups.add(routeGroup);
    }

    @Override
    public void failure(final AdHocServiceException serviceException) {

    }

    private int getNextRouteNumber(final RouteGroup selectedRouteGroup) {
        final int nextNumber = routeManager.getNextFreeRouteNumber();
        return nextNumber;
    }

    /**
     * Used to renders RouteGroups in JLists and JComboBoxes. If the combo box
     * selection is null, an empty text <code>""</code> is rendered.
     */
    private static final class RouteGroupListCellRenderer extends
            DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            final Component component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            final RouteGroup group = (RouteGroup) value;
            setText(group == null ? "" : (" " + group.getName()));
            return component;
        }
    }

    /**
     * Used to renders Route in JLists and JComboBoxes. If the combo box
     * selection is null, an empty text <code>""</code> is rendered.
     */
    private static final class RouteListCellRenderer extends
            DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            final Component component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            final Route route = (Route) value;
            setText(route == null ? "" : ("#" + route.getNumber() + ": " + route.getName()));
            return component;
        }
    }

    /**
     * Sets the selected RouteGroup as bean in the details model.
     */
    private final class RouteGroupSelectionHandler implements
            ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (routeGroupList.getSelectedIndex() == -1) {
                routeGroupList.setSelectedIndex(0);
            }
            final RouteGroup selectedGroup = (RouteGroup) routeGroupList
                    .getSelectedValue();
            if (selectedGroup == null) {
                return;
            }
            routes.clear();
            routes.addAll(selectedGroup.getRoutes());
            routeGroupConfig.setRouteGroup(selectedGroup);
        }
    }

    /**
     * Sets the selected Route as bean in the details model.
     */
    private final class RouteSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (routesList.getSelectedIndex() == -1) {
                routesList.setSelectedIndex(0);
            }
            final Route selectedRoute = (Route) routesList.getSelectedValue();
            if (selectedRoute == null) {
                return;
            }
        }
    }

    private class EditRouteGroupAction extends AbstractAction {

        public EditRouteGroupAction() {
            super("Edit Group", ImageTools
                    .createImageIconFromIconSet("edit.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final RouteGroup groupToEdit = routeGroupModel.getSelection();

            routeManager.updateRouteGroup(groupToEdit);

        }
    }

    private class AddRouteGroupAction extends AbstractAction {


        public AddRouteGroupAction() {
            super("Add Group", ImageTools
                    .createImageIconFromIconSet("list-add.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final String newRouteGroupName = JOptionPane.showInputDialog(
                    RoutesConfigurationDialog.this,
                    "Enter the name of the new route group", "Add route group",
                    JOptionPane.QUESTION_MESSAGE);

            if (newRouteGroupName == null || newRouteGroupName.equals("")) {
                return;
            }

            final RouteGroup newRouteGroup = new RouteGroup();
            newRouteGroup.setName(newRouteGroupName);

            routeManager.addRouteGroup(newRouteGroup);

        }

    }

    private class RemoveRouteGroupAction extends AbstractAction {


        public RemoveRouteGroupAction() {
            super("Remove Group", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final RouteGroup routeGroupToDelete = routeGroupModel
                    .getSelection();
            if (routeGroupToDelete == null) {
                JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
                        "Please select a Route-Group", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final int response = JOptionPane.showConfirmDialog(
                    RoutesConfigurationDialog.this,
                    "Really remove Route-Group '"
                            + routeGroupToDelete.getName() + "' ?",
                    "Remove Route-Group", JOptionPane.YES_NO_OPTION
            );
            if (response == JOptionPane.YES_OPTION) {
                try {
                    routeManager.removeRouteGroup(routeGroupToDelete);
                    routeGroupConfig.setRouteGroup(null);
                    routeGroups.remove(routeGroupToDelete);
                } catch (final ManagerException e1) {
                    ctx.getMainApp().handleException(e1);
                }
            }
        }
    }

    private class AddRouteAction extends AbstractAction {


        public AddRouteAction() {
            super("Add", ImageTools.createImageIconFromIconSet("list-add.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final RouteGroup selectedRouteGroup = routeGroupModel
                    .getSelection();
            if (selectedRouteGroup == null) {
                JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
                        "Please select a route group", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final int nextNumber = getNextRouteNumber(selectedRouteGroup);
            if (nextNumber == -1) {
                return;
            }

            final Route newRoute = RouteHelper.createDefaultRoute(routeManager,
                    nextNumber);

            new RouteConfig(RoutesConfigurationDialog.this, ctx, newRoute,
                    selectedRouteGroup, true);
        }
    }

    private class DuplicateRouteAction extends AbstractAction {


        public DuplicateRouteAction() {
            super("Duplicate", ImageTools
                    .createImageIconFromIconSet("editcopy.png"));

        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final RouteGroup selectedRouteGroup = routeGroupModel
                    .getSelection();
            if (selectedRouteGroup == null) {
                JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
                        "Please select a route group", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Route selectedRoute = routesModel.getElementAt(routesList
                    .getSelectedIndex());
            if (selectedRoute == null) {
                return;
            }
            final int nextNumber = getNextRouteNumber(selectedRouteGroup);
            if (nextNumber == -1) {
                return;
            }

            final Route newRoute = RouteHelper.copyRoute(routeManager,
                    selectedRoute, selectedRouteGroup, nextNumber);

            new RouteConfig(RoutesConfigurationDialog.this, ctx, newRoute,
                    selectedRouteGroup, true);
        }
    }

    private class EditRouteAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {

            final Route route = (Route) routesList.getSelectedValue();
            new RouteConfig(RoutesConfigurationDialog.this, ctx, route,
                    route.getRouteGroup(), false);
        }
    }

    private class RemoveRouteAction extends AbstractAction {

        public RemoveRouteAction() {
            super("Remove", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final int[] rows = routesList.getSelectedIndices();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
                        "Please select a route", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final int response = JOptionPane.showConfirmDialog(
                    RoutesConfigurationDialog.this, "Really remove route(s) ?",
                    "Remove route(s)", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                final Set<Route> routesToRemove = new HashSet<Route>();
                for (final int row : rows) {
                    routesToRemove.add(routesModel.getElementAt(row));
                }
                for (final Route route : routesToRemove) {
                    routeManager.removeRoute(route);
                }
                routesList.clearSelection();
            }
        }
    }
}
