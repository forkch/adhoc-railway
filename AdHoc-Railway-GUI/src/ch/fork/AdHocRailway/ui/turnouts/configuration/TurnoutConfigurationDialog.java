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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerListener;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.TableResizer;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TurnoutConfigurationDialog extends JDialog implements
		TurnoutManagerListener {

	private boolean okPressed;

	private JList turnoutGroupList;

	private JTable turnoutsTable;

	private JButton addGroupButton;

	private JButton removeGroupButton;
	private JButton editGroupButton;

	private SelectionInList<TurnoutGroup> turnoutGroupModel;

	private JButton addTurnoutButton;

	private JButton removeTurnoutButton;

	private JButton okButton;

	private TurnoutGroupConfigPanel turnoutGroupConfig;

	private ArrayListModel<TurnoutGroup> turnoutGroups;

	private ArrayListModel<Turnout> turnouts;
	private final TurnoutManager turnoutPersistence = AdHocRailway
			.getInstance().getTurnoutPersistence();

	public TurnoutConfigurationDialog(JFrame owner) {
		super(owner, "Turnout Configuration", true);
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		turnoutPersistence.addTurnoutManagerListener(this);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	private void buildPanel() {
		initComponents();
		initEventHandling();

		FormLayout layout = new FormLayout("pref, 10dlu, pref:grow",
				"pref, 3dlu, fill:pref:grow, 3dlu, pref, 3dlu, pref, 3dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Turnout Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(turnoutGroupList), cc.xy(1, 3));
		builder.add(turnoutGroupConfig, cc.xy(1, 5));
		builder.add(buildGroupButtonBar(), cc.xy(1, 7));

		builder.addSeparator("Turnouts", cc.xyw(3, 1, 1));
		builder.add(new JScrollPane(turnoutsTable), cc.xy(3, 3));
		builder.add(buildTurnoutButtonBar(), cc.xy(3, 7));

		builder.add(buildMainButtonBar(), cc.xyw(1, 9, 3));
		add(builder.getPanel());

	}

	private Component buildTurnoutButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addTurnoutButton,
				removeTurnoutButton);
	}

	private Component buildGroupButtonBar() {
		return ButtonBarFactory.buildCenteredBar(editGroupButton,
				addGroupButton, removeGroupButton);
	}

	private Component buildMainButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	private void initComponents() {
		turnoutGroups = new ArrayListModel<TurnoutGroup>(
				turnoutPersistence.getAllTurnoutGroups());

		turnoutGroupModel = new SelectionInList<TurnoutGroup>(
				(ListModel) turnoutGroups);

		turnoutGroupList = BasicComponentFactory.createList(turnoutGroupModel,
				new TurnoutGroupListCellRenderer());
		turnoutGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		turnoutGroupList.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"deleteTurnoutGroup");
		turnoutGroupList.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"deleteTurnoutGroup");
		turnoutGroupList.getActionMap().put("deleteTurnoutGroup",
				new RemoveTurnoutGroupAction());

		turnoutGroupConfig = new TurnoutGroupConfigPanel();

		editGroupButton = new JButton(new EditTurnoutGroupAction());
		addGroupButton = new JButton(new AddTurnoutGroupAction());
		removeGroupButton = new JButton(new RemoveTurnoutGroupAction());

		turnouts = new ArrayListModel<Turnout>();
		SelectionInList<Turnout> turnoutModel = new SelectionInList<Turnout>(
				(ListModel) turnouts);

		turnoutModel.setList(turnouts);
		turnoutsTable = new JTable();
		turnoutsTable.setModel(new TurnoutTableModel(turnoutModel));

		turnoutsTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		turnoutsTable.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTurnout");
		turnoutsTable.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"deleteTurnout");
		turnoutsTable.getActionMap().put("deleteTurnout",
				new RemoveTurnoutAction());
		TableColumn typeColumn = turnoutsTable.getColumnModel().getColumn(1);
		typeColumn.setCellRenderer(new TurnoutTypeCellRenderer());

		TableColumn defaultStateColumn = turnoutsTable.getColumnModel()
				.getColumn(8);
		defaultStateColumn
				.setCellRenderer(new TurnoutDefaultStateCellRenderer());
		turnoutsTable.setRowHeight(30);

		addTurnoutButton = new JButton(new AddTurnoutAction());
		removeTurnoutButton = new JButton(new RemoveTurnoutAction());

		okButton = new JButton("OK",
				ImageTools.createImageIconFromIconSet("ok.png"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				setVisible(false);
			}
		});
	}

	private void initEventHandling() {
		turnoutGroupList
				.addListSelectionListener(new TurnoutGroupSelectionHandler());
		turnoutsTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditTurnoutAction().actionPerformed(null);
				}
			}

		});
	}

	/**
	 * Sets the selected group as bean in the details model.
	 */
	private final class TurnoutGroupSelectionHandler implements
			ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {

			if (e.getValueIsAdjusting()) {
				return;
			}
			if (turnoutGroupList.getSelectedIndex() == -1) {
				turnoutGroupList.setSelectedIndex(0);
			}
			TurnoutGroup selectedGroup = (TurnoutGroup) turnoutGroupList
					.getSelectedValue();
			if (selectedGroup == null) {
				return;
			}

			turnouts.clear();
			turnouts.addAll(selectedGroup.getTurnouts());

			turnoutGroupConfig.setTurnoutGroup(selectedGroup);
			TableResizer.adjustColumnWidths(turnoutsTable, 5);
		}
	}

	private class EditTurnoutGroupAction extends AbstractAction {
		public EditTurnoutGroupAction() {
			super("Edit Group");
			// , ImageTools.createImageIconFromIconSet("edit.png")
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			TurnoutGroup groupToEdit = (TurnoutGroup) (turnoutGroupList
					.getSelectedValue());

			turnoutPersistence.updateTurnoutGroup(groupToEdit);

		}
	}

	private class AddTurnoutGroupAction extends AbstractAction {
		public AddTurnoutGroupAction() {
			super("Add Group", ImageTools.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String newGroupName = JOptionPane.showInputDialog(
					TurnoutConfigurationDialog.this,
					"Enter the name of the new Turnout-Group",
					"Add Turnout-Group", JOptionPane.QUESTION_MESSAGE);
			if (newGroupName == null) {
				return;
			}
			TurnoutGroup newTurnoutGroup = new TurnoutGroup();
			newTurnoutGroup.setName(newGroupName);
			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
				String newAmount = JOptionPane.showInputDialog(
						TurnoutConfigurationDialog.this,
						"How many Turnouts should be in this group?", "10");
				int newOffset = 1;
				for (TurnoutGroup group : turnoutPersistence
						.getAllTurnoutGroups()) {
					newOffset += group.getTurnoutNumberAmount();
				}
				newTurnoutGroup.setTurnoutNumberOffset(newOffset);
				newTurnoutGroup.setTurnoutNumberAmount(Integer
						.parseInt(newAmount));
			}

			turnoutPersistence.addTurnoutGroup(newTurnoutGroup);
			turnoutGroupConfig.setTurnoutGroup(null);

		}
	}

	private class RemoveTurnoutGroupAction extends AbstractAction {
		public RemoveTurnoutGroupAction() {
			super("Remove Group", ImageTools
					.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			TurnoutGroup groupToDelete = turnoutGroupModel.getSelection();
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
				turnoutPersistence.removeTurnoutGroup(groupToDelete);
				turnoutGroupConfig.setTurnoutGroup(null);
			}
		}
	}

	private class AddTurnoutAction extends AbstractAction {
		public AddTurnoutAction() {
			super("Add", ImageTools.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			TurnoutGroup selectedTurnoutGroup = turnoutGroupModel
					.getSelection();
			if (selectedTurnoutGroup == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a switch group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int nextNumber = 0;
			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
				nextNumber = turnoutPersistence
						.getNextFreeTurnoutNumberOfGroup(selectedTurnoutGroup);
				if (nextNumber == -1) {
					JOptionPane.showMessageDialog(
							TurnoutConfigurationDialog.this,
							"No more free numbers in this group", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				nextNumber = turnoutPersistence.getNextFreeTurnoutNumber();
			}
			Turnout newTurnout = createDefaultTurnout(selectedTurnoutGroup,
					nextNumber);

			TurnoutConfig switchConfig = new TurnoutConfig(
					TurnoutConfigurationDialog.this, newTurnout);
			if (switchConfig.isOkPressed()) {
				turnouts.clear();
				turnouts.addAll(selectedTurnoutGroup.getTurnouts());
			}
		}

		private Turnout createDefaultTurnout(TurnoutGroup selectedTurnoutGroup,
				int nextNumber) {
			Turnout newTurnout = new Turnout();
			newTurnout.setNumber(nextNumber);

			newTurnout.setBus1(Preferences.getInstance().getIntValue(
					PreferencesKeys.DEFAULT_TURNOUT_BUS));
			newTurnout.setBus2(Preferences.getInstance().getIntValue(
					PreferencesKeys.DEFAULT_TURNOUT_BUS));

			newTurnout.setTurnoutGroup(selectedTurnoutGroup);
			newTurnout.setDefaultState(TurnoutState.STRAIGHT);
			newTurnout.setOrientation(TurnoutOrientation.EAST);
			newTurnout.setTurnoutType(TurnoutType.DEFAULT);
			return newTurnout;
		}
	}

	private class RemoveTurnoutAction extends AbstractAction {

		public RemoveTurnoutAction() {
			super("Remove", ImageTools.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (turnoutsTable.isEditing()) {
				turnoutsTable.getCellEditor().stopCellEditing();
			}

			int[] rows = turnoutsTable.getSelectedRows();
			int[] numbers = new int[rows.length];
			int i = 0;
			for (int row : rows) {
				numbers[i] = (Integer) turnoutsTable.getValueAt(row, 0);
				i++;
			}
			for (int number : numbers) {
				Turnout turnoutByNumber = turnoutPersistence
						.getTurnoutByNumber(number);
				turnoutPersistence.removeTurnout(turnoutByNumber);
			}
			turnoutsTable.clearSelection();
			turnouts.clear();

			TurnoutGroup selectedTurnoutGroup = turnoutGroupModel
					.getSelection();
			turnouts.addAll(selectedTurnoutGroup.getTurnouts());
		}
	}

	private class EditTurnoutAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {

			int row = turnoutsTable.getSelectedRow();
			int number = (Integer) turnoutsTable.getValueAt(row, 0);
			PresentationModel<Turnout> model = new PresentationModel<Turnout>(
					turnoutPersistence.getTurnoutByNumber(number));
			new TurnoutConfig(TurnoutConfigurationDialog.this, model);
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	@Override
	public void turnoutsUpdated(List<TurnoutGroup> turnoutGroups) {
	}

	@Override
	public void turnoutUpdated(Turnout turnout) {

	}

	@Override
	public void turnoutRemoved(Turnout turnout) {
	}

	@Override
	public void turnoutAdded(Turnout turnout) {

	}

	@Override
	public void turnoutGroupAdded(TurnoutGroup group) {
		turnoutGroups.add(group);
	}

	@Override
	public void turnoutGroupRemoved(TurnoutGroup group) {
		turnoutGroups.remove(group);

	}

	@Override
	public void turnoutGroupUpdated(TurnoutGroup group) {
		turnoutGroups.remove(group);
		turnoutGroups.add(group);

	}

	@Override
	public void failure(TurnoutManagerException arg0) {

	}
}
