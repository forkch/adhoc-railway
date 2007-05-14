package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.SpringUtilities;
import ch.fork.AdHocRailway.ui.TableResizer;

public class RoutesConfigurationDialog<E> extends
		ConfigurationDialog<RoutesConfiguration> {
	private RouteControl routeControl;

	private Map<Integer, Route> numberToRouteWorkCopy;

	private List<RouteGroup> routeGroupsWorkCopy;

	private ListListModel routesListModel;

	private JList routesInGroupJList;

	private JButton addRouteButton;

	private JButton removeRouteButton;

	private JPopupMenu routesPopupMenu;

	private RoutedSwitchesTableModel routedSwitchesTableModel;

	private JPanel routeDetailPanel;

	private JTable routedSwitchesTable;

	private JTextField routeNumberField;

	private JPanel routeGroupsPanel;

	private ListListModel<RouteGroup> routeGroupListModel;

	private JList routeGroupJList;

	private JPopupMenu routeGroupPopupMenu;

	private JButton addRouteGroupButton;

	private JButton removeRouteGroupButton;

	private JPanel routesInGroupPanel;

	private JTextField routeNameField;

	public RoutesConfigurationDialog(JFrame parent) {
		super(parent, "Edit Routes");
		initGUI();
	}

	private void initGUI() {
		createRouteGroupsPanel();
		createRoutesInGroupPanel();
		createRouteDetailPanel();
		JPanel routeSelectionPanel = new JPanel(new GridLayout(1, 2));
		routeSelectionPanel.add(routeGroupsPanel);
		routeSelectionPanel.add(routesInGroupPanel);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(routeSelectionPanel, BorderLayout.WEST);
		mainPanel.add(routeDetailPanel, BorderLayout.CENTER);

		addMainComponent(mainPanel);
		pack();
		setVisible(true);
	}

	@Override
	public void createTempConfiguration() {
		this.routeControl = RouteControl.getInstance();
		this.numberToRouteWorkCopy = new HashMap<Integer, Route>();
		this.routeGroupsWorkCopy = new ArrayList<RouteGroup>();
		for (Route r : routeControl.getNumberToRoutes().values()) {
			numberToRouteWorkCopy.put(r.getNumber(), (Route) r.clone());
		}
		for (RouteGroup rg : routeControl.getRouteGroups()) {
			RouteGroup clone = rg.clone();
			routeGroupsWorkCopy.add(clone);
			for (Route r : rg.getRoutes()) {
				clone.addRoute(this.numberToRouteWorkCopy.get(r.getNumber()));
			}
		}
	}

	@Override
	public RoutesConfiguration getTempConfiguration() {
		return new RoutesConfiguration(numberToRouteWorkCopy,
				routeGroupsWorkCopy);
	}

	private void createRouteGroupsPanel() {
		routeGroupsPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory.createTitledBorder("Route Groups");
		routeGroupsPanel.setBorder(title);
		routeGroupsPanel.getInsets(new Insets(5, 5, 5, 5));
		routeGroupListModel = new ListListModel<RouteGroup>(routeGroupsWorkCopy);
		routeGroupJList = new JList(routeGroupListModel);
		routeGroupJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		routeGroupPopupMenu = new JPopupMenu();
		JMenuItem addItem = new JMenuItem("Add");
		JMenuItem removeItem = new JMenuItem("Remove");
		JMenuItem renameItem = new JMenuItem("Rename");
		JMenuItem moveUpItem = new JMenuItem("Move up");
		JMenuItem moveDownItem = new JMenuItem("Move down");
		routeGroupPopupMenu.add(addItem);
		routeGroupPopupMenu.add(removeItem);
		routeGroupPopupMenu.add(renameItem);
		routeGroupPopupMenu.add(new JSeparator());
		routeGroupPopupMenu.add(moveUpItem);
		routeGroupPopupMenu.add(moveDownItem);
		addRouteGroupButton = new JButton("Add");
		removeRouteGroupButton = new JButton("Remove");
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(addRouteGroupButton);
		buttonPanel.add(removeRouteGroupButton);
		routeGroupsPanel.add(routeGroupJList, BorderLayout.CENTER);
		routeGroupsPanel.add(buttonPanel, BorderLayout.SOUTH);
		/* Install ActionListeners */
		routeGroupJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateRoutesInGroupPanel();
			}

		});
		routeGroupJList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					routeGroupPopupMenu.show(e.getComponent(), e.getX(), e
							.getY());
				}
			}
		});
		addItem.addActionListener(new AddRouteGroupAction());
		addRouteGroupButton.addActionListener(new AddRouteGroupAction());
		removeItem.addActionListener(new RemoveRouteGroupAction());
		removeRouteGroupButton.addActionListener(new RemoveRouteGroupAction());
		renameItem.addActionListener(new RenameRouteGroupAction());
		moveUpItem.addActionListener(new MoveRouteGroupAction(true));
		moveDownItem.addActionListener(new MoveRouteGroupAction(false));

	}

	private void createRoutesInGroupPanel() {
		routesInGroupPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory
				.createTitledBorder("Routes in Group xyz");
		routesInGroupPanel.setBorder(title);
		routesInGroupPanel.getInsets(new Insets(5, 5, 5, 5));

		routesListModel = new ListListModel<Route>();
		routesInGroupJList = new JList(routesListModel);
		routesInGroupJList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

		routesInGroupPanel.add(routesInGroupJList, BorderLayout.CENTER);
		routesInGroupPanel.add(buttonPanel, BorderLayout.SOUTH);

		/* Install ActionListeners */
		routesInGroupJList
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						updateRouteDetailPanel();
					}

				});
		routesInGroupJList.addMouseListener(new MouseAdapter() {
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

	}

	private void createRouteDetailPanel() {

		Route selectedRoute = (Route) routesInGroupJList.getSelectedValue();

		routeDetailPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory.createTitledBorder("Route");
		routeDetailPanel.setBorder(title);
		routeDetailPanel.getInsets(new Insets(5, 5, 5, 5));

		JLabel routeNameLabel = new JLabel("Route name");
		JLabel routeNumberLabel = new JLabel("Route number");
		
		routeNameField = new JTextField(15);
		routeNameField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
				
			}

			public void focusLost(FocusEvent arg0) {
				Route selectedRoute = (Route) routesInGroupJList.getSelectedValue();
				selectedRoute.setName(routeNameField.getText());
				updateRoutesInGroupPanel();
			}
			
		});
		routeNumberField = new JTextField(15);
		routeNumberField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
				
			}

			public void focusLost(FocusEvent arg0) {
				Route selectedRoute = (Route) routesInGroupJList.getSelectedValue();
				selectedRoute.setNumber(Integer.parseInt(routeNumberField.getText()));
				updateRoutesInGroupPanel();
			}
			
		});
		if(selectedRoute != null) {
			routeNumberField.setText("" + selectedRoute.getNumber());
			routeNameField.setText(selectedRoute.getName());
		}
		

		JPanel routeSettingsPanel = new JPanel(new SpringLayout());

		routeSettingsPanel.add(routeNameLabel);
		routeSettingsPanel.add(routeNameField);
		routeSettingsPanel.add(routeNumberLabel);
		routeSettingsPanel.add(routeNumberField);
		
		SpringUtilities.makeCompactGrid(routeSettingsPanel, 2, 2, // rows, cols
	            6, 6, // initX, initY
	            6, 6); // xPad, yPad
		
		routedSwitchesTableModel = new RoutedSwitchesTableModel();
		routedSwitchesTable = new JTable(routedSwitchesTableModel);

		routedSwitchesTable.setRowHeight(24);
		JScrollPane tableScrollPane = new JScrollPane(routedSwitchesTable);

		routeDetailPanel.add(routeSettingsPanel, BorderLayout.NORTH);
		routeDetailPanel.add(tableScrollPane, BorderLayout.CENTER);

		TableColumn stateColumn = routedSwitchesTable.getColumnModel()
				.getColumn(1);

		JComboBox switchStateRoutedComboBox = new JComboBox();
		switchStateRoutedComboBox.addItem(SwitchState.STRAIGHT);
		switchStateRoutedComboBox.addItem(SwitchState.LEFT);
		switchStateRoutedComboBox.addItem(SwitchState.RIGHT);
		switchStateRoutedComboBox
				.setRenderer(new SwitchRoutedStateComboBoxCellRenderer());
		stateColumn.setCellEditor(new DefaultCellEditor(
				switchStateRoutedComboBox));
		stateColumn.setCellRenderer(new SwitchRoutedStateCellRenderer());

		JButton addSwitchButton = new JButton("Add switch to route...");
		JButton removeSwitchButton = new JButton("Remove switch from route");
		addSwitchButton.addActionListener(new AddSwitchToRouteAction());
		removeSwitchButton.addActionListener(new RemoveSwitchFromRouteAction());
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buttonPanel.add(addSwitchButton);
		buttonPanel.add(removeSwitchButton);

		routeDetailPanel.add(buttonPanel, BorderLayout.SOUTH);

	}

	private void updateRoutesInGroupPanel() {
		RouteGroup selectedRouteGroup = (RouteGroup) (routeGroupJList
				.getSelectedValue());
		if (selectedRouteGroup == null) {
			routesInGroupJList.setModel(null);
			routesInGroupPanel.setBorder(new TitledBorder("Route Group"));
		} else {
			routesInGroupPanel.setBorder(new TitledBorder("Route Group '"
					+ selectedRouteGroup.getName() + "'"));
			routesListModel.setList(new ArrayList<Route>(selectedRouteGroup.getRoutes()));
			routesInGroupJList.setSelectedIndex(0);
		}
		pack();
	}

	private void updateRouteDetailPanel() {
		Route selectedRoute = (Route) (routesInGroupJList.getSelectedValue());
		if (selectedRoute == null) {
			((TitledBorder) routeDetailPanel.getBorder()).setTitle("Route");
			routeNumberField.setText("");
		} else {
			((TitledBorder) routeDetailPanel.getBorder()).setTitle("Route '"
					+ selectedRoute.getName() + "'");
			routeNumberField.setText("" + selectedRoute.getNumber());
			routeNameField.setText(selectedRoute.getName());
		}
		((RoutedSwitchesTableModel) routedSwitchesTableModel)
				.setRoute(selectedRoute);
		TableResizer.adjustColumnWidths(routedSwitchesTable, 30);
		if (routedSwitchesTable.getRowCount() > 0) {
			TableResizer.adjustRowHeight(routedSwitchesTable);
		}
		pack();
	}

	private class AddRouteGroupAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			String newRouteGroupName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the name of the new route group", "Add route group",
					JOptionPane.QUESTION_MESSAGE);
			RouteGroup newRouteGroup = new RouteGroup(newRouteGroupName);
			routeGroupsWorkCopy.add(newRouteGroup);
			routeGroupListModel.updated();
			routeGroupJList.setSelectedValue(newRouteGroup, true);
			updateRoutesInGroupPanel();
		}

	}

	private class RemoveRouteGroupAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			RouteGroup routeGroupToDelete = (RouteGroup) (routeGroupJList
					.getSelectedValue());
			if (routeGroupToDelete == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response = JOptionPane.showConfirmDialog(
					RoutesConfigurationDialog.this, "Really remove route group '"
							+ routeGroupToDelete.getName() + "' ?", "Remove route group",
					JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				routeGroupsWorkCopy.remove(routeGroupToDelete);
				routeGroupListModel.updated();
			}
		}

	}

	private class RenameRouteGroupAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			RouteGroup routeGroupToRename = (RouteGroup) (routeGroupJList
					.getSelectedValue());
			if (routeGroupToRename == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String newSectionName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this, "Enter new name",
					"Rename route group", JOptionPane.QUESTION_MESSAGE);
			if (!newSectionName.equals("")) {
				routeGroupToRename.setName(newSectionName);
				routeGroupListModel.updated();
			}
			updateRoutesInGroupPanel();
		}

	}

	private class AddRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			String newRouteName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the name of the new Route", "Add Route",
					JOptionPane.QUESTION_MESSAGE);
			SortedSet<Integer> usedNumbers = new TreeSet<Integer>(
					numberToRouteWorkCopy.keySet());
			int nextNumber = 1;
			if (usedNumbers.size() == 0) {
				nextNumber = 1;
			} else {
				nextNumber = usedNumbers.last().intValue() + 1;
			}
			Route newRoute = new Route(newRouteName, nextNumber);
			numberToRouteWorkCopy.put(newRoute.getNumber(), newRoute);
			RouteGroup selectedRouteGroup = (RouteGroup) (routeGroupJList
					.getSelectedValue());
			selectedRouteGroup.addRoute(newRoute);
			routesInGroupJList.setSelectedValue(newRoute, true);
			updateRouteDetailPanel();
			updateRoutesInGroupPanel();
		}

	}

	private class RemoveRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			Route routeToDelete = (Route) (routesInGroupJList
					.getSelectedValue());
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
				numberToRouteWorkCopy.remove(routeToDelete.getNumber());
				RouteGroup selectedRouteGroup = (RouteGroup) (routeGroupJList
						.getSelectedValue());
				selectedRouteGroup.removeRoute(routeToDelete);
			}
			updateRoutesInGroupPanel();
		}

	}

	private class RenameRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			Route routeToRename = (Route) (routesInGroupJList
					.getSelectedValue());
			if (routeToRename == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String newSectionName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this, "Enter new name",
					"Rename Switch-Group", JOptionPane.QUESTION_MESSAGE);
			if (!newSectionName.equals("")) {
				routeToRename.setName(newSectionName);
			}
			updateRoutesInGroupPanel();
			updateRouteDetailPanel();
		}

	}

	private class AddSwitchToRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			Route selectedRoute = (Route) (routesInGroupJList
					.getSelectedValue());
			if (selectedRoute == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String switchNumberAsString = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the number of the Switch", "Add switch to route",
					JOptionPane.QUESTION_MESSAGE);
			int switchNumber = Integer.parseInt(switchNumberAsString);
			selectedRoute.addRouteItem(new RouteItem(switchNumber,
					SwitchState.STRAIGHT));
			updateRouteDetailPanel();
		}

	}

	private class RemoveSwitchFromRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

		}

	}

	private class MoveRouteGroupAction extends AbstractAction {
		private boolean up;

		public MoveRouteGroupAction(boolean up) {
			this.up = up;
		}

		public void actionPerformed(ActionEvent e) {
			Route routeToMove = (Route) (routesInGroupJList.getSelectedValue());
			if (routeToMove == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			/*
			 * int oldIndex = routesWorkCopy.indexOf(routeToMove); int newIndex =
			 * oldIndex; if (up) { if (oldIndex != 0) { newIndex = oldIndex - 1; }
			 * else { return; } } else { if (oldIndex != routesWorkCopy.size() -
			 * 1) { newIndex = oldIndex + 1; } else { return; } }
			 * routesWorkCopy.remove(oldIndex); routesWorkCopy.add(newIndex,
			 * routeToMove); routeList.setSelectedIndex(newIndex);
			 * routesListModel.updated(); updateRouteDetailPanel();
			 */
		}

	}

}