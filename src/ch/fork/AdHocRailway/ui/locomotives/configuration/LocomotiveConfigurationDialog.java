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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.TableResizer;
import ch.fork.AdHocRailway.ui.TutorialUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LocomotiveConfigurationDialog extends JDialog {

	private boolean								okPressed;

	private LocomotivePersistenceIface			locomotivePersistence;

	private JTable								locomotivesTable;

	private JList								locomotiveGroupList;

	private SelectionInList<LocomotiveGroup>	locomotiveGroupModel;

	private JButton								addLocomotiveButton;

	private JButton								removeLocomotiveButton;

	private JButton								addGroupButton;

	private JButton								removeGroupButton;

	private SelectionInList<Locomotive>			locomotiveModel;

	private JButton								okButton;

	public LocomotiveConfigurationDialog(JFrame owner) {
		super(owner, "Locomotive Configuration", true);
		locomotivePersistence = AdHocRailway.getInstance()
				.getLocomotivePersistence();
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

		FormLayout layout = new FormLayout("pref, 10dlu, pref:grow",
				"pref, 3dlu, fill:pref:grow, 10dlu, pref, 10dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Locomotive Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(locomotiveGroupList), cc.xy(1, 3));
		builder.add(buildGroupButtonBar(), cc.xy(1, 5));

		builder.addSeparator("Locomotives", cc.xyw(3, 1, 1));
		builder.add(new JScrollPane(locomotivesTable), cc.xy(3, 3));
		builder.add(buildLocomotiveButtonBar(), cc.xy(3, 5));

		builder.add(buildMainButtonBar(), cc.xyw(1, 7, 3));
		add(builder.getPanel());

	}

	private Component buildLocomotiveButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addLocomotiveButton,
				removeLocomotiveButton);
	}

	private Component buildGroupButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addGroupButton,
				removeGroupButton);
	}

	private Component buildMainButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	private void initComponents() {
		ArrayListModel<LocomotiveGroup> locomotiveGroups = locomotivePersistence
				.getAllLocomotiveGroups();
		locomotiveGroupModel = new SelectionInList<LocomotiveGroup>(
				(ListModel) locomotiveGroups);

		locomotiveGroupList = BasicComponentFactory
				.createList(locomotiveGroupModel);
		locomotiveGroupList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		locomotiveGroupList
				.setCellRenderer(new LocomotiveGroupListCellRenderer());

		addGroupButton = new JButton(new AddLocomotiveGroupAction());
		removeGroupButton = new JButton(new RemoveLocomotiveGroupAction());

		locomotiveModel = new SelectionInList<Locomotive>();
		locomotivesTable = new JTable();
		locomotivesTable.setModel(new LocomotiveTableModel(locomotiveModel));
		locomotivesTable.setSelectionModel(new SingleListSelectionAdapter(
				locomotiveModel.getSelectionIndexHolder()));

		addLocomotiveButton = new JButton(new AddLocomotiveAction());
		removeLocomotiveButton = new JButton(new RemoveLocomotiveAction());

		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					locomotivePersistence.flush();
				} catch (LocomotivePersistenceException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				} finally {
					okPressed = true;
					setVisible(false);
				}
			}

		});
	}

	private void initEventHandling() {
		locomotiveGroupList
				.addListSelectionListener(new LocomotiveGroupSelectionHandler());
		locomotivesTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditLocomotiveAction().actionPerformed(null);
				}
			}

		});
	}

	/**
	 * Sets the selected group as bean in the details model.
	 */
	private final class LocomotiveGroupSelectionHandler implements
			ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			if (locomotiveGroupList.getSelectedIndex() == -1)
				locomotiveGroupList.setSelectedIndex(0);
			LocomotiveGroup selectedGroup = (LocomotiveGroup) locomotiveGroupList
					.getSelectedValue();
			List<Locomotive> locomotives = new ArrayList<Locomotive>(
					selectedGroup.getLocomotives());
			locomotiveModel.setList(locomotives);
			TableResizer.adjustColumnWidths(locomotivesTable, 5);
		}
	}

	// TableModel *************************************************************

	/**
	 * Describes how to present an Album in a JTable.
	 */
	private static final class LocomotiveTableModel extends
			AbstractTableAdapter<Locomotive> {

		private static final String[]	COLUMNS	= { "Name", "Type", "Bus",
														"Address", "Image",
														"Desc" };

		private LocomotiveTableModel(ListModel listModel) {
			super(listModel, COLUMNS);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Locomotive locomotive = getRow(rowIndex);
			switch (columnIndex) {
			case 0:
				return locomotive.getName();
			case 1:
				return locomotive.getLocomotiveType();
			case 2:
				return locomotive.getBus();
			case 3:
				return locomotive.getAddress();
			case 4:
				return locomotive.getImage();
			case 5:
				return locomotive.getDescription();
			default:
				throw new IllegalStateException("Unknown column");
			}
		}

	}

	/**
	 * Used to renders LocomotiveGroups in JLists and JComboBoxes. If the combo
	 * box selection is null, an empty text <code>""</code> is rendered.
	 */
	private static final class LocomotiveGroupListCellRenderer extends
			DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);

			LocomotiveGroup group = (LocomotiveGroup) value;
			setText(group == null ? "" : (" " + group.getName()));
			return component;
		}
	}

	private class AddLocomotiveGroupAction extends AbstractAction {

		public AddLocomotiveGroupAction() {
			super("Add", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent arg0) {
			String newGroupName = JOptionPane.showInputDialog(
					LocomotiveConfigurationDialog.this,
					"Enter the name of the new Locomotive-Group",
					"Add Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
			LocomotiveGroup newSection = new LocomotiveGroup(0, newGroupName);
			locomotivePersistence.addLocomotiveGroup(newSection);
		}
	}

	private class RemoveLocomotiveGroupAction extends AbstractAction {
		public RemoveLocomotiveGroupAction() {
			super("Remove", ImageTools.createImageIcon("remove.png"));
		}

		public void actionPerformed(ActionEvent arg0) {
			LocomotiveGroup groupToDelete = (LocomotiveGroup) (locomotiveGroupList
					.getSelectedValue());
			int response = JOptionPane.showConfirmDialog(
					LocomotiveConfigurationDialog.this,
					"Really remove Locomotive-Group '"
							+ groupToDelete.getName() + "' ?",
					"Remove Locomotive-Group", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				try {
					locomotivePersistence.deleteLocomotiveGroup(groupToDelete);
				} catch (LocomotivePersistenceException e) {
					ExceptionProcessor.getInstance().processException(e);
				}
			}
		}
	}

	private class AddLocomotiveAction extends AbstractAction {
		public AddLocomotiveAction() {
			super("Add", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent e) {
			LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) (locomotiveGroupList
					.getSelectedValue());
			Locomotive newLocomotive = new Locomotive();
			newLocomotive.setLocomotiveGroup(selectedLocomotiveGroup);
			LocomotiveConfig locomotiveConfig = new LocomotiveConfig(
					LocomotiveConfigurationDialog.this, newLocomotive);
			if (locomotiveConfig.isOkPressed()) {
				selectedLocomotiveGroup.getLocomotives().add(newLocomotive);
				locomotivePersistence.addLocomotive(newLocomotive);
				List<Locomotive> locomotives = new ArrayList<Locomotive>(
						selectedLocomotiveGroup.getLocomotives());
				locomotiveModel.setList(locomotives);
			}
		}
	}

	private class EditLocomotiveAction extends AbstractAction {
		public EditLocomotiveAction() {
			super("Edit");
		}

		public void actionPerformed(ActionEvent e) {

			PresentationModel<Locomotive> model = new PresentationModel<Locomotive>(
					locomotiveModel);
			LocomotiveConfig turnoutConfig = new LocomotiveConfig(
					LocomotiveConfigurationDialog.this, model);
			if (turnoutConfig.isOkPressed()) {
				locomotivePersistence.updateLocomotive(model.getBean());
			}
		}
	}

	private class RemoveLocomotiveAction extends AbstractAction {
		public RemoveLocomotiveAction() {
			super("Remove", ImageTools.createImageIcon("remove.png"));
		}

		public void actionPerformed(ActionEvent e) {
			LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) locomotiveGroupList
					.getSelectedValue();

			int row = locomotivesTable.getSelectedRow();
			Locomotive locomotiveToDelete = locomotiveModel.getElementAt(row);

			locomotivePersistence.deleteLocomotive(locomotiveToDelete);
			List<Locomotive> locomotives = new ArrayList<Locomotive>(
					selectedLocomotiveGroup.getLocomotives());
			locomotiveModel.setList(locomotives);
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}
}
