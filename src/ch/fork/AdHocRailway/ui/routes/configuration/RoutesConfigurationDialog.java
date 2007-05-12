package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteControl;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.TableResizer;

public class RoutesConfigurationDialog<E> extends
		ConfigurationDialog<RoutesConfiguration> {
	private RouteControl routeControl;

	private List<Route> routesWorkCopy;

	private Map<Integer, Route> numberToRouteWorkCopy;

	private ListListModel routesListModel;

	private JList routeList;

	private JButton addRouteButton;

	private JButton removeRouteButton;

	private JPopupMenu routesPopupMenu;

	private RoutedSwitchesTableModel routedSwitchesTableModel;

	private JPanel routeDetailPanel;

	private JTable routedSwitchesTable;

	private JTextField routeNumberField;

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
		this.numberToRouteWorkCopy = new HashMap<Integer, Route>();
		for (Route r : routeControl.getRoutes()) {
			Route newRoute = (Route) r.clone();
			routesWorkCopy.add(newRoute);
		}
		for (Route r : routeControl.getNumberToRoutes().values()) {
			numberToRouteWorkCopy.put(r.getNumber(), r);
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

		Route selectedRoute = (Route) routeList.getSelectedValue();

		routeDetailPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory.createTitledBorder("Route");
		routeDetailPanel.setBorder(title);
		routeDetailPanel.getInsets(new Insets(5, 5, 5, 5));

		JLabel routeNumberLabel = new JLabel("Route Number");
		if (selectedRoute != null) {
			routeNumberField = new JTextField(15);
			routeNumberField.setText(""+selectedRoute.getNumber());
		}else
			routeNumberField = new JTextField(15);

		JPanel numberPanel = new JPanel(new FlowLayout());
		numberPanel.add(routeNumberLabel);
		numberPanel.add(routeNumberField);
		routedSwitchesTableModel = new RoutedSwitchesTableModel();
		routedSwitchesTable = new JTable(routedSwitchesTableModel);

		routedSwitchesTable.setRowHeight(24);
		JScrollPane tableScrollPane = new JScrollPane(routedSwitchesTable);

		routeDetailPanel.add(numberPanel, BorderLayout.NORTH);
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
		addSwitchButton.addActionListener(new AddSwitchAction());
		removeSwitchButton.addActionListener(new RemoveSwitchAction());
		JPanel buttonPanel = new JPanel(new FlowLayout());

		buttonPanel.add(addSwitchButton);
		buttonPanel.add(removeSwitchButton);

		routeDetailPanel.add(buttonPanel, BorderLayout.SOUTH);

		return routeDetailPanel;
	}

	private void updateRouteDetailPanel() {
		Route selectedRoute = (Route) (routeList.getSelectedValue());
		if (selectedRoute == null) {
			((TitledBorder) routeDetailPanel.getBorder()).setTitle("Route");
			routeNumberField.setText("");
		} else {
			((TitledBorder) routeDetailPanel.getBorder()).setTitle("Route '"
					+ selectedRoute.getName() + "'");
			routeNumberField.setText(""+selectedRoute.getNumber());
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
			SortedSet<Integer> usedNumbers = new TreeSet<Integer>(
					numberToRouteWorkCopy.keySet());
			int nextNumber = 1;
			if (usedNumbers.size() == 0) {
				nextNumber = 1;
			} else {
				nextNumber = usedNumbers.last().intValue() + 1;
			}
			Route newRoute = new Route(newRouteName, nextNumber);
			routesWorkCopy.add(newRoute);
			routesListModel.updated();
			routeList.setSelectedValue(newRoute, true);
			updateRouteDetailPanel();
		}

	}

	private class RemoveRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			Route routeToDelete = (Route) (routeList.getSelectedValue());
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
				routesWorkCopy.remove(routeToDelete);
				routesListModel.updated();
			}
		}

	}

	private class RenameRouteAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			Route routeToRename = (Route) (routeList.getSelectedValue());
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
			Route routeToMove = (Route) (routeList.getSelectedValue());
			if (routeToMove == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int oldIndex = routesWorkCopy.indexOf(routeToMove);
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

	private class AddSwitchAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			Route selectedRoute = (Route) (routeList.getSelectedValue());
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

	private class RemoveSwitchAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

		}

	}
}