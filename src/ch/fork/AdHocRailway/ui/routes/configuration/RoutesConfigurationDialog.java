package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.routes.HibernateRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
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
	private RoutePersistenceIface routePersistence = HibernateRoutePersistence
			.getInstance();
	private TurnoutPersistenceIface turnoutPersistence = HibernateTurnoutPersistence
			.getInstance();

	private JList routeGroupList;

	private JButton addRouteGroupButton;

	private JButton removeRouteGroupButton;

	private JList routesList;

	private JButton addRouteButton;

	private JButton removeRouteButton;

	private JTable routeItemTable;

	private JSpinner routeNumberField;

	private JTextField routeNameField;
	private SelectionInList<RouteGroup> routeGroupModel;
	private JButton okButton;
	protected boolean okPressed;
	private SelectionInList<Route> routesModel;
	private PresentationModel<Route> routeModel;
	private SelectionInList<RouteItem> routeItemModel;
	private JButton addRouteItemButton;
	private JButton removeRouteItemButton;

	public RoutesConfigurationDialog(JFrame parent) {
		super(parent, "Edit Routes");
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
				"pref, 10dlu, pref, 10dlu, right:pref, 3dlu, pref:grow",
				"pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow");
		PanelBuilder builder = new PanelBuilder(layout);
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Route Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(routeGroupList), cc.xywh(1, 3, 1, 5));
		builder.add(buildRouteGroupButtonBar(), cc.xy(1, 9));

		builder.addSeparator("Routes", cc.xyw(3, 1, 1));
		builder.add(new JScrollPane(routesList), cc.xywh(3, 3, 1, 5));
		builder.add(buildRouteButtonBar(), cc.xy(3, 9));

		builder.addSeparator("Route", cc.xyw(5, 1, 3));

		builder.addLabel("Route Number", cc.xy(5, 3));
		builder.add(routeNumberField, cc.xy(7, 3));

		builder.addLabel("Route Name", cc.xy(5, 5));
		builder.add(routeNameField, cc.xy(7, 5));
		builder.add(new JScrollPane(routeItemTable), cc.xyw(5, 7, 3));
		builder.add(buildRouteItemButtonBar(), cc.xyw(5, 9, 3));

		builder.add(buildMainButtonBar(), cc.xyw(1, 11, 7));

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
				removeRouteItemButton);
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
		removeRouteItemButton = new JButton(new RemoveRouteItemAction());

		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				setVisible(false);
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

		private static final String[] COLUMNS = { "Turnout Number",
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
			super("Add Group");
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
			super("Remove Group");
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
			super("Add Route");
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
			super("Remove Route");
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
			super("Add Turnout");
		}

		public void actionPerformed(ActionEvent e) {
			Route selectedRoute = (Route) (routesList.getSelectedValue());
			if (selectedRoute == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String switchNumberAsString = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the number of the Switch", "Add switch	 to route",
					JOptionPane.QUESTION_MESSAGE);
			Turnout turnout;
			try {
				turnout = turnoutPersistence.getTurnoutByNumber(Integer
						.parseInt(switchNumberAsString));

				RouteItem i = new RouteItem();
				i.setRoute(selectedRoute);
				i.setRoutedStateEnum(TurnoutState.STRAIGHT);
				i.setTurnout(turnout);

				try {
					routePersistence.addRouteItem(i);
					List<RouteItem> routeItems = new ArrayList<RouteItem>(
							selectedRoute.getRouteItems());
					routeItemModel.setList(routeItems);
				} catch (RoutePersistenceException e1) {
					e1.printStackTrace();
				}
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (TurnoutException e1) {
				ExceptionProcessor.getInstance().processExceptionDialog(e1);
			}
		}
	}

	private class RemoveRouteItemAction extends AbstractAction {

		public RemoveRouteItemAction() {
			super("Remove Turnout");
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
