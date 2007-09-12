/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/configuration/LocomotiveConfigurationDialog.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:53 BST 2006
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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistence;
import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.TableResizer;

public class LocomotiveConfigurationDialog<E> extends
		ConfigurationDialog<LocomotiveConfiguration> {

	private LocomotivePersistence locomotivePersistence;

	private TableModel locomotivesTableModel;

	private JTable locomotivesTable;

	private JPopupMenu locomotiveGroupPopupMenu;

	private JList locomotiveGroupList;

	private JPanel locomotivesPanel;

	private ListListModel locomotiveGroupListModel;
	private JButton addLocomotiveGroupButton;

	private JButton removeLocomotiveGroupButton;

	private JButton addLocomotiveButton;

	private JButton removeLocomotiveButton;

	private JButton editLocomotiveButton;

	public LocomotiveConfigurationDialog(JFrame owner) {
		super(owner, "Locomotive Configuration");
		locomotivePersistence = LocomotivePersistence.getInstance();
		initGUI();
	}

	private void initGUI() {
		JPanel locomotiveGroupPanel = createLocomotiveGroupPanel();
		JPanel locomotivesPanel = createLocomotivesPanel();
		updateLocomotivesPanel();

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(locomotiveGroupPanel, BorderLayout.WEST);
		mainPanel.add(locomotivesPanel, BorderLayout.CENTER);
		addMainComponent(mainPanel);
		LocomotiveConfigurationFocusTraversalPolicy newPolicy = new LocomotiveConfigurationFocusTraversalPolicy();
		setFocusTraversalPolicy(newPolicy);
		setSize(new Dimension(700, 500));
		setVisible(true);
	}

	@Override
	public void createTempConfiguration() {
		// TODO
		// this.locomotiveControl = LocomotiveControl.getInstance();
		// this.locomotivesWorkCopy = new TreeSet<ControledLocomotive>();
		// for (ControledLocomotive l : locomotiveControl.getLocomotives()) {
		// ControledLocomotive clone = l.clone();
		// this.locomotivesWorkCopy.add(clone);
		// }
		// this.locomotiveGroupsWorkCopy = new ArrayList<LocomotiveGroup>();
		// for (LocomotiveGroup lg : locomotiveControl.getLocomotiveGroups()) {
		// LocomotiveGroup clone = lg.clone();
		// this.locomotiveGroupsWorkCopy.add(clone);
		// for (ControledLocomotive l : lg.getLocomotives()) {
		// clone.addLocomotive(l);
		// }
		// }
	}

	@Override
	public LocomotiveConfiguration getTempConfiguration() {
		// TODO
		// return new LocomotiveConfiguration(this.locomotiveGroupsWorkCopy,
		// this.locomotivesWorkCopy);
		return null;
	}

	private JPanel createLocomotiveGroupPanel() {
		JPanel locomotiveGroupPanel = new JPanel(new BorderLayout());
		TitledBorder title = BorderFactory
				.createTitledBorder("Locomotive-Groups");
		locomotiveGroupPanel.setBorder(title);
		locomotiveGroupPanel.getInsets(new Insets(5, 5, 5, 5));
		locomotiveGroupListModel = new ListListModel<LocomotiveGroup>(
				new ArrayList<LocomotiveGroup>(locomotivePersistence
						.getAllLocomotiveGroups()));

		locomotiveGroupList = new JList(locomotiveGroupListModel);
		locomotiveGroupList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		locomotiveGroupPopupMenu = new JPopupMenu();
		JMenuItem addItem = new JMenuItem("Add");
		JMenuItem removeItem = new JMenuItem("Remove");
		JMenuItem renameItem = new JMenuItem("Rename");
		locomotiveGroupPopupMenu.add(addItem);
		locomotiveGroupPopupMenu.add(removeItem);
		locomotiveGroupPopupMenu.add(renameItem);
		addLocomotiveGroupButton = new JButton("Add");
		removeLocomotiveGroupButton = new JButton("Remove");
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(addLocomotiveGroupButton);
		buttonPanel.add(removeLocomotiveGroupButton);
		locomotiveGroupPanel.add(locomotiveGroupList, BorderLayout.CENTER);
		locomotiveGroupPanel.add(buttonPanel, BorderLayout.SOUTH);
		/* Install ActionListeners */
		locomotiveGroupList
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						updateLocomotivesPanel();
					}
				});
		locomotiveGroupList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					locomotiveGroupPopupMenu.show(e.getComponent(), e.getX(), e
							.getY());
				}
			}
		});
		addItem.addActionListener(new AddLocomotiveGroupAction());
		addLocomotiveGroupButton
				.addActionListener(new AddLocomotiveGroupAction());
		removeItem.addActionListener(new RemoveLocomotiveGroupAction());
		removeLocomotiveGroupButton
				.addActionListener(new RemoveLocomotiveGroupAction());
		renameItem.addActionListener(new RenameLocomotiveGroupAction());
		return locomotiveGroupPanel;
	}

	private JPanel createLocomotivesPanel() {
		locomotivesPanel = new JPanel(new BorderLayout());
		locomotivesPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "escapeAction");
		locomotivesPanel.getActionMap().put("escapeAction",
				new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						cancelPressed = true;
						LocomotiveConfigurationDialog.this.setVisible(false);
					}
				});
		locomotivesPanel.getInsets(new Insets(5, 5, 5, 5));
		TitledBorder title = BorderFactory
				.createTitledBorder("Locomotive-Group");
		locomotivesPanel.setBorder(title);

		locomotivesTableModel = new LocomotiveTableModel(null);

		locomotivesTable = new JTable(locomotivesTableModel);
		JScrollPane switchGroupTablePane = new JScrollPane(locomotivesTable,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		switchGroupTablePane.setPreferredSize(new Dimension(600, 400));

		locomotivesPanel.add(switchGroupTablePane, BorderLayout.CENTER);

		JComboBox locomotiveTypeCombobox = new JComboBox();
		locomotiveTypeCombobox.addItem("DeltaLocomotive");
		locomotiveTypeCombobox.addItem("DigitalLocomotive");
		TableColumn locomotiveTypeColumn = locomotivesTable.getColumnModel()
				.getColumn(1);
		locomotiveTypeColumn.setCellEditor(new DefaultCellEditor(
				locomotiveTypeCombobox));

		locomotivesTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditLocomotiveAction().actionPerformed(null);
				}
			}

		});

		addLocomotiveButton = new JButton("Add");
		editLocomotiveButton = new JButton("Edit");
		removeLocomotiveButton = new JButton("Remove");
		addLocomotiveButton.addActionListener(new AddLocomotiveAction());
		editLocomotiveButton.addActionListener(new EditLocomotiveAction());
		removeLocomotiveButton.addActionListener(new RemoveLocomotiveAction());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(addLocomotiveButton);
		buttonPanel.add(editLocomotiveButton);
		buttonPanel.add(removeLocomotiveButton);
		locomotivesPanel.add(buttonPanel, BorderLayout.SOUTH);
		return locomotivesPanel;
	}

	private void updateLocomotivesPanel() {
		LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) (locomotiveGroupList
				.getSelectedValue());
		if (selectedLocomotiveGroup == null) {
			locomotivesPanel.setBorder(BorderFactory
					.createTitledBorder("Locomotive-Group"));
		} else {
			locomotivesPanel.setBorder(BorderFactory
					.createTitledBorder("Locomotive-Group '"
							+ selectedLocomotiveGroup.getName() + "'"));
		}
		// TODO
		((LocomotiveTableModel) locomotivesTableModel)
				.setLocomotiveGroup(selectedLocomotiveGroup);
		TableResizer.adjustColumnWidths(locomotivesTable, 30);
		if (locomotivesTable.getRowCount() > 0) {
			TableResizer.adjustRowHeight(locomotivesTable);
		}
		locomotivesTable.revalidate();
		locomotivesTable.repaint();
	}

	private class AddLocomotiveGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent arg0) {
			String newGroupName = JOptionPane.showInputDialog(
					LocomotiveConfigurationDialog.this,
					"Enter the name of the new Locomotive-Group",
					"Add Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
			// TODO
			LocomotiveGroup newSection = new LocomotiveGroup(0, newGroupName);
			locomotivePersistence.addLocomotiveGroup(newSection);
			locomotiveGroupListModel = new ListListModel<LocomotiveGroup>(
					new ArrayList<LocomotiveGroup>(locomotivePersistence
							.getAllLocomotiveGroups()));

			locomotiveGroupList.setModel(locomotiveGroupListModel);
			locomotiveGroupList.setSelectedValue(newSection, true);
		}
	}

	private class RemoveLocomotiveGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent arg0) {
			LocomotiveGroup groupToDelete = (LocomotiveGroup) (locomotiveGroupList
					.getSelectedValue());
			int response = JOptionPane.showConfirmDialog(
					LocomotiveConfigurationDialog.this,
					"Really remove Locomotive-Group '"
							+ groupToDelete.getName() + "' ?",
					"Remove Locomotive-Group", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				locomotivePersistence.deleteLocomotiveGroup(groupToDelete);
				locomotiveGroupListModel = new ListListModel<LocomotiveGroup>(
						new ArrayList<LocomotiveGroup>(locomotivePersistence
								.getAllLocomotiveGroups()));

				locomotiveGroupList.setModel(locomotiveGroupListModel);
			}
		}
	}

	private class RenameLocomotiveGroupAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			LocomotiveGroup groupToRename = (LocomotiveGroup) (locomotiveGroupList
					.getSelectedValue());
			String newSectionName = JOptionPane.showInputDialog(
					LocomotiveConfigurationDialog.this, "Enter new name",
					"Rename Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
			if (!newSectionName.equals("")) {
				groupToRename.setName(newSectionName);
				locomotivePersistence.updateLocomotiveGroup(groupToRename);
				locomotiveGroupListModel = new ListListModel<LocomotiveGroup>(
						new ArrayList<LocomotiveGroup>(locomotivePersistence
								.getAllLocomotiveGroups()));

				locomotiveGroupList.setModel(locomotiveGroupListModel);
			}
		}
	}

	private class AddLocomotiveAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) (locomotiveGroupList
					.getSelectedValue());
			Locomotive newLocomotive = new Locomotive();
			newLocomotive.setLocomotiveGroup(selectedLocomotiveGroup);
			LocomotiveConfig locomotiveConfig = new LocomotiveConfig(
					LocomotiveConfigurationDialog.this, newLocomotive);
			if (locomotiveConfig.isOkPressed()) {

				locomotivePersistence.addLocomotive(locomotiveConfig
						.getLocomotive());
			}

			updateLocomotivesPanel();

		}
	}

	private class EditLocomotiveAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (locomotivesTable.isEditing())
				locomotivesTable.getCellEditor().stopCellEditing();

			Locomotive locomotiveToEdit = ((LocomotiveTableModel) locomotivesTableModel)
					.getLocomotiveAt(locomotivesTable.getSelectedRow());
			LocomotiveConfig locomotiveConfig = new LocomotiveConfig(
					LocomotiveConfigurationDialog.this, locomotiveToEdit);
			if (locomotiveConfig.isOkPressed()) {

				locomotivePersistence.addLocomotive(locomotiveConfig
						.getLocomotive());
			}
			updateLocomotivesPanel();
		}
	}

	private class RemoveLocomotiveAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (locomotivesTable.isEditing())
				locomotivesTable.getCellEditor().stopCellEditing();

			Locomotive locomotiveToRemove = ((LocomotiveTableModel) locomotivesTableModel)
					.getLocomotiveAt(locomotivesTable.getSelectedRow());
			locomotivePersistence.deleteLocomotive(locomotiveToRemove);
			updateLocomotivesPanel();
		}
	}

	private class LocomotiveConfigurationFocusTraversalPolicy extends
			FocusTraversalPolicy {

		public Component getComponentAfter(Container focusCycleRoot,
				Component aComponent) {
			if (aComponent.equals(okButton)) {
				return cancelButton;
			} else if (aComponent.equals(cancelButton)) {
				return addLocomotiveGroupButton;
			} else if (aComponent.equals(addLocomotiveGroupButton)) {
				return removeLocomotiveGroupButton;
			} else if (aComponent.equals(removeLocomotiveGroupButton)) {
				return addLocomotiveButton;
			} else if (aComponent.equals(addLocomotiveButton)) {
				return removeLocomotiveButton;
			}
			return okButton;
		}

		public Component getComponentBefore(Container focusCycleRoot,
				Component aComponent) {

			if (aComponent.equals(okButton)) {
				return removeLocomotiveButton;
			} else if (aComponent.equals(removeLocomotiveButton)) {
				return addLocomotiveButton;
			} else if (aComponent.equals(addLocomotiveButton)) {
				return removeLocomotiveGroupButton;
			} else if (aComponent.equals(removeLocomotiveGroupButton)) {
				return addLocomotiveGroupButton;
			} else if (aComponent.equals(addLocomotiveGroupButton)) {
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
