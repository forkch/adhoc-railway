package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
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
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RoutesConfigurationDialog extends JDialog {
	private RoutePersistenceIface		routePersistence	= AdHocRailway
																	.getInstance()
																	.getRoutePersistence();
	private TurnoutPersistenceIface		turnoutPersistence	= AdHocRailway
																	.getInstance()
																	.getTurnoutPersistence();

	private JList						routeGroupList;

	private JButton						addRouteGroupButton;

	private JButton						removeRouteGroupButton;

	private JList						routesList;

	private JButton						addRouteButton;

	private JButton						removeRouteButton;

	private JTable						routeItemTable;

	private JSpinner					routeNumberField;

	private JTextField					routeNameField;
	private SelectionInList<RouteGroup>	routeGroupModel;
	private JButton						okButton;
	protected boolean					okPressed;
	private SelectionInList<Route>		routesModel;
	private PresentationModel<Route>	routeModel;
	private SelectionInList<RouteItem>	routeItemModel;
	private JButton						addRouteItemButton;
	private JButton						removeRouteItemButton;
	private JButton						recordRouteButton;
	public StringBuffer					enteredNumberKeys;
	private PanelBuilder				builder;
	private RouteGroupConfigPanel		routeGroupConfig;
	public ThreeDigitDisplay			digitDisplay;

	public RoutesConfigurationDialog(JFrame parent) {
		super(parent, "Edit Routes", true);
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
	}

	private void buildPanel() {
		initComponents();
		initEventHandling();

		FormLayout layout = new FormLayout(
				"pref, 5dlu, pref, 5dlu, right:pref:grow, 3dlu, pref:grow",
				"pref, 3dlu, " + "pref, 3dlu, " + "pref, 3dlu, "
						+ "pref:grow, 3dlu, " + "pref:grow, 3dlu, "
						+ "pref, 3dlu, " + "pref");
		builder = new PanelBuilder(layout);
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Route Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(routeGroupList), cc.xywh(1, 3, 1, 5));
		builder.add(routeGroupConfig, cc.xy(1, 9));
		builder.add(buildRouteGroupButtonBar(), cc.xy(1, 11));

		builder.addSeparator("Routes", cc.xyw(3, 1, 1));
		builder.add(new JScrollPane(routesList), cc.xywh(3, 3, 1, 7));
		builder.add(buildRouteButtonBar(), cc.xy(3, 11));

		builder.addSeparator("Route", cc.xyw(5, 1, 3));

		builder.addLabel("Route Number", cc.xy(5, 3));
		builder.add(routeNumberField, cc.xy(7, 3));

		builder.addLabel("Route Name", cc.xy(5, 5));
		builder.add(routeNameField, cc.xy(7, 5));
		builder.add(new JScrollPane(routeItemTable), cc.xywh(5, 7, 3, 3));
		builder.add(buildRouteItemButtonBar(), cc.xyw(5, 11, 3));

		builder.add(buildMainButtonBar(), cc.xyw(1, 13, 7));

		add(builder.getPanel());
		// add(new FormDebugPanel(layout));
	}

	private Component buildRouteGroupButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteGroupButton,
				removeRouteGroupButton);
	}

	private Component buildRouteButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteButton,
				removeRouteButton);
	}

	private Component buildRouteItemButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteItemButton,
				recordRouteButton, removeRouteItemButton);
	}

	private Component buildMainButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	private void initComponents() {
		ArrayListModel<RouteGroup> routeGroups = routePersistence
				.getAllRouteGroups();
		routeGroupModel = new SelectionInList<RouteGroup>(
				(ListModel) routeGroups);

		routeGroupList = BasicComponentFactory.createList(routeGroupModel);
		routeGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		routeGroupList.setCellRenderer(new RouteGroupListCellRenderer());

		routeGroupConfig = new RouteGroupConfigPanel();

		addRouteGroupButton = new JButton(new AddRouteGroupAction());
		removeRouteGroupButton = new JButton(new RemoveRouteGroupAction());

		routesModel = new SelectionInList<Route>();
		routesList = new JList();
		routesList.setModel(routesModel);
		routesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		routesList.setCellRenderer(new RouteListCellRenderer());

		addRouteButton = new JButton(new AddRouteAction());
		removeRouteButton = new JButton(new RemoveRouteAction());

		routeModel = new PresentationModel<Route>(routesModel);

		routeNumberField = new JSpinner();
		routeNumberField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				routeModel.getModel(Route.PROPERTYNAME_NUMBER), 1, // defaultValue
				0, // minValue
				1000, // maxValue
				1)); // step

		routeNameField = BasicComponentFactory.createTextField(routeModel
				.getModel(Route.PROPERTYNAME_NAME));
		routeNameField.setColumns(5);

		routeItemModel = new SelectionInList<RouteItem>();
		routeItemTable = new JTable();
		routeItemTable.setModel(new RouteItemTableModel(routeItemModel));
		routeItemTable.setRowHeight(30);
		routeItemTable.setSelectionModel(new SingleListSelectionAdapter(
				routeItemModel.getSelectionIndexHolder()));

		TableColumn routedStateColumn = routeItemTable.getColumnModel()
				.getColumn(1);
		routedStateColumn.setCellRenderer(new RoutedTurnoutStateCellRenderer());

		addRouteItemButton = new JButton(new AddRouteItemAction());
		recordRouteButton = new JButton(new RecordRouteAction());
		removeRouteItemButton = new JButton(new RemoveRouteItemAction());

		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					routePersistence.flush();
				} catch (RoutePersistenceException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				} finally {
					okPressed = true;
					setVisible(false);
				}
			}

		});
	}

	private void initEventHandling() {
		routeGroupList
				.addListSelectionListener(new RouteGroupSelectionHandler());

		routesList.addListSelectionListener(new RouteSelectionHandler());
	}

	/**
	 * Sets the selected RouteGroup as bean in the details model.
	 */
	private final class RouteGroupSelectionHandler implements
			ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			if (routeGroupList.getSelectedIndex() == -1)
				routeGroupList.setSelectedIndex(0);
			RouteGroup selectedGroup = (RouteGroup) routeGroupList
					.getSelectedValue();
			if (selectedGroup == null)
				return;
			routesList.setSelectedIndex(-1);
			routeGroupConfig.setRouteGroup(selectedGroup);
			List<Route> routes = new ArrayList<Route>(selectedGroup.getRoutes());
			routesModel.setList(routes);
		}
	}

	/**
	 * Sets the selected Route as bean in the details model.
	 */
	private final class RouteSelectionHandler implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			if (routesList.getSelectedIndex() == -1)
				routesList.setSelectedIndex(0);
			Route selectedRoute = (Route) routesList.getSelectedValue();
			if (selectedRoute == null)
				return;
			List<RouteItem> routeItems = new ArrayList<RouteItem>(selectedRoute
					.getRouteItems());
			routeModel.setBean(selectedRoute);
			routeItemModel.setList(routeItems);
		}
	}

	/**
	 * Used to renders RouteGroups in JLists and JComboBoxes. If the combo box
	 * selection is null, an empty text <code>""</code> is rendered.
	 */
	private static final class RouteGroupListCellRenderer extends
			DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);

			RouteGroup group = (RouteGroup) value;
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
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);

			Route route = (Route) value;
			setText(route == null ? "" : (" " + route.getName()));
			return component;
		}
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

	private class AddRouteGroupAction extends AbstractAction {

		public AddRouteGroupAction() {
			super("Add Group", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent e) {
			String newRouteGroupName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the name of the new route group", "Add route group",
					JOptionPane.QUESTION_MESSAGE);

			RouteGroup newRouteGroup = new RouteGroup();
			newRouteGroup.setName(newRouteGroupName);
			routePersistence.addRouteGroup(newRouteGroup);

		}

	}

	private class RemoveRouteGroupAction extends AbstractAction {

		public RemoveRouteGroupAction() {
			super("Remove Group", ImageTools.createImageIcon("remove.png"));
		}

		public void actionPerformed(ActionEvent e) {
			RouteGroup routeGroupToDelete = (RouteGroup) (routeGroupList
					.getSelectedValue());
			if (routeGroupToDelete == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a Route-Group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response = JOptionPane.showConfirmDialog(
					RoutesConfigurationDialog.this,
					"Really remove Route-Group '"
							+ routeGroupToDelete.getName() + "' ?",
					"Remove Route-Group", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				try {
					routePersistence.deleteRouteGroup(routeGroupToDelete);
				} catch (RoutePersistenceException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
			}
		}

	}

	public boolean isOkPressed() {
		return okPressed;
	}

	private class AddRouteAction extends AbstractAction {

		public AddRouteAction() {
			super("Add Route", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent e) {
			String newRouteName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the name of the new Route", "Add Route",
					JOptionPane.QUESTION_MESSAGE);

			int nextNumber = routePersistence.getNextFreeRouteNumber();

			Route newRoute = new Route();
			newRoute.setName(newRouteName);
			newRoute.setNumber(nextNumber);
			RouteGroup selectedRouteGroup = (RouteGroup) (routeGroupList
					.getSelectedValue());

			newRoute.setRouteGroup(selectedRouteGroup);
			try {
				routePersistence.addRoute(newRoute);
				List<Route> routes = new ArrayList<Route>(selectedRouteGroup
						.getRoutes());
				routesModel.setList(routes);
			} catch (RoutePersistenceException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}

		}
	}

	private class RemoveRouteAction extends AbstractAction {

		public RemoveRouteAction() {
			super("Remove Route", ImageTools.createImageIcon("remove.png"));
		}

		public void actionPerformed(ActionEvent e) {
			RouteGroup selectedRouteGroup = (RouteGroup) (routeGroupList
					.getSelectedValue());
			Route routeToDelete = (Route) (routesList.getSelectedValue());
			if (routeToDelete == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response = JOptionPane.showConfirmDialog(
					RoutesConfigurationDialog.this, "Really remove Route '"
							+ routeToDelete.getName() + "' ?", "Remove Route",
					JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				try {
					routePersistence.deleteRoute(routeToDelete);
					List<Route> routes = new ArrayList<Route>(
							selectedRouteGroup.getRoutes());
					routesModel.setList(routes);
				} catch (RoutePersistenceException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
			}
		}
	}

	private class AddRouteItemAction extends AbstractAction {

		public AddRouteItemAction() {
			super("Add Turnout", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent e) {
			Route selectedRoute = (Route) (routesList.getSelectedValue());
			if (selectedRoute == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

		}
	}

	private class RecordRouteAction extends AbstractAction {

		private boolean	recording;
		private JWindow	numberDisplayDialog;

		public RecordRouteAction() {
			super("Record",ImageTools.createImageIcon("record_off.png"));
		}

		public void actionPerformed(ActionEvent e) {
			if (!recording) {
				Route selectedRoute = (Route) (routesList.getSelectedValue());
				if (selectedRoute == null) {
					JOptionPane.showMessageDialog(
							RoutesConfigurationDialog.this,
							"Please select a route", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				digitDisplay = new ThreeDigitDisplay();
				numberDisplayDialog = new JWindow(
						RoutesConfigurationDialog.this);
				numberDisplayDialog.add(digitDisplay);
				numberDisplayDialog.pack();
				numberDisplayDialog.setAlwaysOnTop(true);
				
				TutorialUtils.locateOnOpticalScreenLeft3rd(numberDisplayDialog);
				recordRouteButton.setIcon(ImageTools.createImageIcon("record.png"));
				initKeyboardActions(selectedRoute);
				numberDisplayDialog.setVisible(true);
				recording = true;
			} else {
				recordRouteButton.setIcon(ImageTools.createImageIcon("record_off.png"));
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
							.toString(i)), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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
			if(enteredNumberAsString.equals(""))
				return;
			int enteredNumber = Integer.parseInt(enteredNumberAsString);
			Turnout turnout;
			try {
				turnout = turnoutPersistence.getTurnoutByNumber(enteredNumber);
				TurnoutState routedState = null;
				if (e.getActionCommand().equals("/")) {
					// ThreeWay LEFT
					routedState = TurnoutState.LEFT;
				} else if (e.getActionCommand().equals("*")) {
					// ThreeWay STRAIGHT
					routedState = TurnoutState.STRAIGHT;
				} else if (e.getActionCommand().equals("-")) {
					// ThreeWay RIGHT
					routedState = TurnoutState.RIGHT;
				} else if (e.getActionCommand().equals("+")
						|| e.getActionCommand().equals("bs")) {
					// CURVED
					if (!turnout.isThreeWay()) {
						switch (turnout.getDefaultStateEnum()) {
						case STRAIGHT:
							routedState = TurnoutState.LEFT;
							break;
						case LEFT:
							routedState = TurnoutState.STRAIGHT;
							break;
						}
					}
				} else if (e.getActionCommand().equals("\n")) {
					// STRIAHT
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
					List<RouteItem> routeItems = new ArrayList<RouteItem>(route
							.getRouteItems());
					routeItemModel.setList(routeItems);
				} catch (RoutePersistenceException e1) {
					e1.printStackTrace();
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
			Route selectedRoute = (Route) (routesList.getSelectedValue());
			RouteItem routeItem = routeItemModel.getSelection();
			if (routeItem == null)
				return;

			routePersistence.deleteRouteItem(routeItem);
			List<RouteItem> routeItems = new ArrayList<RouteItem>(selectedRoute
					.getRouteItems());
			routeItemModel.setList(routeItems);
		}
	}
}
