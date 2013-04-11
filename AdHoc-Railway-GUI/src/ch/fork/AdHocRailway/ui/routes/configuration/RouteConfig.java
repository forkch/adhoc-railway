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

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.ErrorPanel;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.SwingUtils;
import ch.fork.AdHocRailway.ui.ThreeDigitDisplay;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.routes.RouteWidget;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.Trigger;
import com.jgoodies.forms.factories.ButtonBarFactory;

public class RouteConfig extends JDialog {
	private static final long serialVersionUID = -6408833917980514400L;
	private boolean okPressed;
	private boolean cancelPressed;

	private final PresentationModel<Route> presentationModel;
	private JButton okButton;
	private JButton cancelButton;
	private JSpinner routeNumberSpinner;
	private JTextField routeNameField;
	private SelectionInList<RouteItem> routeItemModel;
	private JTable routeItemTable;
	private JButton recordRouteButton;
	public StringBuffer enteredNumberKeys;
	private JButton removeRouteItemButton;
	public ThreeDigitDisplay digitDisplay;
	private JTextField routeOrientationField;
	private JPanel mainPanel;
	private final Trigger trigger = new Trigger();
	private final RouteGroup selectedRouteGroup;
	private ErrorPanel errorPanel;
	private RouteWidget testRouteWidget;
	private final Route testRoute;
	private BufferedValueModel routeNumberModel;
	private BufferedValueModel routeNameModel;
	private BufferedValueModel routeOrientationModel;
	private final RouteContext routeContext;
	private final RouteManager routePersistence;
	private final TurnoutManager turnoutManager;

	public RouteConfig(final JDialog owner, final RouteContext ctx,
			final Route myRoute, final RouteGroup selectedRouteGroup) {
		super(owner, "Route Config", true);
		this.routeContext = ctx;
		routePersistence = ctx.getRouteManager();
		turnoutManager = ctx.getTurnoutManager();

		testRoute = RouteHelper.copyRoute(myRoute);
		this.selectedRouteGroup = selectedRouteGroup;
		this.presentationModel = new PresentationModel<Route>(myRoute, trigger);
		initGUI();
	}

	public RouteConfig(final Frame owner, final RouteContext ctx,
			final Route myRoute, final RouteGroup selectedRouteGroup) {
		super(owner, "Route Config", true);
		this.routeContext = ctx;
		routePersistence = ctx.getRouteManager();
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

		pack();
		setLocationRelativeTo(getParent());
		SwingUtils.addEscapeListener(this);
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
				.getBean().getRouteItems()));

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

	class RouteChangeListener implements PropertyChangeListener {

		private final String property;

		public RouteChangeListener(final String property) {
			this.property = property;
		}

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if (property.equals(Route.PROPERTYNAME_ROUTE_ITEMS)) {
				@SuppressWarnings("unchecked")
				final SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(
						(ArrayList<RouteItem>) evt.getNewValue());
				RouteHelper.update(testRoute, property, routeItems);
			} else {
				RouteHelper.update(testRoute, property, evt.getNewValue());
			}
			testRouteWidget.setRoute(testRoute);
		}
	}

	private Component buildRouteItemButtonBar() {
		return ButtonBarFactory.buildCenteredBar(recordRouteButton,
				removeRouteItemButton);
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
	}

	private class RecordRouteAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1287566434458519970L;
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
											.createImageIconFromIconSet("dialog-error.png"));
					return;
				}

				recordRouteButton.setIcon(ImageTools
						.createImageIconFromIconSet("media-record.png"));
				initKeyboardActions(selectedRoute);
				recording = true;
			} else {
				recordRouteButton.setIcon(ImageTools
						.createImageIconFromIconSet("media-playback-stop.png"));
				recording = false;
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
							JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				}
			}
			for (final JPanel p : panels) {
				final KeyBoardLayout kbl = Preferences.getInstance()
						.getKeyBoardLayout();
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
		/**
		 * 
		 */
		private static final long serialVersionUID = -126103872681435146L;
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
											.createImageIconFromIconSet("dialog-error.png"));
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
							.getRouteItems();
					for (final RouteItem item : itemsOfRoute) {
						if (item.getTurnout().equals(turnout)) {
							itemToRemove = item;
							break;
						}
					}
					if (itemToRemove != null) {
						routePersistence.removeRouteItem(itemToRemove);
					}
					final RouteItem i = new RouteItem();
					i.setRoute(route);
					i.setRoutedState(routedState);
					i.setTurnout(turnout);

					try {
						routePersistence.addRouteItem(i);
						final List<RouteItem> routeItems = new ArrayList<RouteItem>(
								route.getRouteItems());
						routeItemModel.setList(routeItems);
					} catch (final RouteManagerException e1) {
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
		/**
		 * 
		 */
		private static final long serialVersionUID = 2298539073910194594L;

		public CurvedLeftAction(final Route route) {
			super(route);
		}
	}

	private class StraightAction extends SwitchingAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2866441796856357663L;

		public StraightAction(final Route route) {
			super(route);
		}
	}

	private class CurvedRightAction extends SwitchingAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6106999361346507826L;

		public CurvedRightAction(final Route route) {
			super(route);
		}
	}

	private class EnableRouteAction extends SwitchingAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2554594488359463589L;

		public EnableRouteAction(final Route route) {
			super(route);
		}
	}

	private class DisableRouteAction extends SwitchingAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -150319621580243688L;

		public DisableRouteAction(final Route route) {
			super(route);
		}
	}

	private class NumberEnteredAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4851780584974377445L;

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

		/**
		 * 
		 */
		private static final long serialVersionUID = 1122483690867740137L;

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
			routePersistence.removeRouteItem(routeItem);
			final List<RouteItem> routeItems = new ArrayList<RouteItem>(
					selectedRoute.getRouteItems());
			routeItemModel.setList(routeItems);
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7279880783090999221L;

		public ApplyChangesAction() {
			super("OK", ImageTools
					.createImageIconFromIconSet("dialog-ok-apply.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			trigger.triggerCommit();
			final Route route = presentationModel.getBean();

			routePersistence.addRouteManagerListener(new RouteAddListener() {

				@Override
				public void routeAdded(final Route route) {
					success(routePersistence, route);
				}

				@Override
				public void routeUpdated(final Route route) {
					success(routePersistence, route);

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
						final RouteManagerException routeManagerException) {

					errorPanel.setErrorText(routeManagerException.getMessage());
				}

			});
			if (route.getId() == -1) {
				routePersistence.addRouteToGroup(route, selectedRouteGroup);
			} else {
				routePersistence.updateRoute(route);
			}

		}
	}

	class CancelAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8783083615316968787L;

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

	public boolean isCancelPressed() {
		return cancelPressed;
	}

}
