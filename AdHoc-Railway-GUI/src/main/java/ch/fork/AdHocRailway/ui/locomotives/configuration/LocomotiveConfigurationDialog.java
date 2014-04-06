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

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.LocomotiveManagerListener;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.ui.TableColumnAdjuster;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveImageHelper;
import ch.fork.AdHocRailway.ui.tools.ImageTools;
import ch.fork.AdHocRailway.ui.tools.SwingUtils;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.factories.ButtonBarFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class LocomotiveConfigurationDialog extends JDialog implements
        LocomotiveManagerListener {

    private final LocomotiveManager locomotiveManager;
    private final LocomotiveContext ctx;
    private boolean okPressed;
    private JTable locomotivesTable;
    private JList<LocomotiveGroup> locomotiveGroupList;
    private JButton addLocomotiveButton;
    private JButton removeLocomotiveButton;
    private JButton addGroupButton;
    private JButton removeGroupButton;
    private JButton okButton;
    private LocomotiveGroupSelectionHandler groupSelectionHandler;
    private JButton editGroupButton;
    private SelectionInList<LocomotiveGroup> locomotiveGroupModel;
    private SelectionInList<Locomotive> locomotiveModel;
    private ArrayListModel<LocomotiveGroup> locomotiveGroups;
    private ArrayListModel<Locomotive> locomotives;
    private boolean disableListener;
    private JScrollPane groupScrollPane;
    private JScrollPane locomotiveTableScrollPane;
    private LocomotiveGroupConfigPanel locomotiveGroupConfig;
    private TableColumnAdjuster tca;

    public LocomotiveConfigurationDialog(final LocomotiveContext ctx,
                                         final JFrame owner) {
        super(owner, "Locomotive Configuration", true);
        this.ctx = ctx;
        this.locomotiveManager = ctx.getLocomotiveManager();
        initGUI();
    }

    private void initGUI() {
        initComponents();
        buildPanel();
        initEventHandling();
        locomotiveManager.addLocomotiveManagerListener(this);
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void initComponents() {
        locomotiveGroups = new ArrayListModel<LocomotiveGroup>();

        locomotiveGroupModel = new SelectionInList<LocomotiveGroup>(
                (ListModel<LocomotiveGroup>) locomotiveGroups);

        locomotiveGroupList = BasicComponentFactory
                .createList(locomotiveGroupModel);
        locomotiveGroupList
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locomotiveGroupList
                .setCellRenderer(new LocomotiveGroupListCellRenderer());

        groupScrollPane = new JScrollPane(locomotiveGroupList);

        locomotiveGroupConfig = new LocomotiveGroupConfigPanel();

        addGroupButton = new JButton(new AddLocomotiveGroupAction());
        editGroupButton = new JButton(new EditLocomotiveGroupAction());
        removeGroupButton = new JButton(new RemoveLocomotiveGroupAction());
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {

            Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                setBorder(padding);
                return this;
            }

        };


        locomotivesTable = new JTable();
        locomotivesTable.setDefaultRenderer(Object.class, r);


        locomotives = new ArrayListModel<Locomotive>();

        locomotiveModel = new SelectionInList<Locomotive>();
        locomotiveModel.setList(locomotives);

        locomotivesTable.setModel(new LocomotiveTableModel(locomotiveModel));
        locomotivesTable
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        locomotivesTable.setRowHeight(50);

        final TableColumn imageColumn = locomotivesTable.getColumnModel()
                .getColumn(1);
        imageColumn.setCellRenderer(new LocomotiveImageTableCellRenderer());

        locomotiveTableScrollPane = new JScrollPane(locomotivesTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        locomotivesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tca = new TableColumnAdjuster(locomotivesTable, 10);

        addLocomotiveButton = new JButton(new AddLocomotiveAction());
        removeLocomotiveButton = new JButton(new RemoveLocomotiveAction());

        okButton = new JButton("OK",
                ImageTools.createImageIconFromIconSet("dialog-ok-apply.png"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                okPressed = true;
                setVisible(false);
                locomotiveManager
                        .removeLocomotiveManagerListenerInNextEvent(LocomotiveConfigurationDialog.this);
            }
        });
    }

    private void initEventHandling() {
        groupSelectionHandler = new LocomotiveGroupSelectionHandler();
        locomotiveGroupList.addListSelectionListener(groupSelectionHandler);
        locomotivesTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {
                    new EditLocomotiveAction().actionPerformed(null);
                }
            }

        });
        SwingUtils.addEscapeListener(this);

    }

    private void buildPanel() {

        setLayout(new BorderLayout());
        final JPanel mainPanel = new JPanel(new MigLayout("fill",
                "[shrink][shrink][shrink][grow]", "[shrink][grow][shrink]"));

        mainPanel.add(new JLabel("Locomotive Groups"));
        mainPanel.add(new JSeparator(), "growx");
        mainPanel.add(new JLabel("Locomotives"), "gap unrelated");
        mainPanel.add(new JSeparator(), "growx, wrap");

        mainPanel.add(groupScrollPane, "grow, span 2");
        mainPanel.add(locomotiveTableScrollPane,
                "gap unrelated, grow, span 2 2, w 900, wrap");

        mainPanel.add(locomotiveGroupConfig, "shrink, span 2, wrap");

        mainPanel.add(buildGroupButtonBar(), "span 2, align center");
        mainPanel.add(buildLocomotiveButtonBar(),
                "gap unrelated, span 2, align center, wrap");
        mainPanel.add(buildMainButtonBar(), "span 4, align right");
        add(mainPanel, BorderLayout.CENTER);

    }

    private Component buildLocomotiveButtonBar() {
        return ButtonBarFactory.buildCenteredBar(addLocomotiveButton,
                removeLocomotiveButton);
    }

    private Component buildGroupButtonBar() {
        return ButtonBarFactory.buildCenteredBar(addGroupButton,
                editGroupButton, removeGroupButton);
    }

    private Component buildMainButtonBar() {
        return ButtonBarFactory.buildRightAlignedBar(okButton);
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    @Override
    public void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> updatedLocomotiveGroups) {
        locomotiveGroups.addAll(updatedLocomotiveGroups);

    }

    @Override
    public void locomotiveAdded(final Locomotive locomotive) {
        if (locomotive.getGroup().equals(locomotiveGroupModel.getSelection())) {
            locomotives.add(locomotive);
        }
    }

    @Override
    public void locomotiveUpdated(final Locomotive locomotive) {
        if (locomotive.getGroup().equals(locomotiveGroupModel.getSelection())) {
            locomotives.remove(locomotive);
            locomotives.add(locomotive);
        }
    }

    @Override
    public void locomotiveRemoved(final Locomotive locomotive) {
        if (disableListener) {
            return;
        }
        if (locomotive.getGroup().equals(locomotiveGroupModel.getSelection())) {
            locomotives.remove(locomotive);
        }
    }

    @Override
    public void locomotiveGroupAdded(final LocomotiveGroup group) {
        locomotiveGroups.add(group);

    }

    @Override
    public void locomotiveGroupUpdated(final LocomotiveGroup group) {
        locomotiveGroups.remove(group);
        locomotiveGroups.add(group);
    }

    @Override
    public void locomotiveGroupRemoved(final LocomotiveGroup group) {
        if (locomotiveGroupModel.getSelection().equals(group)) {
            locomotives.clear();
        }
        locomotiveGroups.remove(group);
    }

    @Override
    public void failure(
            final ManagerException locomotiveManagerException) {

    }

    /**
     * Sets the selected group as bean in the details model.
     */
    private final class LocomotiveGroupSelectionHandler implements
            ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (locomotiveGroupList.getSelectedIndex() == -1) {
                locomotiveGroupList.setSelectedIndex(0);
            }
            final LocomotiveGroup selectedGroup = (LocomotiveGroup) locomotiveGroupList
                    .getSelectedValue();
            if (selectedGroup == null) {
                return;
            }

            locomotives.clear();
            locomotives.addAll(selectedGroup.getLocomotives());

            locomotiveModel.setList(locomotives);
            locomotiveGroupConfig.setLocomotiveGroup(selectedGroup);
            tca.adjustColumns();
        }
    }

    private class AddLocomotiveGroupAction extends AbstractAction {

        public AddLocomotiveGroupAction() {
            super("Add", ImageTools.createImageIconFromIconSet("list-add.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final String newGroupName = JOptionPane.showInputDialog(
                    LocomotiveConfigurationDialog.this,
                    "Enter the name of the new Locomotive-Group",
                    "Add Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
            final LocomotiveGroup newGroup = new LocomotiveGroup("",
                    newGroupName);
            locomotiveManager.addLocomotiveGroup(newGroup);
            locomotiveGroupConfig.setLocomotiveGroup(null);
        }
    }

    private class EditLocomotiveGroupAction extends AbstractAction {

        public EditLocomotiveGroupAction() {

            super("Edit Group", ImageTools
                    .createImageIconFromIconSet("edit.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final LocomotiveGroup groupToEdit = locomotiveGroupModel
                    .getSelection();

            locomotiveManager.updateLocomotiveGroup(groupToEdit);

        }
    }

    private class RemoveLocomotiveGroupAction extends AbstractAction {

        public RemoveLocomotiveGroupAction() {
            super("Remove", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final LocomotiveGroup groupToDelete = (LocomotiveGroup) (locomotiveGroupList
                    .getSelectedValue());
            final int response = JOptionPane.showConfirmDialog(
                    LocomotiveConfigurationDialog.this,
                    "Really remove Locomotive-Group '"
                            + groupToDelete.getName() + "' ?",
                    "Remove Locomotive-Group", JOptionPane.YES_NO_OPTION
            );
            if (response == JOptionPane.YES_OPTION) {
                try {
                    locomotiveManager.removeLocomotiveGroup(groupToDelete);
                    locomotiveGroupConfig.setLocomotiveGroup(null);
                } catch (final ManagerException e) {
                    ctx.getMainApp().handleException(e);
                }
            }
        }
    }

    private class AddLocomotiveAction extends AbstractAction {

        public AddLocomotiveAction() {
            super("Add", ImageTools.createImageIconFromIconSet("list-add.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final LocomotiveGroup selectedLocomotiveGroup = locomotiveGroupModel
                    .getSelection();
            if (selectedLocomotiveGroup == null) {
                JOptionPane.showMessageDialog(
                        LocomotiveConfigurationDialog.this,
                        "Please select a locomotive group", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Locomotive newLocomotive = createDefaultLocomotive();

            new LocomotiveConfig(ctx, LocomotiveConfigurationDialog.this,
                    newLocomotive, selectedLocomotiveGroup);
        }

        private Locomotive createDefaultLocomotive() {
            final Locomotive newLocomotive = new Locomotive();
            newLocomotive.setType(LocomotiveType.DIGITAL);

            newLocomotive.setBus(1);
            newLocomotive
                    .setFunctions(LocomotiveFunction.getDigitalFunctions());
            return newLocomotive;
        }
    }

    private class EditLocomotiveAction extends AbstractAction {

        public EditLocomotiveAction() {
            super("Edit");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final LocomotiveGroup selectedLocomotiveGroup = locomotiveGroupModel
                    .getSelection();
            final int selectedRow = locomotivesTable.getSelectedRow();
            new LocomotiveConfig(ctx, LocomotiveConfigurationDialog.this,
                    locomotiveModel.getElementAt(selectedRow),
                    selectedLocomotiveGroup);
            final List<Locomotive> locomotives = new ArrayList<Locomotive>(
                    selectedLocomotiveGroup.getLocomotives());
            locomotiveModel.setList(locomotives);
        }
    }

    private class RemoveLocomotiveAction extends AbstractAction {

        public RemoveLocomotiveAction() {
            super("Remove", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final LocomotiveGroup selectedLocomotiveGroup = locomotiveGroupModel
                    .getSelection();

            final int[] rows = locomotivesTable.getSelectedRows();

            if (rows.length == 0) {
                JOptionPane.showMessageDialog(
                        LocomotiveConfigurationDialog.this,
                        "Please select a locomotive", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            final int response = JOptionPane.showConfirmDialog(
                    LocomotiveConfigurationDialog.this,
                    "Really remove Locomotive(s) ?", "Remove Locomotive",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {

                disableListener = true;
                final Set<Locomotive> locomotivesToRemove = new HashSet<Locomotive>();
                for (final int row : rows) {
                    locomotivesToRemove.add(locomotiveModel.getElementAt(row));
                }
                for (final Locomotive locomotive : locomotivesToRemove) {

                    locomotiveManager.removeLocomotiveFromGroup(locomotive,
                            selectedLocomotiveGroup);
                    locomotives.remove(locomotive);

                }
                disableListener = false;
                locomotivesTable.clearSelection();
            }
        }
    }

    private class LocomotiveImageTableCellRenderer extends JLabel implements TableCellRenderer {

        public LocomotiveImageTableCellRenderer() {

        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setIcon(null);
            if (value == null) {
                return this;
            }
            Locomotive locomotive = locomotiveModel.getElementAt(row);
            String image = (String) value;
            setIcon(LocomotiveImageHelper.getLocomotiveIcon(locomotive, 120));

            return this;

        }
    }
}
