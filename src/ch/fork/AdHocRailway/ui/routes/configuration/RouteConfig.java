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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.AbstractAction;
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
import javax.swing.ListModel;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.ThreeDigitDisplay;
import ch.fork.AdHocRailway.ui.TutorialUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RouteConfig extends JDialog implements PropertyChangeListener {
	private boolean						okPressed;
	private boolean						cancelPressed;
	private JSpinner					numberTextField;
	private Set<Integer>				usedTurnoutNumbers;

	private RoutePersistenceIface		routePersistence	= AdHocRailway
																	.getInstance()
																	.getRoutePersistence();
	private TurnoutPersistenceIface		turnoutPersistence	= AdHocRailway
																	.getInstance()
																	.getTurnoutPersistence();

	private PresentationModel<Route>	presentationModel;
	private JButton						okButton;
	private JButton						cancelButton;
	private PanelBuilder				builder;
	private JSpinner					routeNumberField;
	private JTextField					routeNameField;
	private SelectionInList<RouteItem>	routeItemModel;
	private JTable						routeItemTable;
	private JButton						addRouteItemButton;
	private JButton						recordRouteButton;
	public StringBuffer					enteredNumberKeys;
	private JButton						removeRouteItemButton;
	public ThreeDigitDisplay			digitDisplay;

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
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
	}

	private void initComponents() {

		routeNumberField = new JSpinner();
		routeNumberField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Route.PROPERTYNAME_NUMBER), 1, // defaultValue
				0, // minValue
				1000, // maxValue
				1)); // step

		routeNameField = BasicComponentFactory
				.createTextField(presentationModel
						.getModel(Route.PROPERTYNAME_NAME));
		routeNameField.setColumns(5);

		routeItemModel = new SelectionInList<RouteItem>();
		routeItemTable = new JTable();
		routeItemTable.setModel(new RouteItemTableModel(routeItemModel));
		routeItemTable.setRowHeight(30);
		routeItemTable.setSelectionModel(new SingleListSelectionAdapter(
				routeItemModel.getSelectionIndexHolder()));

		routeItemModel.setList(new ArrayList<RouteItem>(presentationModel.getBean().getRouteItems()));

		TableColumn routedStateColumn = routeItemTable.getColumnModel()
				.getColumn(1);
		routedStateColumn.setCellRenderer(new RoutedTurnoutStateCellRenderer());

		addRouteItemButton = new JButton(new AddRouteItemAction());
		recordRouteButton = new JButton(new RecordRouteAction());
		removeRouteItemButton = new JButton(new RemoveRouteItemAction());

		okButton = new JButton(new ApplyChangesAction());
		cancelButton = new JButton(new CancelAction());
		presentationModel.getBean().addPropertyChangeListener(this);
		validate(presentationModel.getBean());
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout("pref, 3dlu, pref:grow",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
		builder = new PanelBuilder(layout);

		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Route Number", cc.xy(1, 1));
		builder.add(routeNumberField, cc.xy(3, 1));

		builder.addLabel("Route Name", cc.xy(1, 3));
		builder.add(routeNameField, cc.xy(3, 3));

		builder.add(new JScrollPane(routeItemTable), cc.xyw(1, 5, 3));

		builder.add(buildRouteItemButtonBar(), cc.xyw(1, 7, 3));
		builder.add(buildButtonBar(), cc.xyw(1, 9, 3));

		add(builder.getPanel());
	}

	private Component buildRouteItemButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteItemButton,
				recordRouteButton, removeRouteItemButton);
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
	}

	// TableModel *************************************************************

	/**
	 * Describes how to present an Album in a JTable.
	 */
	private static final class RouteItemTableModel extends
			AbstractTableAdapter<RouteItem> {

		private static final String[]	COLUMNS	= { "Turnout Number",
														"Routed Turnout State" };

		private RouteItemTableModel(ListModel listModel) {
			super(listModel, COLUMNS);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			RouteItem routeItem = getRow(rowIndex);
			switch (columnIndex) {
			case 0:
				return routeItem.getTurnout().getNumber();
			case 1:
				return routeItem.getRoutedStateEnum();
			default:
				throw new IllegalStateException("Unknown column");
			}
		}

	}

	public void propertyChange(PropertyChangeEvent evt) {
		Route route = presentationModel.getBean();
		if (!validate(route))
			return;
	}

	private boolean validate(Route route) {
		boolean validate = true;
//		if (route.getNumber() == 0
//				|| usedTurnoutNumbers.contains(route.getNumber())) {
//			setSpinnerColor(numberTextField, UIConstants.ERROR_COLOR);
//			validate = false;
//			okButton.setEnabled(false);
//		} else {
//			setSpinnerColor(numberTextField,
//					UIConstants.DEFAULT_TEXTFIELD_COLOR);
//			okButton.setEnabled(true);
//		}

		return validate;
	}

	private void setSpinnerColor(JSpinner spinner, Color color) {
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
				.getEditor();
		editor.getTextField().setBackground(color);
	}

	private class AddRouteItemAction extends AbstractAction {

		public AddRouteItemAction() {
			super("Add Turnout", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent e) {

		}
	}

	private class RecordRouteAction extends AbstractAction {

		private boolean	recording;
		private JWindow	numberDisplayDialog;

		public RecordRouteAction() {
			super("Record", ImageTools.createImageIcon("record_off.png"));
		}

		public void actionPerformed(ActionEvent e) {
			if (!recording) {
				Route selectedRoute = (Route) (presentationModel.getBean());
				if (selectedRoute == null) {
					JOptionPane
							.showMessageDialog(
									RouteConfig.this,
									"Please select a route",
									"Error",
									JOptionPane.ERROR_MESSAGE,
									ImageTools
											.createImageIcon("messagebox_critical.png"));
					return;
				}
				digitDisplay = new ThreeDigitDisplay();
				numberDisplayDialog = new JWindow(RouteConfig.this);
				numberDisplayDialog.add(digitDisplay);
				numberDisplayDialog.pack();
				numberDisplayDialog.setAlwaysOnTop(true);

				TutorialUtils.locateOnOpticalScreenLeft3rd(numberDisplayDialog);
				recordRouteButton.setIcon(ImageTools
						.createImageIcon("record.png"));
				initKeyboardActions(selectedRoute);
				numberDisplayDialog.setVisible(true);
				recording = true;
			} else {
				recordRouteButton.setIcon(ImageTools
						.createImageIcon("record_off.png"));
				recording = false;
				numberDisplayDialog.setVisible(false);
			}
		}

		private void initKeyboardActions(Route route) {
			enteredNumberKeys = new StringBuffer();
			JPanel routeItemPanel = builder.getPanel();
			JPanel[] panels = new JPanel[] { routeItemPanel, digitDisplay };
			for (int i = 0; i <= 10; i++) {
				for (JPanel p : panels) {
					p.registerKeyboardAction(new NumberEnteredAction(), Integer
							.toString(i), KeyStroke.getKeyStroke(Integer
							.toString(i)),
							JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
					p.registerKeyboardAction(new NumberEnteredAction(), Integer
							.toString(i), KeyStroke.getKeyStroke("NUMPAD"
							+ Integer.toString(i)),
							JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				}

			}
			for (JPanel p : panels) {
				p.registerKeyboardAction(new SwitchingAction(route), "\\",
						KeyStroke.getKeyStroke(92, 0),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				p.registerKeyboardAction(new SwitchingAction(route), "\n",
						KeyStroke.getKeyStroke("ENTER"),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

				p.registerKeyboardAction(new SwitchingAction(route), "+",
						KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

				p.registerKeyboardAction(new SwitchingAction(route), "bs",
						KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

				p.registerKeyboardAction(new SwitchingAction(route), "/",
						KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

				p.registerKeyboardAction(new SwitchingAction(route), "*",
						KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

				p.registerKeyboardAction(new SwitchingAction(route), "-",
						KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			}

		}
	}

	private class SwitchingAction extends AbstractAction {
		private Route	route;

		public SwitchingAction(Route route) {
			this.route = route;
		}

		public void actionPerformed(ActionEvent e) {

			String enteredNumberAsString = enteredNumberKeys.toString();
			if (enteredNumberAsString.equals(""))
				return;
			int enteredNumber = Integer.parseInt(enteredNumberAsString);
			Turnout turnout;
			try {
				turnout = turnoutPersistence.getTurnoutByNumber(enteredNumber);
				if (turnout == null) {
					JOptionPane
							.showMessageDialog(
									RouteConfig.this,
									"Turnout " + enteredNumber
											+ " does not exist",
									"Error",
									JOptionPane.ERROR_MESSAGE,
									ImageTools
											.createImageIcon("messagebox_critical.png"));
				} else {
					SRCPTurnoutState routedState = null;
					if (e.getActionCommand().equals("/")) {
						// ThreeWay LEFT
						routedState = SRCPTurnoutState.LEFT;
					} else if (e.getActionCommand().equals("*")) {
						// ThreeWay STRAIGHT
						routedState = SRCPTurnoutState.STRAIGHT;
					} else if (e.getActionCommand().equals("-")) {
						// ThreeWay RIGHT
						routedState = SRCPTurnoutState.RIGHT;
					} else if (e.getActionCommand().equals("+")
							|| e.getActionCommand().equals("bs")) {
						// CURVED
						if (!turnout.isThreeWay()) {
							switch (turnout.getDefaultStateEnum()) {
							case STRAIGHT:
								routedState = SRCPTurnoutState.LEFT;
								break;
							case LEFT:
								routedState = SRCPTurnoutState.STRAIGHT;
								break;
							}
						} else {
							routedState = SRCPTurnoutState.LEFT;
						}
					} else if (e.getActionCommand().equals("\n")) {
						// STRAIGHT
						routedState = turnout.getDefaultStateEnum();
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
						routePersistence.deleteRouteItem(itemToRemove);
					}
					RouteItem i = new RouteItem();
					i.setRoute(route);
					i.setRoutedStateEnum(routedState);
					i.setTurnout(turnout);

					try {
						routePersistence.addRouteItem(i);
						List<RouteItem> routeItems = new ArrayList<RouteItem>(
								route.getRouteItems());
						routeItemModel.setList(routeItems);
					} catch (RoutePersistenceException e1) {
						e1.printStackTrace();
					}
				}
				enteredNumberKeys = new StringBuffer();
				digitDisplay.reset();
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (TurnoutException e1) {
				ExceptionProcessor.getInstance().processExceptionDialog(e1);
				enteredNumberKeys = new StringBuffer();
			}
		}
	}

	private class NumberEnteredAction extends AbstractAction {

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
			super("Remove Turnout", ImageTools.createImageIcon("remove.png"));
		}

		public void actionPerformed(ActionEvent e) {
			Route selectedRoute = (Route) (presentationModel.getBean());
			RouteItem routeItem = routeItemModel.getSelection();
			if (routeItem == null)
				return;

			routePersistence.deleteRouteItem(routeItem);
			List<RouteItem> routeItems = new ArrayList<RouteItem>(selectedRoute
					.getRouteItems());
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

		public void actionPerformed(ActionEvent e) {

			Route route = presentationModel.getBean();
			if (route.getId() == 0) {
				routePersistence.addRoute(route);
			} else {
				routePersistence.updateRoute(route);
			}
			okPressed = true;
			route.removePropertyChangeListener(RouteConfig.this);
			RouteConfig.this.setVisible(false);

		}
	}

	class CancelAction extends AbstractAction {

		public CancelAction() {
			super("Cancel");
		}

		public void actionPerformed(ActionEvent e) {
			Route route = presentationModel.getBean();
			route.removePropertyChangeListener(RouteConfig.this);
			okPressed = false;
			cancelPressed = true;
			RouteConfig.this.setVisible(false);
		}
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

}
