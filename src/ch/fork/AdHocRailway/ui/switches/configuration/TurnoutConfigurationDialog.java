/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchConfigurationDialog.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:21 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
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

package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.TableResizer;

public class TurnoutConfigurationDialog<E> extends ConfigurationDialog {

	private JPopupMenu turnoutGroupPopupMenu;

	private JList turnoutGroupList;

	private ListListModel turnoutGroupListModel;

	private JPanel turnoutsPanel;

	private TableModel turnoutsTableModel;

	private JTable turnoutsTable;

	private TurnoutPersistenceIface turnoutPersistence = HibernateTurnoutPersistence
			.getInstance();

	private JPanel turnoutGroupPanel;

	private JButton addTurnoutGroupButton;

	private JButton removeTurnoutGroupButton;

	private JButton addTurnoutButton;

	private JButton add10TurnoutsButton;

	private JButton removeTurnoutButton;

	private JButton editTurnoutButton;

	public TurnoutConfigurationDialog(JFrame owner) {
		super(owner, "Turnout Configuration");

		initGUI();
	}

	private void initGUI() {
		JPanel switchGroupPanel = createSwitchGroupPanel();
		JPanel switchesPanel = createSwitchesPanel();

		// updateSwitchesPanel();

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(switchGroupPanel, BorderLayout.WEST);
		mainPanel.add(switchesPanel, BorderLayout.CENTER);
		addMainComponent(mainPanel);
		SwitchConfigurationFocusTraversalPolicy newPolicy = new SwitchConfigurationFocusTraversalPolicy();
		setFocusTraversalPolicy(newPolicy);
		setSize(new Dimension(750, 550));
		setVisible(true);
	}

	private JPanel createSwitchGroupPanel() {
		turnoutGroupPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory.createTitledBorder("Turnout Groups");
		turnoutGroupPanel.setBorder(title);
		turnoutGroupPanel.getInsets(new Insets(5, 5, 5, 5));
		turnoutGroupListModel = new ListListModel<TurnoutGroup>(
				new ArrayList<TurnoutGroup>(turnoutPersistence
						.getAllTurnoutGroups()));
		turnoutGroupList = new JList(turnoutGroupListModel);
		turnoutGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		turnoutGroupPopupMenu = new JPopupMenu();
		JMenuItem addItem = new JMenuItem("Add");
		JMenuItem removeItem = new JMenuItem("Remove");
		JMenuItem renameItem = new JMenuItem("Rename");
		JMenuItem moveUpItem = new JMenuItem("Move up");
		JMenuItem moveDownItem = new JMenuItem("Move down");
		turnoutGroupPopupMenu.add(addItem);
		turnoutGroupPopupMenu.add(removeItem);
		turnoutGroupPopupMenu.add(renameItem);
		turnoutGroupPopupMenu.add(new JSeparator());
		turnoutGroupPopupMenu.add(moveUpItem);
		turnoutGroupPopupMenu.add(moveDownItem);
		addTurnoutGroupButton = new JButton("Add");
		removeTurnoutGroupButton = new JButton("Remove");
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(addTurnoutGroupButton);
		buttonPanel.add(removeTurnoutGroupButton);
		turnoutGroupPanel.add(turnoutGroupList, BorderLayout.CENTER);
		turnoutGroupPanel.add(buttonPanel, BorderLayout.SOUTH);
		/* Install ActionListeners */

		turnoutGroupList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateTurnoutsPanel();
			}
		});
		turnoutGroupList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					turnoutGroupPopupMenu.show(e.getComponent(), e.getX(), e
							.getY());
				}
			}
		});
		addItem.addActionListener(new AddTurnoutGroupAction());
		addTurnoutGroupButton.addActionListener(new AddTurnoutGroupAction());
		removeItem.addActionListener(new RemoveTurnoutGroupAction());
		removeTurnoutGroupButton
				.addActionListener(new RemoveTurnoutGroupAction());
		renameItem.addActionListener(new RenameTurnoutGroupAction());
		moveUpItem.addActionListener(new MoveTurnoutGroupAction(true));
		moveDownItem.addActionListener(new MoveTurnoutGroupAction(false));

		return turnoutGroupPanel;
	}

	private JPanel createSwitchesPanel() {
		turnoutsPanel = new JPanel(new BorderLayout());

		turnoutsPanel.getInsets(new Insets(5, 5, 5, 5));
		TitledBorder title = BorderFactory.createTitledBorder("Turnout-Group");
		turnoutsPanel.setBorder(title);
		turnoutsTableModel = new TurnoutTableModel();
		turnoutsTable = new JTable(turnoutsTableModel);
		turnoutsTable.setRowHeight(24);

		// SwitchType
		JComboBox switchTypeComboBox = new JComboBox();
		switchTypeComboBox.addItem("DefaultSwitch");
		switchTypeComboBox.addItem("DoubleCrossSwitch");
		switchTypeComboBox.addItem("ThreeWaySwitch");
		switchTypeComboBox.setRenderer(new TurnoutTypeComboBoxCellRenderer());
		TableColumn typeColumn = turnoutsTable.getColumnModel().getColumn(1);
		typeColumn.setCellEditor(new DefaultCellEditor(switchTypeComboBox));
		typeColumn.setCellRenderer(new TurnoutTypeCellRenderer());

		// DefaultState
		JComboBox switchDefaultStateComboBox = new JComboBox();
		switchDefaultStateComboBox.addItem(TurnoutState.STRAIGHT);
		switchDefaultStateComboBox.addItem(TurnoutState.LEFT);
		switchDefaultStateComboBox
				.setRenderer(new TurnoutDefaultStateComboBoxCellRenderer());
		TableColumn defaultStateColumn = turnoutsTable.getColumnModel()
				.getColumn(7);
		defaultStateColumn.setCellEditor(new DefaultCellEditor(
				switchDefaultStateComboBox));
		defaultStateColumn
				.setCellRenderer(new TurnoutDefaultStateCellRenderer());

		// SwitchOrientation
		JComboBox switchOrientationComboBox = new JComboBox();
		switchOrientationComboBox.addItem(TurnoutOrientation.NORTH);
		switchOrientationComboBox.addItem(TurnoutOrientation.EAST);
		switchOrientationComboBox.addItem(TurnoutOrientation.SOUTH);
		switchOrientationComboBox.addItem(TurnoutOrientation.WEST);

		TableColumn switchOrientationColumn = turnoutsTable.getColumnModel()
				.getColumn(8);
		switchOrientationColumn.setCellEditor(new DefaultCellEditor(
				switchOrientationComboBox));

		JScrollPane switchGroupTablePane = new JScrollPane(turnoutsTable);
		switchGroupTablePane.setPreferredSize(new Dimension(750, 400));

		turnoutsPanel.add(switchGroupTablePane, BorderLayout.CENTER);
		turnoutsTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditTurnoutAction().actionPerformed(null);
				}
			}

		});
		addTurnoutButton = new JButton("Add");
		editTurnoutButton = new JButton("Edit");
		add10TurnoutsButton = new JButton("Add 10 Switches");
		removeTurnoutButton = new JButton("Remove");

		addTurnoutButton.addActionListener(new AddTurnoutAction());
		editTurnoutButton.addActionListener(new EditTurnoutAction());
		add10TurnoutsButton.addActionListener(new Add10TurnoutsAction());
		removeTurnoutButton.addActionListener(new RemoveTurnoutAction());

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(addTurnoutButton);
		buttonPanel.add(add10TurnoutsButton);
		buttonPanel.add(editTurnoutButton);
		buttonPanel.add(removeTurnoutButton);

		turnoutsPanel.add(buttonPanel, BorderLayout.SOUTH);
		return turnoutsPanel;
	}

	private void updateTurnoutsPanel() {
		TurnoutGroup selectedTurnoutGroup = (TurnoutGroup) (turnoutGroupList
				.getSelectedValue());
		if (selectedTurnoutGroup == null) {
			turnoutsPanel.setBorder(BorderFactory
					.createTitledBorder("Switch-Group"));
		} else {
			turnoutsPanel.setBorder(BorderFactory
					.createTitledBorder("Switch-Group '"
							+ selectedTurnoutGroup.getName() + "'"));
		}
		((TurnoutTableModel) turnoutsTableModel)
				.setTurnoutGroup(selectedTurnoutGroup);
		TableResizer.adjustColumnWidths(turnoutsTable, 30);
		if (turnoutsTable.getRowCount() > 0) {
			TableResizer.adjustRowHeight(turnoutsTable);
		}
	}

	private class AddTurnoutGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent arg0) {
			String newGroupName = JOptionPane.showInputDialog(
					TurnoutConfigurationDialog.this,
					"Enter the name of the new Turnout-Group",
					"Add Turnout-Group", JOptionPane.QUESTION_MESSAGE);
			TurnoutGroup newTurnoutGroup = new TurnoutGroup();
			newTurnoutGroup.setName(newGroupName);
			turnoutPersistence.addTurnoutGroup(newTurnoutGroup);

			turnoutGroupListModel = new ListListModel<TurnoutGroup>(
					new ArrayList<TurnoutGroup>(turnoutPersistence
							.getAllTurnoutGroups()));
			turnoutGroupList.setModel(turnoutGroupListModel);
			//turnoutGroupList.setSelectedValue(newTurnoutGroup, true);
		}
	}

	private class RemoveTurnoutGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent arg0) {
			TurnoutGroup groupToDelete = (TurnoutGroup) (turnoutGroupList
					.getSelectedValue());
			if (groupToDelete == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a Turnout-Group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response = JOptionPane.showConfirmDialog(
					TurnoutConfigurationDialog.this,
					"Really remove Turnout-Group '" + groupToDelete.getName()
							+ "' ?", "Remove Turnout-Group",
					JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				turnoutPersistence.deleteTurnoutGroup(groupToDelete);
				turnoutGroupListModel = new ListListModel<TurnoutGroup>(
						new ArrayList<TurnoutGroup>(turnoutPersistence
								.getAllTurnoutGroups()));
				turnoutGroupList.setModel(turnoutGroupListModel);
			}
		}
	}

	private class RenameTurnoutGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			TurnoutGroup groupToRename = (TurnoutGroup) (turnoutGroupList
					.getSelectedValue());
			if (groupToRename == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a Turnout-Group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String newGroupName = JOptionPane.showInputDialog(
					TurnoutConfigurationDialog.this, "Enter new name",
					"Rename Turnout-Group", JOptionPane.QUESTION_MESSAGE);
			if (!newGroupName.equals("")) {
				groupToRename.setName(newGroupName);
				turnoutPersistence.updateTurnoutGroup(groupToRename);
				turnoutGroupListModel = new ListListModel<TurnoutGroup>(
						new ArrayList<TurnoutGroup>(turnoutPersistence
								.getAllTurnoutGroups()));
				turnoutGroupList.setModel(turnoutGroupListModel);
			}
		}
	}

	private class MoveTurnoutGroupAction extends AbstractAction {
		private boolean up;

		public MoveTurnoutGroupAction(boolean up) {
			this.up = up;
		}

		public void actionPerformed(ActionEvent e) {
			TurnoutGroup groupToMove = (TurnoutGroup) (turnoutGroupList
					.getSelectedValue());
			if (groupToMove == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a Turnout-Group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			SortedSet<TurnoutGroup> allGroups = turnoutPersistence
					.getAllTurnoutGroups();
			int weight = 0;
			for (TurnoutGroup tg : allGroups) {
				tg.setWeight(weight);
				weight++;
			}
			boolean changed = false;
			if (up) {
				for (TurnoutGroup tg : allGroups) {
					if (tg.getWeight() >= groupToMove.getWeight() - 1) {
						if (!changed) {
							groupToMove.setWeight(tg.getWeight());
							changed = true;
						}
						tg.setWeight(tg.getWeight() + 1);
						turnoutPersistence.updateTurnoutGroup(tg);
					}
				}
			} else {
				for (TurnoutGroup tg : allGroups) {
					if (tg.getWeight() > groupToMove.getWeight()) {
						if (!changed) {
							groupToMove.setWeight(tg.getWeight());
							changed = true;
						}
						tg.setWeight(tg.getWeight() - 1);
						turnoutPersistence.updateTurnoutGroup(tg);
					}
				}
			}

			turnoutPersistence.updateTurnoutGroup(groupToMove);
			turnoutGroupListModel = new ListListModel<TurnoutGroup>(
					new ArrayList<TurnoutGroup>(turnoutPersistence
							.getAllTurnoutGroups()));
			turnoutGroupList.setModel(turnoutGroupListModel);

			turnoutGroupList.setSelectedIndex(groupToMove.getWeight());
			turnoutGroupListModel.updated();
			updateTurnoutsPanel();
		}
	}

	private class AddTurnoutAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			TurnoutGroup selectedTurnoutGroup = (TurnoutGroup) (turnoutGroupList
					.getSelectedValue());
			if (selectedTurnoutGroup == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a switch group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Turnout newTurnout = new Turnout();
			newTurnout.setNumber(turnoutPersistence.getNextFreeTurnoutNumber());
			newTurnout.setTurnoutGroup(selectedTurnoutGroup);
			newTurnout.setDefaultStateEnum(TurnoutState.STRAIGHT);
			newTurnout.setOrientationEnum(TurnoutOrientation.EAST);
			newTurnout.setTurnoutType(turnoutPersistence
					.getTurnoutType(TurnoutTypes.DEFAULT));
			TurnoutConfig switchConfig = new TurnoutConfig(
					TurnoutConfigurationDialog.this, newTurnout);
			if (switchConfig.isOkPressed()) {
				turnoutPersistence.addTurnout(newTurnout);
			}
			updateTurnoutsPanel();
		}
	}
	private class EditTurnoutAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (turnoutsTable.isEditing())
				turnoutsTable.getCellEditor().stopCellEditing();

			Turnout turnoutToEdit = ((TurnoutTableModel) turnoutsTableModel)
					.getTurnoutAt(turnoutsTable.getSelectedRow());
			TurnoutConfig locomotiveConfig = new TurnoutConfig(
					TurnoutConfigurationDialog.this, turnoutToEdit);
			if (locomotiveConfig.isOkPressed()) {
				turnoutPersistence.updateTurnout(turnoutToEdit);
			}
			updateTurnoutsPanel();
		}
	}
	private class Add10TurnoutsAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			TurnoutGroup selectedTurnoutGroup = (TurnoutGroup) (turnoutGroupList
					.getSelectedValue());
			if (selectedTurnoutGroup == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a switch group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int nextNumber = turnoutPersistence.getNextFreeTurnoutNumber();
			for (int i = 0; i < 10; i++) {
				Turnout newTurnout = new Turnout();
				newTurnout.setNumber(turnoutPersistence
						.getNextFreeTurnoutNumber());
				newTurnout.setTurnoutGroup(selectedTurnoutGroup);
				newTurnout.setDefaultStateEnum(TurnoutState.STRAIGHT);
				newTurnout.setOrientationEnum(TurnoutOrientation.EAST);
				newTurnout.setTurnoutType(turnoutPersistence
						.getTurnoutType(TurnoutTypes.DEFAULT));
				turnoutPersistence.addTurnout(newTurnout);
				nextNumber++;
			}
			updateTurnoutsPanel();
		}
	}

	private class RemoveTurnoutAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (turnoutsTable.isEditing())
				turnoutsTable.getCellEditor().stopCellEditing();

			Integer number = (Integer) turnoutsTable.getValueAt(turnoutsTable
					.getSelectedRow(), 0);
			turnoutPersistence.deleteTurnout(turnoutPersistence
					.getTurnoutByNumber(number));
		}
	}

	private class SwitchConfigurationFocusTraversalPolicy extends
			FocusTraversalPolicy {

		public Component getComponentAfter(Container focusCycleRoot,
				Component aComponent) {
			if (aComponent.equals(okButton)) {
				return cancelButton;
			} else if (aComponent.equals(cancelButton)) {
				return addTurnoutGroupButton;
			} else if (aComponent.equals(addTurnoutGroupButton)) {
				return removeTurnoutGroupButton;
			} else if (aComponent.equals(removeTurnoutGroupButton)) {
				return addTurnoutButton;
			} else if (aComponent.equals(addTurnoutButton)) {
				return add10TurnoutsButton;
			} else if (aComponent.equals(add10TurnoutsButton)) {
				return removeTurnoutButton;
			}
			return okButton;
		}

		public Component getComponentBefore(Container focusCycleRoot,
				Component aComponent) {

			if (aComponent.equals(okButton)) {
				return removeTurnoutButton;
			} else if (aComponent.equals(removeTurnoutButton)) {
				return add10TurnoutsButton;
			} else if (aComponent.equals(add10TurnoutsButton)) {
				return addTurnoutButton;
			} else if (aComponent.equals(addTurnoutButton)) {
				return removeTurnoutGroupButton;
			} else if (aComponent.equals(removeTurnoutGroupButton)) {
				return addTurnoutGroupButton;
			} else if (aComponent.equals(addTurnoutGroupButton)) {
				return cancelButton;
			}
			return okButton;
		}

		public Component getDefaultComponent(Container focusCycleRoot) {
			return okButton;
		}

		public Component getLastComponent(Container focusCycleRoot) {
			return cancelButton;
		}

		public Component getFirstComponent(Container focusCycleRoot) {
			return okButton;
		}
	}

}
