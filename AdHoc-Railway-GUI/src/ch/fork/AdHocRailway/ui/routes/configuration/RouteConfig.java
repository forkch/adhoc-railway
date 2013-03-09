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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.ThreeDigitDisplay;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RouteConfig extends JDialog {
	private boolean okPressed;
	private boolean cancelPressed;

	private final PresentationModel<Route> presentationModel;
	private JButton okButton;
	private JButton cancelButton;
	private PanelBuilder builder;
	private JSpinner routeNumberField;
	private JTextField routeNameField;
	private SelectionInList<RouteItem> routeItemModel;
	private JTable routeItemTable;
	private JButton addRouteItemButton;
	private JButton recordRouteButton;
	public StringBuffer enteredNumberKeys;
	private JButton removeRouteItemButton;
	public ThreeDigitDisplay digitDisplay;
	private JTextField routeOrientationField;

	public RouteConfig(JDialog owner, Route myRoute) {
		this(owner, new PresentationModel<Route>(myRoute));
	}

	public RouteConfig(Frame owner, Route myRoute) {
		super(owner, "Route Config", true);
		this.presentationModel = new PresentationModel<Route>(myRoute);
		initGUI();
	}

	public RouteConfig(JDialog owner, PresentationModel<Route> presentationModel) {
		super(owner, "Route Config", true);
		this.presentationModel = presentationModel;
		initGUI();
	}

	public RouteConfig(Frame owner, PresentationModel<Route> presentationModel) {
		super(owner, "Route Config", true);
		this.presentationModel = presentationModel;
		initGUI();
	}

	private void initGUI() {
		// usedRouteNumbers = routePersistence.getUsedRouteNumbers();
		// usedRouteNumbers.remove(presentationModel.getBean().getNumber());
		buildPanel();
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	private void initComponents() {

		routeNumberField = new JSpinner();
		routeNumberField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel("number"), 1, // defaultValue
				0, // minValue
				1000, // maxValue
				1)); // step

		routeOrientationField = BasicComponentFactory
				.createTextField(presentationModel.getModel("orientation"));
		routeOrientationField.setColumns(5);

		routeNameField = BasicComponentFactory
				.createTextField(presentationModel.getModel("name"));
		routeNameField.setColumns(5);

		routeItemModel = new SelectionInList<RouteItem>();
		routeItemTable = new JTable();
		routeItemTable.setModel(new RouteItemTableModel(routeItemModel));
		routeItemTable.setRowHeight(30);
		routeItemTable.setSelectionModel(new SingleListSelectionAdapter(
				routeItemModel.getSelectionIndexHolder()));

		routeItemModel.setList(new ArrayList<RouteItem>(presentationModel
				.getBean().getRouteItems()));

		TableColumn routedStateColumn = routeItemTable.getColumnModel()
				.getColumn(1);
		routedStateColumn.setCellRenderer(new RoutedTurnoutStateCellRenderer());

		addRouteItemButton = new JButton(new AddRouteItemAction());
		recordRouteButton = new JButton(new RecordRouteAction());
		removeRouteItemButton = new JButton(new RemoveRouteItemAction());

		digitDisplay = new ThreeDigitDisplay();

		okButton = new JButton(new ApplyChangesAction());
		cancelButton = new JButton(new CancelAction());
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout("pref, 5dlu, pref, 3dlu, pref:grow",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
		builder = new PanelBuilder(layout);

		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.add(digitDisplay, cc.xywh(1, 1, 1, 7));
		builder.addLabel("Route Number", cc.xy(3, 1));
		builder.add(routeNumberField, cc.xy(5, 1));

		builder.addLabel("Route Name", cc.xy(3, 3));
		builder.add(routeNameField, cc.xy(5, 3));

		builder.addLabel("Route Orientation", cc.xy(3, 5));
		builder.add(routeOrientationField, cc.xy(5, 5));

		builder.add(new JScrollPane(routeItemTable), cc.xyw(3, 7, 3));

		builder.add(buildRouteItemButtonBar(), cc.xyw(3, 9, 3));
		builder.add(buildButtonBar(), cc.xyw(1, 11, 5));

		add(builder.getPanel());
	}

	private Component buildRouteItemButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteItemButton,
				recordRouteButton, removeRouteItemButton);
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
	}

	private class AddRouteItemAction extends AbstractAction {

		public AddRouteItemAction() {
			super("Add Turnout", ImageTools
					.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

		}
	}

	private class RecordRouteAction extends AbstractAction {

		private boolean recording;
		private JWindow numberDisplayDialog;

		public RecordRouteAction() {
			super("Record", ImageTools
					.createImageIconFromIconSet("record_off.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!recording) {
				Route selectedRoute = (presentationModel.getBean());
				if (selectedRoute == null) {
					JOptionPane
							.showMessageDialog(
									RouteConfig.this,
									"Please select a route",
									"Error",
									JOptionPane.ERROR_MESSAGE,
									ImageTools
											.createImageIconFromIconSet("messagebox_critical.png"));
					return;
				}

				recordRouteButton.setIcon(ImageTools
						.createImageIconFromIconSet("record.png"));
				initKeyboardActions(selectedRoute);
				recording = true;
			} else {
				recordRouteButton.setIcon(ImageTools
						.createImageIconFromIconSet("record_off.png"));
				recording = false;
			}
		}

		private void initKeyboardActions(Route route) {
			enteredNumberKeys = new StringBuffer();
			JPanel routeItemPanel = builder.getPanel();
			JPanel[] panels = new JPanel[] { routeItemPanel, digitDisplay };
			for (int i = 0; i <= 10; i++) {
				for (JPanel p : panels) {
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
			for (JPanel p : panels) {
				KeyBoardLayout kbl = Preferences.getInstance()
						.getKeyBoardLayout();
				InputMap inputMap = p
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

		public SwitchingAction(Route route) {
			this.route = route;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			String enteredNumberAsString = enteredNumberKeys.toString();
			if (enteredNumberAsString.equals("")) {
				return;
			}
			int enteredNumber = Integer.parseInt(enteredNumberAsString);
			System.out.println(enteredNumber);
			Turnout turnout;
			try {
				RouteManager routePersistence = AdHocRailway.getInstance()
						.getRoutePersistence();
				TurnoutManager turnoutPersistence = AdHocRailway.getInstance()
						.getTurnoutPersistence();
				turnout = turnoutPersistence.getTurnoutByNumber(enteredNumber);
				System.out.println(turnout);
				if (turnout == null) {
					JOptionPane
							.showMessageDialog(
									RouteConfig.this,
									"Turnout " + enteredNumber
											+ " does not exist",
									"Error",
									JOptionPane.ERROR_MESSAGE,
									ImageTools
											.createImageIconFromIconSet("messagebox_critical.png"));
				} else {
					TurnoutState routedState = null;
					if (this instanceof CurvedLeftAction) {
						// ThreeWay LEFT
						routedState = TurnoutState.LEFT;
					} else if (this instanceof StraightAction) {
						// ThreeWay STRAIGHT
						routedState = TurnoutState.STRAIGHT;
					} else if (this instanceof CurvedRightAction) {
						// ThreeWay RIGHT
						routedState = TurnoutState.RIGHT;
					} else if (this instanceof EnableRouteAction) {
						// CURVED
						if (!turnout.isThreeWay()) {
							switch (turnout.getDefaultState()) {
							case STRAIGHT:
								routedState = TurnoutState.LEFT;
								break;
							case LEFT:
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
					SortedSet<RouteItem> itemsOfRoute = route.getRouteItems();
					for (RouteItem item : itemsOfRoute) {
						if (item.getTurnout().equals(turnout)) {
							itemToRemove = item;
							break;
						}
					}
					if (itemToRemove != null) {
						routePersistence.removeRouteItem(itemToRemove);
					}
					RouteItem i = new RouteItem();
					i.setRoute(route);
					i.setRoutedState(routedState);
					i.setTurnout(turnout);

					try {
						routePersistence.addRouteItem(i);
						List<RouteItem> routeItems = new ArrayList<RouteItem>(
								route.getRouteItems());
						routeItemModel.setList(routeItems);
					} catch (RouteManagerException e1) {
						e1.printStackTrace();
					}
				}
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} finally {
				enteredNumberKeys = new StringBuffer();
				digitDisplay.reset();
			}
		}
	}

	private class CurvedLeftAction extends SwitchingAction {
		public CurvedLeftAction(Route route) {
			super(route);
		}
	}

	private class StraightAction extends SwitchingAction {
		public StraightAction(Route route) {
			super(route);
		}
	}

	private class CurvedRightAction extends SwitchingAction {
		public CurvedRightAction(Route route) {
			super(route);
		}
	}

	private class EnableRouteAction extends SwitchingAction {
		public EnableRouteAction(Route route) {
			super(route);
		}
	}

	private class DisableRouteAction extends SwitchingAction {
		public DisableRouteAction(Route route) {
			super(route);
		}
	}

	private class NumberEnteredAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {

			enteredNumberKeys.append(e.getActionCommand());
			String switchNumberAsString = enteredNumberKeys.toString();
			int switchNumber = Integer.parseInt(switchNumberAsString);
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
					.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Route selectedRoute = (presentationModel.getBean());
			RouteItem routeItem = routeItemModel.getSelection();
			if (routeItem == null) {
				return;
			}
			RouteManager routePersistence = AdHocRailway.getInstance()
					.getRoutePersistence();
			routePersistence.removeRouteItem(routeItem);
			List<RouteItem> routeItems = new ArrayList<RouteItem>(
					selectedRoute.getRouteItems());
			routeItemModel.setList(routeItems);
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {

		public ApplyChangesAction() {
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final RouteManager routePersistence = AdHocRailway.getInstance()
					.getRoutePersistence();
			Route route = presentationModel.getBean();

			routePersistence.addRouteManagerListener(new RouteAddListener() {

				@Override
				public void routeAdded(Route route) {
					success(routePersistence, route);
				}

				@Override
				public void routeUpdated(Route route) {
					success(routePersistence, route);

				}

				private void success(final RouteManager routePersistence,
						Route route) {
					routePersistence
							.removeRouteManagerListenerInNextEvent(this);

					okPressed = true;
					RouteConfig.this.setVisible(false);

					if (Preferences.getInstance().getBooleanValue(
							PreferencesKeys.AUTOSAVE)) {
						AdHocRailway.getInstance().saveActualFile();
					}
				}

				@Override
				public void failure(RouteManagerException routeManagerException) {
					System.out.println("failure");
				}

			});
			if (route.getId() == -1) {
				routePersistence.addRoute(route);
			} else {
				routePersistence.updateRoute(route);
			}

		}
	}

	class CancelAction extends AbstractAction {

		public CancelAction() {
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			okPressed = false;
			cancelPressed = true;
			RouteConfig.this.setVisible(false);
		}
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

}
