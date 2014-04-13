/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutConfig.java 157 2008-03-29 18:31:54Z fork_ch $
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

import ch.fork.AdHocRailway.domain.turnouts.*;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.GlobalKeyShortcutHelper;
import ch.fork.AdHocRailway.ui.ThreeDigitDisplay;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;
import ch.fork.AdHocRailway.ui.tools.ImageTools;
import ch.fork.AdHocRailway.ui.tools.SwingUtils;
import ch.fork.AdHocRailway.ui.widgets.ErrorPanel;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.Trigger;
import com.jgoodies.forms.factories.ButtonBarFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

public class RouteConfig extends JDialog {
    private final Trigger trigger = new Trigger();
    public StringBuffer enteredNumberKeys;
    public ThreeDigitDisplay digitDisplay;
    private PresentationModel<Route> presentationModel;
    private RouteGroup selectedRouteGroup;
    private Route testRoute;
    private RouteContext routeContext;
    private boolean createRoute;
    private RouteManager routeManager;
    private TurnoutManager turnoutManager;
    private boolean okPressed;
    private boolean cancelPressed;
    private JButton okButton;
    private JButton cancelButton;
    private JSpinner routeNumberSpinner;
    private JTextField routeNameField;
    private SelectionInList<RouteItem> routeItemModel;
    private JTable routeItemTable;
    private JButton recordRouteButton;
    private JButton removeRouteItemButton;
    private JTextField routeOrientationField;
    private JPanel mainPanel;
    private ErrorPanel errorPanel;
    private RouteWidget testRouteWidget;
    private BufferedValueModel routeNumberModel;
    private BufferedValueModel routeNameModel;
    private BufferedValueModel routeOrientationModel;

    public RouteConfig(final JDialog owner, final RouteContext ctx,
                       final Route myRoute, final RouteGroup selectedRouteGroup, boolean createRoute) {
        super(owner, "Route Config", true);
        init(ctx, myRoute, selectedRouteGroup, createRoute);

    }

    public RouteConfig(final Frame owner, final RouteContext ctx,
                       final Route myRoute, final RouteGroup selectedRouteGroup, boolean createRoute) {
        super(owner, "Route Config", true);
        init(ctx, myRoute, selectedRouteGroup, createRoute);
    }

    private void init(final RouteContext ctx,
                      final Route myRoute, final RouteGroup selectedRouteGroup, boolean createRoute) {
        this.routeContext = ctx;
        this.createRoute = createRoute;
        routeManager = ctx.getRouteManager();
        turnoutManager = ctx.getTurnoutManager();

        testRoute = RouteHelper.copyRoute(myRoute);
        this.selectedRouteGroup = selectedRouteGroup;
        this.presentationModel = new PresentationModel<Route>(myRoute, trigger);
        initGUI();
    }

    private void initGUI() {
        initComponents();
        buildPanel();
        initEventHandling();
        initShortcuts();

        routeNameField.requestFocus();

        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void initComponents() {

        routeNumberModel = getBufferedModel(Route.PROPERTYNAME_NUMBER);
        routeOrientationModel = getBufferedModel(Route.PROPERTYNAME_ORIENTATION);
        routeNameModel = getBufferedModel(Route.PROPERTYNAME_NAME);

        routeNumberSpinner = new JSpinner();
        routeNumberSpinner.setModel(SpinnerAdapterFactory.createNumberAdapter(
                routeNumberModel, 1, // defaultValue
                0, // minValue
                1000, // maxValue
                1)); // step

        routeOrientationField = BasicComponentFactory
                .createTextField(routeOrientationModel);

        routeNameField = BasicComponentFactory.createTextField(routeNameModel);

        routeItemModel = new SelectionInList<RouteItem>();
        routeItemTable = new JTable();
        routeItemTable.setModel(new RouteItemTableModel(routeItemModel));
        routeItemTable.setRowHeight(30);
        routeItemTable.setSelectionModel(new SingleListSelectionAdapter(
                routeItemModel.getSelectionIndexHolder()));

        routeItemModel.setList(new ArrayList<RouteItem>(presentationModel
                .getBean().getRoutedTurnouts()));

        final TableColumn routedStateColumn = routeItemTable.getColumnModel()
                .getColumn(1);
        routedStateColumn.setCellRenderer(new RoutedTurnoutStateCellRenderer(
                routeContext.getTurnoutManager()));

        recordRouteButton = new JButton(new RecordRouteAction());
        removeRouteItemButton = new JButton(new RemoveRouteItemAction());

        digitDisplay = new ThreeDigitDisplay();

        errorPanel = new ErrorPanel();

        testRouteWidget = new RouteWidget(routeContext, testRoute, true);
        okButton = new JButton(new ApplyChangesAction());
        cancelButton = new JButton(new CancelAction());
    }

    private BufferedValueModel getBufferedModel(final String propertynameNumber) {
        return presentationModel.getBufferedModel(propertynameNumber);

    }

    private void buildPanel() {

        mainPanel = new JPanel(new MigLayout());
        mainPanel.add(digitDisplay);

        final JPanel infoPanel = new JPanel(new MigLayout());
        infoPanel.add(new JLabel("Route Number"));
        infoPanel.add(routeNumberSpinner, "wrap, w 150!");

        infoPanel.add(new JLabel("Route Name"));
        infoPanel.add(routeNameField, "wrap, w 150!");

        infoPanel.add(new JLabel("Route Orienation"));
        infoPanel.add(routeOrientationField, "w 150!, top");

        mainPanel.add(infoPanel, "gap unrelated");
        mainPanel.add(testRouteWidget, "wrap");
        mainPanel.add(buildRouteItemButtonBar(), "span 3, align center, wrap");
        mainPanel.add(new JScrollPane(routeItemTable), "span 3, grow x, wrap");

        mainPanel.add(errorPanel, "span 2");
        mainPanel.add(buildButtonBar(), "span 1, align right");

        add(mainPanel);
    }

    private void initEventHandling() {

        routeNumberModel.addValueChangeListener(new RouteChangeListener(
                Route.PROPERTYNAME_NUMBER));
        routeNameModel.addValueChangeListener(new RouteChangeListener(
                Route.PROPERTYNAME_NAME));
        routeOrientationModel.addValueChangeListener(new RouteChangeListener(
                Route.PROPERTYNAME_ORIENTATION));
        routeItemModel.addPropertyChangeListener(new RouteChangeListener(
                Route.PROPERTYNAME_ROUTE_ITEMS));
    }

    private void initShortcuts() {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_R, 0, new RecordRouteAction());
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_ENTER, 0, new ApplyChangesAction());
    }

    private Component buildRouteItemButtonBar() {
        return ButtonBarFactory.buildCenteredBar(recordRouteButton,
                removeRouteItemButton);
    }

    public boolean validate(final Route routeToValidate) {
        boolean valid = true;
        if (RouteHelper.isNumberValid(routeToValidate,
                presentationModel.getBean(), routeManager)) {
            setSpinnerColor(routeNumberSpinner,
                    UIConstants.DEFAULT_TEXTFIELD_COLOR);
            okButton.setEnabled(true);
        } else {
            setSpinnerColor(routeNumberSpinner, UIConstants.ERROR_COLOR);
            valid = false;
            okButton.setEnabled(false);
        }

        return valid;
    }

    private void setSpinnerColor(final JSpinner spinner, final Color color) {
        final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
                .getEditor();
        editor.getTextField().setBackground(color);
    }

    private JComponent buildButtonBar() {
        return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    class RouteChangeListener implements PropertyChangeListener {

        private final String property;

        public RouteChangeListener(final String property) {
            this.property = property;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equalsIgnoreCase("selectionIndex")) {
                return;
            }
            if (evt.getPropertyName().equalsIgnoreCase("selection")) {
                return;
            }
            if (property.equals(Route.PROPERTYNAME_ROUTE_ITEMS)) {
                // final SortedSet<RouteItem> routeItems = new
                // TreeSet<RouteItem>(
                // (ArrayList<RouteItem>) evt.getNewValue());
                // RouteHelper.update(testRoute, property, routeItems);
            } else {
                RouteHelper.update(testRoute, property, evt.getNewValue());
            }
            testRouteWidget.setRoute(testRoute);

            if (!validate(testRoute)) {
                return;
            }
        }
    }

    private class RecordRouteAction extends AbstractAction {

        private boolean recording;

        public RecordRouteAction() {
            super("Record", ImageTools
                    .createImageIconFromIconSet("media-playback-stop.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (!recording) {
                final Route selectedRoute = (presentationModel.getBean());
                if (selectedRoute == null) {
                    JOptionPane
                            .showMessageDialog(
                                    RouteConfig.this,
                                    "Please select a route",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE,
                                    ImageTools
                                            .createImageIconFromIconSet("dialog-error.png")
                            );
                    return;
                }

                initKeyboardActions(selectedRoute);
                recordRouteButton.setIcon(ImageTools
                        .createImageIconFromIconSet("media-record.png"));
                recording = true;
            } else {
                removeKeyboarAtions();
                recordRouteButton.setIcon(ImageTools
                        .createImageIconFromIconSet("media-playback-stop.png"));
                recording = false;
            }
        }

        private void removeKeyboarAtions() {
            final Set<JPanel> panels = new HashSet<JPanel>();

            panels.add(digitDisplay);
            panels.add(mainPanel);

            final KeyBoardLayout kbl = Preferences.getInstance()
                    .getKeyBoardLayout();
            for (final JPanel p : panels) {
                for (int i = 0; i <= 10; i++) {
                    p.unregisterKeyboardAction(KeyStroke.getKeyStroke(Integer.toString(i)));
                    p.unregisterKeyboardAction(KeyStroke.getKeyStroke("NUMPAD"
                                    + Integer.toString(i))
                    );
                }
                for (KeyStroke keyStroke1 : kbl.getKeys("CurvedLeft")) {
                    p.unregisterKeyboardAction(keyStroke1);
                }
                for (KeyStroke keyStroke : kbl.getKeys("CurvedRight")) {
                    p.unregisterKeyboardAction(keyStroke);
                }
                for (KeyStroke keyStroke : kbl.getKeys("Straight")) {
                    p.unregisterKeyboardAction(keyStroke);
                }
                for (KeyStroke keyStroke : kbl.getKeys("EnableRoute")) {
                    p.unregisterKeyboardAction(keyStroke);
                }
                for (KeyStroke keyStroke : kbl.getKeys("DisableRoute")) {
                    p.unregisterKeyboardAction(keyStroke);
                }

            }
        }

        private void initKeyboardActions(final Route route) {
            enteredNumberKeys = new StringBuffer();
            final Set<JPanel> panels = new HashSet<JPanel>();

            panels.add(digitDisplay);
            panels.add(mainPanel);
            for (final JPanel p : panels) {
                for (int i = 0; i <= 10; i++) {
                    p.registerKeyboardAction(new NumberEnteredAction(),
                            Integer.toString(i),
                            KeyStroke.getKeyStroke(Integer.toString(i)),
                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                    p.registerKeyboardAction(
                            new NumberEnteredAction(),
                            Integer.toString(i),
                            KeyStroke.getKeyStroke("NUMPAD"
                                    + Integer.toString(i)),
                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                    );
                }
            }
            final KeyBoardLayout kbl = Preferences.getInstance()
                    .getKeyBoardLayout();
            for (final JPanel p : panels) {
                final InputMap inputMap = p
                        .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                p.getActionMap().put("CurvedLeft", new CurvedLeftAction(route));
                kbl.assignKeys(inputMap, "CurvedLeft");
                p.getActionMap().put("CurvedRight",
                        new CurvedRightAction(route));
                kbl.assignKeys(inputMap, "CurvedRight");
                p.getActionMap().put("Straight", new StraightAction(route));
                kbl.assignKeys(inputMap, "Straight");
                p.getActionMap().put("EnableRoute",
                        new EnableRouteAction(route));
                kbl.assignKeys(inputMap, "EnableRoute");
                p.getActionMap().put("DisableRoute",
                        new DisableRouteAction(route));
                kbl.assignKeys(inputMap, "DisableRoute");
            }
        }
    }

    private abstract class SwitchingAction extends AbstractAction {
        private final Route route;

        public SwitchingAction(final Route route) {
            this.route = route;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final String enteredNumberAsString = enteredNumberKeys.toString();
            if (enteredNumberAsString.equals("")) {
                return;
            }
            final int enteredNumber = Integer.parseInt(enteredNumberAsString);
            Turnout turnout;
            try {
                turnout = turnoutManager.getTurnoutByNumber(enteredNumber);
                if (turnout == null) {
                    JOptionPane
                            .showMessageDialog(
                                    RouteConfig.this,
                                    "Turnout " + enteredNumber
                                            + " does not exist",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE,
                                    ImageTools
                                            .createImageIconFromIconSet("dialog-error.png")
                            );
                } else {
                    TurnoutState routedState = null;
                    if (this instanceof CurvedLeftAction) {
                        routedState = TurnoutState.LEFT;
                    } else if (this instanceof StraightAction) {
                        routedState = TurnoutState.STRAIGHT;
                    } else if (this instanceof CurvedRightAction) {
                        routedState = TurnoutState.RIGHT;
                    } else if (this instanceof EnableRouteAction) {
                        // CURVED
                        if (!turnout.isThreeWay()) {
                            switch (turnout.getDefaultState()) {
                                case STRAIGHT:
                                    routedState = TurnoutState.LEFT;
                                    break;
                                case LEFT:
                                case RIGHT:
                                case UNDEF:
                                    routedState = TurnoutState.STRAIGHT;
                                    break;
                            }
                        } else {
                            routedState = TurnoutState.LEFT;
                        }
                    } else if (this instanceof DisableRouteAction) {
                        // STRAIGHT
                        routedState = turnout.getDefaultState();
                    }

                    RouteItem itemToRemove = null;
                    final SortedSet<RouteItem> itemsOfRoute = route
                            .getRoutedTurnouts();
                    for (final RouteItem item : itemsOfRoute) {
                        if (item.getTurnout().equals(turnout)) {
                            itemToRemove = item;
                            break;
                        }
                    }
                    if (itemToRemove != null) {
                        routeManager.removeRouteItem(itemToRemove);
                    }
                    final RouteItem i = new RouteItem();
                    i.setRoute(route);
                    i.setState(routedState);
                    i.setTurnout(turnout);

                    try {
                        i.setId(UUID.randomUUID().toString()); //just a dummy id
                        routeManager.addRouteItemToGroup(i, route);
                        final List<RouteItem> routeItems = new ArrayList<RouteItem>(
                                route.getRoutedTurnouts());
                        routeItemModel.setList(routeItems);
                    } catch (final ManagerException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (final NumberFormatException e1) {
                e1.printStackTrace();
            } finally {
                enteredNumberKeys = new StringBuffer();
                digitDisplay.reset();
            }
        }
    }

    private class CurvedLeftAction extends SwitchingAction {

        public CurvedLeftAction(final Route route) {
            super(route);
        }
    }

    private class StraightAction extends SwitchingAction {

        public StraightAction(final Route route) {
            super(route);
        }
    }

    private class CurvedRightAction extends SwitchingAction {

        public CurvedRightAction(final Route route) {
            super(route);
        }
    }

    private class EnableRouteAction extends SwitchingAction {

        public EnableRouteAction(final Route route) {
            super(route);
        }
    }

    private class DisableRouteAction extends SwitchingAction {

        public DisableRouteAction(final Route route) {
            super(route);
        }
    }

    private class NumberEnteredAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {

            enteredNumberKeys.append(e.getActionCommand());
            final String switchNumberAsString = enteredNumberKeys.toString();
            final int switchNumber = Integer.parseInt(switchNumberAsString);
            if (switchNumber > 999) {
                enteredNumberKeys = new StringBuffer();
                digitDisplay.reset();
                return;
            }
            digitDisplay.setNumber(switchNumber);

        }
    }

    private class RemoveRouteItemAction extends AbstractAction {


        public RemoveRouteItemAction() {
            super("Remove Turnout", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final Route selectedRoute = (presentationModel.getBean());
            final RouteItem routeItem = routeItemModel.getSelection();
            if (routeItem == null) {
                return;
            }
            routeManager.removeRouteItem(routeItem);
            final List<RouteItem> routeItems = new ArrayList<RouteItem>(
                    selectedRoute.getRoutedTurnouts());
            routeItemModel.setList(routeItems);
        }
    }

    class ApplyChangesAction extends AbstractAction {

        public ApplyChangesAction() {
            super("OK", ImageTools
                    .createImageIconFromIconSet("dialog-ok-apply.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            trigger.triggerCommit();
            final Route route = presentationModel.getBean();

            routeManager.addRouteManagerListener(new RouteAddListener() {

                @Override
                public void routeAdded(final Route route) {
                    success(routeManager, route);
                }

                @Override
                public void routeUpdated(final Route route) {
                    success(routeManager, route);

                }

                private void success(final RouteManager routePersistence,
                                     final Route route) {
                    routePersistence
                            .removeRouteManagerListenerInNextEvent(this);

                    okPressed = true;
                    RouteConfig.this.setVisible(false);

                }

                @Override
                public void failure(
                        final ManagerException routeManagerException) {

                    errorPanel.setErrorText(routeManagerException.getMessage());
                }

            });
            if (createRoute) {
                routeManager.addRouteToGroup(route, selectedRouteGroup);
            } else {
                routeManager.updateRoute(route);
            }

        }
    }

    class CancelAction extends AbstractAction {

        public CancelAction() {
            super("Cancel", ImageTools
                    .createImageIconFromIconSet("dialog-cancel.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            trigger.triggerFlush();
            okPressed = false;
            cancelPressed = true;
            RouteConfig.this.setVisible(false);
        }
    }

}
