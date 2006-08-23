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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchOrientation;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchState;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.TableResizer;

public class SwitchConfigurationDialog extends JDialog {
    private List<SwitchGroup>          switchGroupsWorkCopy;
    private Map<Integer, Switch>       switchNumberToSwitchWorkCopy;
    private ListListModel<SwitchGroup> switchGroupListModel;
    private JPopupMenu                 switchGroupPopupMenu;
    private JList                      switchGroupList;
    private JPanel                     switchesPanel;
    private TableModel                 switchesTableModel;
    private JTable                     switchesTable;
    private Frame                      owner;
    private boolean                    cancelPressed = false;
    private boolean                    okPressed     = false;
    private SwitchControl              switchControl;

    public SwitchConfigurationDialog(Frame owner) {
        super(owner, "Switch Configuration", true);
        this.owner = owner;
        this.switchControl = SwitchControl.getInstance();
        this.switchNumberToSwitchWorkCopy = new HashMap<Integer, Switch>();
        for (Switch s : switchControl.getNumberToSwitch().values()) {
            Switch clone = s.clone();
            this.switchNumberToSwitchWorkCopy.put(clone.getNumber(), clone);
        }
        this.switchGroupsWorkCopy = new ArrayList<SwitchGroup>();
        for (SwitchGroup sg : switchControl.getSwitchGroups()) {
            SwitchGroup clone = sg.clone();
            this.switchGroupsWorkCopy.add(clone);
            for (Switch s : sg.getSwitches()) {
                clone.addSwitch(this.switchNumberToSwitchWorkCopy.get(s
                    .getNumber()));
            }
        }
        initGUI();
    }

    private void initGUI() {
        JPanel switchGroupPanel = createSwitchGroupPanel();
        add(switchGroupPanel, BorderLayout.WEST);
        JPanel switchesPanel = createSwitchesPanel();
        add(switchesPanel, BorderLayout.CENTER);
        updateSwitchesPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                SwitchConfigurationDialog.this.setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                SwitchConfigurationDialog.this.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    private JPanel createSwitchGroupPanel() {
        JPanel switchGroupPanel = new JPanel(new BorderLayout());
        TitledBorder title = BorderFactory.createTitledBorder("Switch Groups");
        switchGroupPanel.setBorder(title);
        switchGroupPanel.getInsets(new Insets(5, 5, 5, 5));
        switchGroupListModel = new ListListModel<SwitchGroup>(
            switchGroupsWorkCopy);
        switchGroupList = new JList(switchGroupListModel);
        switchGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        switchGroupPopupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add");
        JMenuItem removeItem = new JMenuItem("Remove");
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem moveUpItem = new JMenuItem("Move up");
        JMenuItem moveDownItem = new JMenuItem("Move down");
        switchGroupPopupMenu.add(addItem);
        switchGroupPopupMenu.add(removeItem);
        switchGroupPopupMenu.add(renameItem);
        switchGroupPopupMenu.add(new JSeparator());
        switchGroupPopupMenu.add(moveUpItem);
        switchGroupPopupMenu.add(moveDownItem);
        JButton addSwitchGroupButton = new JButton("Add");
        JButton removeSwitchGroupButton = new JButton("Remove");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addSwitchGroupButton);
        buttonPanel.add(removeSwitchGroupButton);
        switchGroupPanel.add(switchGroupList, BorderLayout.CENTER);
        switchGroupPanel.add(buttonPanel, BorderLayout.SOUTH);
        /* Install ActionListeners */
        switchGroupList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateSwitchesPanel();
            }
        });
        switchGroupList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    switchGroupPopupMenu.show(e.getComponent(), e.getX(), e
                        .getY());
                }
            }
        });
        addItem.addActionListener(new AddSwitchGroupAction());
        addSwitchGroupButton.addActionListener(new AddSwitchGroupAction());
        removeItem.addActionListener(new RemoveSwitchGroupAction());
        removeSwitchGroupButton
            .addActionListener(new RemoveSwitchGroupAction());
        renameItem.addActionListener(new RenameSwitchGroupAction());
        moveUpItem.addActionListener(new MoveSwitchGroupAction(true));
        moveDownItem.addActionListener(new MoveSwitchGroupAction(false));
        return switchGroupPanel;
    }

    private JPanel createSwitchesPanel() {
        switchesPanel = new JPanel(new BorderLayout());
        switchesPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "escapeAction");
        switchesPanel.getActionMap().put("escapeAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                SwitchConfigurationDialog.this.setVisible(false);
            }
        });
        switchesPanel.getInsets(new Insets(5, 5, 5, 5));
        TitledBorder title = BorderFactory.createTitledBorder("Switch-Group");
        switchesPanel.setBorder(title);
        switchesTableModel = new SwitchesTableModel(
            switchNumberToSwitchWorkCopy);
        switchesTable = new JTable(switchesTableModel);
        switchesTable.setRowHeight(24);

        // SwitchType
        JComboBox switchTypeComboBox = new JComboBox();
        switchTypeComboBox.addItem("DefaultSwitch");
        switchTypeComboBox.addItem("DoubleCrossSwitch");
        switchTypeComboBox.addItem("ThreeWaySwitch");
        switchTypeComboBox.setRenderer(new SwitchTypeComboBoxCellRenderer());
        TableColumn typeColumn = switchesTable.getColumnModel().getColumn(1);
        typeColumn.setCellEditor(new DefaultCellEditor(switchTypeComboBox));
        typeColumn.setCellRenderer(new SwitchTypeCellRenderer());

        // DefaultState
        JComboBox switchDefaultStateComboBox = new JComboBox();
        switchDefaultStateComboBox.addItem(SwitchState.STRAIGHT);
        switchDefaultStateComboBox.addItem(SwitchState.LEFT);
        switchDefaultStateComboBox
            .setRenderer(new SwitchDefaultStateComboBoxCellRenderer());
        TableColumn defaultStateColumn = switchesTable.getColumnModel()
            .getColumn(7);
        defaultStateColumn.setCellEditor(new DefaultCellEditor(
            switchDefaultStateComboBox));
        defaultStateColumn
            .setCellRenderer(new SwitchDefaultStateCellRenderer());

        // SwitchOrientation
        JComboBox switchOrientationComboBox = new JComboBox();
        switchOrientationComboBox.addItem(SwitchOrientation.NORTH);
        switchOrientationComboBox.addItem(SwitchOrientation.EAST);
        switchOrientationComboBox.addItem(SwitchOrientation.SOUTH);
        switchOrientationComboBox.addItem(SwitchOrientation.WEST);

        TableColumn switchOrientationColumn = switchesTable.getColumnModel()
            .getColumn(8);
        switchOrientationColumn.setCellEditor(new DefaultCellEditor(
            switchOrientationComboBox));

        JScrollPane switchGroupTablePane = new JScrollPane(switchesTable);
        switchGroupTablePane.setPreferredSize(new Dimension(750, 400));

        switchesPanel.add(switchGroupTablePane, BorderLayout.CENTER);


        JButton addSwitchButton = new JButton("Add");
        JButton add10SwitchesButton = new JButton("Add 10 Switches");
        JButton removeSwitchButton = new JButton("Remove");

        addSwitchButton.addActionListener(new AddSwitchAction());
        add10SwitchesButton.addActionListener(new Add10SwitchesAction());
        removeSwitchButton.addActionListener(new RemoveSwitchAction());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(addSwitchButton);
        buttonPanel.add(add10SwitchesButton);
        buttonPanel.add(removeSwitchButton);

        switchesPanel.add(buttonPanel, BorderLayout.SOUTH);
        return switchesPanel;
    }

    private void updateSwitchesPanel() {
        SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
            .getSelectedValue());
        if (selectedSwitchGroup == null) {
            ((TitledBorder) switchesPanel.getBorder()).setTitle("Switch-Group");
        } else {
            ((TitledBorder) switchesPanel.getBorder())
                .setTitle("Switch-Group '" + selectedSwitchGroup.getName()
                    + "'");
        }
        ((SwitchesTableModel) switchesTableModel)
            .setSwitchGroup(selectedSwitchGroup);
        TableResizer.adjustColumnWidths(switchesTable, 30);
        if (switchesTable.getRowCount() > 0) {
            TableResizer.adjustRowHeight(switchesTable);
        }
        pack();
    }

    public List<SwitchGroup> getSwitchGroups() {
        return switchGroupsWorkCopy;
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public Map<Integer, Switch> getSwitchNumberToSwitch() {
        return switchNumberToSwitchWorkCopy;
    }

    private class AddSwitchGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent arg0) {
            String newGroupName = JOptionPane.showInputDialog(
                SwitchConfigurationDialog.this,
                "Enter the name of the new Switch-Group", "Add Switch-Group",
                JOptionPane.QUESTION_MESSAGE);
            SwitchGroup newSection = new SwitchGroup(newGroupName);
            switchGroupsWorkCopy.add(newSection);
            switchGroupListModel.updated();
            switchGroupList.setSelectedValue(newSection, true);
        }
    }
    private class RemoveSwitchGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent arg0) {
            SwitchGroup groupToDelete = (SwitchGroup) (switchGroupList
                .getSelectedValue());
            int response = JOptionPane.showConfirmDialog(
                SwitchConfigurationDialog.this, "Really remove Switch-Group '"
                    + groupToDelete.getName() + "' ?", "Remove Switch-Group",
                JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                switchGroupsWorkCopy.remove(groupToDelete);
                switchGroupListModel.updated();
            }
        }
    }
    private class RenameSwitchGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            SwitchGroup groupToRename = (SwitchGroup) (switchGroupList
                .getSelectedValue());
            String newSectionName = JOptionPane.showInputDialog(
                SwitchConfigurationDialog.this, "Enter new name",
                "Rename Switch-Group", JOptionPane.QUESTION_MESSAGE);
            if (!newSectionName.equals("")) {
                groupToRename.setName(newSectionName);
                switchGroupListModel.updated();
            }
        }
    }
    private class MoveSwitchGroupAction extends AbstractAction {
        private boolean up;

        public MoveSwitchGroupAction(boolean up) {
            this.up = up;
        }

        public void actionPerformed(ActionEvent e) {
            SwitchGroup groupToMove = (SwitchGroup) (switchGroupList
                .getSelectedValue());
            int oldIndex = switchGroupsWorkCopy.indexOf(groupToMove);
            int newIndex = oldIndex;
            if (up) {
                if (oldIndex != 0) {
                    newIndex = oldIndex - 1;
                } else {
                    return;
                }
            } else {
                if (oldIndex != switchGroupsWorkCopy.size() - 1) {
                    newIndex = oldIndex + 1;
                } else {
                    return;
                }
            }
            switchGroupsWorkCopy.remove(oldIndex);
            switchGroupsWorkCopy.add(newIndex, groupToMove);
            switchGroupList.setSelectedIndex(newIndex);
            switchGroupListModel.updated();
            updateSwitchesPanel();
        }
    }
    private class AddSwitchAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
                .getSelectedValue());
            SortedSet<Integer> usedNumbers = new TreeSet<Integer>(
                switchNumberToSwitchWorkCopy.keySet());
            int nextNumber = 1;
            if (usedNumbers.size() == 0) {
                nextNumber = 1;
            } else {
                nextNumber = usedNumbers.last().intValue() + 1;
            }
            Switch newSwitch = new DefaultSwitch(nextNumber, "");
            SwitchConfig switchConfig = new SwitchConfig(
                SwitchConfigurationDialog.this, newSwitch);
            if (switchConfig.isOkPressed()) {
                switchNumberToSwitchWorkCopy.put(switchConfig.getSwitch()
                    .getNumber(), switchConfig.getSwitch());
                selectedSwitchGroup.addSwitch(switchConfig.getSwitch());
            }
            newSwitch = null;
            updateSwitchesPanel();
        }
    }
    private class Add10SwitchesAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
                .getSelectedValue());
            if (selectedSwitchGroup == null) {
                return;
            }
            SortedSet<Integer> usedNumbers = new TreeSet<Integer>(
                switchNumberToSwitchWorkCopy.keySet());
            int nextNumber = 1;
            if (usedNumbers.size() == 0) {
                nextNumber = 1;
            } else {
                nextNumber = usedNumbers.last().intValue() + 1;
            }
            for (int i = 0; i < 10; i++) {
                Switch newSwitch = new DefaultSwitch(nextNumber, "",
                    new Address(Constants.DEFAULT_BUS, nextNumber));
                switchNumberToSwitchWorkCopy.put(newSwitch.getNumber(),
                    newSwitch);
                selectedSwitchGroup.addSwitch(newSwitch);
                nextNumber++;
            }
            updateSwitchesPanel();
        }
    }
    private class RemoveSwitchAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (switchesTable.isEditing())
                switchesTable.getCellEditor().stopCellEditing();
            SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
                .getSelectedValue());
            Integer number = (Integer) switchesTable.getValueAt(switchesTable
                .getSelectedRow(), 0);
            selectedSwitchGroup.removeSwitch(switchNumberToSwitchWorkCopy
                .get(number));
            switchNumberToSwitchWorkCopy.remove(number);
            updateSwitchesPanel();
        }
    }
}
