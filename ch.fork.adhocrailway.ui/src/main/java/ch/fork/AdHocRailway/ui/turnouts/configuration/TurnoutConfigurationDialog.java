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

import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.TurnoutManagerListener;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.utils.GlobalKeyShortcutHelper;
import ch.fork.AdHocRailway.ui.utils.ImageTools;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.utils.TableColumnAdjuster;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.common.collect.ArrayListModel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class TurnoutConfigurationDialog extends JDialog implements
        TurnoutManagerListener {


    private final TurnoutManager turnoutManager;
    private final TurnoutContext ctx;
    private boolean okPressed;
    private JList<?> turnoutGroupList;
    private JTable turnoutsTable;
    private JButton addGroupButton;
    private JButton removeGroupButton;
    private JButton editGroupButton;
    private SelectionInList<TurnoutGroup> turnoutGroupModel;
    private JButton addTurnoutButton;
    private JButton removeTurnoutButton;
    private JButton okButton;
    private TurnoutGroupConfigPanel turnoutGroupConfig;
    private com.jgoodies.common.collect.ArrayListModel<TurnoutGroup> turnoutGroups;
    private com.jgoodies.common.collect.ArrayListModel<Turnout> turnouts;
    private SelectionInList<Turnout> turnoutModel;
    private JScrollPane groupScrollPane;
    private JScrollPane turnoutTableScrollPane;
    private TableColumnAdjuster tca;

    public TurnoutConfigurationDialog(final JFrame owner,
                                      final TurnoutContext ctx) {
        super(owner, "Turnout Configuration", true);
        this.ctx = ctx;
        turnoutManager = ctx.getTurnoutManager();
        initGUI();
    }

    private void initGUI() {
        initComponents();
        buildPanel();
        initEventHandling();
        initShortcuts();
        turnoutManager.addTurnoutManagerListener(this);
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void initShortcuts() {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, new AddTurnoutGroupAction());
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK, new AddTurnoutAction());
    }

    private void initComponents() {
        turnoutGroups = new ArrayListModel<TurnoutGroup>();

        turnoutGroupModel = new SelectionInList<TurnoutGroup>(
                (ListModel<?>) turnoutGroups);

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

        groupScrollPane = new JScrollPane(turnoutGroupList);

        turnoutGroupConfig = new TurnoutGroupConfigPanel();

        addGroupButton = new JButton(new AddTurnoutGroupAction());
        editGroupButton = new JButton(new EditTurnoutGroupAction());
        removeGroupButton = new JButton(new RemoveTurnoutGroupAction());

        turnouts = new ArrayListModel<Turnout>();
        turnoutModel = new SelectionInList<Turnout>();

        turnoutModel.setList(turnouts);
        turnoutsTable = new JTable() {

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        turnoutsTable.setModel(new TurnoutTableModel(turnoutModel));

        turnoutsTable
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        turnoutsTable.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTurnout");
        turnoutsTable.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                "deleteTurnout");
        turnoutsTable.getActionMap().put("deleteTurnout",
                new RemoveTurnoutAction());

        final TableColumn typeColumn = turnoutsTable.getColumnModel()
                .getColumn(1);
        typeColumn.setCellRenderer(new TurnoutTypeCellRenderer());

        final TableColumn defaultStateColumn = turnoutsTable.getColumnModel()
                .getColumn(8);
        defaultStateColumn
                .setCellRenderer(new TurnoutDefaultStateCellRenderer());
        turnoutsTable.setRowHeight(30);

        turnoutTableScrollPane = new JScrollPane(turnoutsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        turnoutsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tca = new TableColumnAdjuster(turnoutsTable, 10);
        addTurnoutButton = new JButton(new AddTurnoutAction());
        removeTurnoutButton = new JButton(new RemoveTurnoutAction());

        okButton = new JButton("OK",
                ImageTools.createImageIconFromIconSet("dialog-ok-apply.png"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                okPressed = true;
                setVisible(false);
                turnoutManager
                        .removeTurnoutManagerListenerInNextEvent(TurnoutConfigurationDialog.this);
            }
        });
    }

    private void initEventHandling() {
        turnoutGroupList
                .addListSelectionListener(new TurnoutGroupSelectionHandler());
        turnoutsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {
                    new EditTurnoutAction().actionPerformed(null);
                }
            }

        });

        SwingUtils.addEscapeListener(this);

    }

    private void buildPanel() {
        setLayout(new BorderLayout());
        final JPanel mainPanel = new JPanel(new MigLayout("fill",
                "[shrink][shrink][shrink][grow]", "[shrink][grow][shrink]"));

        mainPanel.add(new JLabel("Turnout Groups"));
        mainPanel.add(new JSeparator(), "growx");
        mainPanel.add(new JLabel("Turnouts"), "gap unrelated");
        mainPanel.add(new JSeparator(), "growx, wrap");

        mainPanel.add(groupScrollPane, "grow, span 2");
        mainPanel.add(turnoutTableScrollPane,
                "gap unrelated, grow, span 2 2, w 900, wrap");

        mainPanel.add(turnoutGroupConfig, "shrink, span 2, wrap");

        mainPanel.add(buildGroupButtonBar(), "span 2, align center");
        mainPanel.add(buildTurnoutButtonBar(),
                "gap unrelated, span 2, align center, wrap");
        mainPanel.add(buildMainButtonBar(), "span 4, align right");
        add(mainPanel, BorderLayout.CENTER);
    }

    private Component buildTurnoutButtonBar() {
        return ButtonBarFactory.buildCenteredBar(addTurnoutButton,
                removeTurnoutButton);
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
    public void turnoutsUpdated(
            final SortedSet<TurnoutGroup> updatedTurnoutGroups) {
        this.turnoutGroups.addAll(updatedTurnoutGroups);
    }

    @Override
    public void turnoutUpdated(final Turnout turnout) {
        if (turnout.getTurnoutGroup().equals(turnoutGroupModel.getSelection())) {
            turnouts.remove(turnout);
            turnouts.add(turnout);
        }
    }

    @Override
    public void turnoutRemoved(final Turnout turnout) {
        if (turnout == null) {
            return;
        }
        if (turnout.getTurnoutGroup().equals(turnoutGroupModel.getSelection())) {
            turnouts.remove(turnout);
        }
    }

    @Override
    public void turnoutAdded(final Turnout turnout) {
        if (turnout.getTurnoutGroup().equals(turnoutGroupModel.getSelection())) {
            turnouts.add(turnout);
        }
    }

    @Override
    public void turnoutGroupAdded(final TurnoutGroup group) {
        turnoutGroups.add(group);
        turnoutGroupModel.setSelection(group);
    }

    @Override
    public void turnoutGroupRemoved(final TurnoutGroup group) {
        TurnoutGroup selectedGroup = turnoutGroupModel.getSelection();
        if (selectedGroup != null && selectedGroup.equals(group)) {
            turnouts.clear();
        }
        turnoutGroups.remove(group);

    }

    @Override
    public void turnoutGroupUpdated(final TurnoutGroup group) {
        turnoutGroups.remove(group);
        turnoutGroups.add(group);

    }

    @Override
    public void failure(final AdHocServiceException serviceException) {

    }

    /**
     * Sets the selected group as bean in the details model.
     */
    private final class TurnoutGroupSelectionHandler implements
            ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {

            if (e.getValueIsAdjusting()) {
                return;
            }
            if (turnoutGroupList.getSelectedIndex() == -1) {
                turnoutGroupList.setSelectedIndex(0);
            }
            final TurnoutGroup selectedGroup = (TurnoutGroup) turnoutGroupList
                    .getSelectedValue();

            if (selectedGroup == null) {
                return;
            }

            turnouts.clear();
            turnouts.addAll(selectedGroup.getTurnouts());

            turnoutGroupConfig.setTurnoutGroup(selectedGroup);

            tca.adjustColumns();
        }
    }

    private class EditTurnoutGroupAction extends AbstractAction {

        public EditTurnoutGroupAction() {
            super("Edit Group", ImageTools
                    .createImageIconFromIconSet("edit.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final TurnoutGroup groupToEdit = turnoutGroupModel.getSelection();

            turnoutManager.updateTurnoutGroup(groupToEdit);

        }
    }

    private class AddTurnoutGroupAction extends AbstractAction {

        public AddTurnoutGroupAction() {
            super("Add Group", ImageTools
                    .createImageIconFromIconSet("list-add.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final String newGroupName = JOptionPane.showInputDialog(
                    TurnoutConfigurationDialog.this,
                    "Enter the name of the new Turnout-Group",
                    "Add Turnout-Group", JOptionPane.QUESTION_MESSAGE);
            if (newGroupName == null) {
                return;
            }
            final TurnoutGroup newTurnoutGroup = new TurnoutGroup();
            newTurnoutGroup.setName(newGroupName);

            turnoutManager.addTurnoutGroup(newTurnoutGroup);
            turnoutGroupConfig.setTurnoutGroup(null);
        }
    }

    private class RemoveTurnoutGroupAction extends AbstractAction {

        public RemoveTurnoutGroupAction() {
            super("Remove Group", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final TurnoutGroup groupToDelete = turnoutGroupModel.getSelection();
            if (groupToDelete == null) {
                JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
                        "Please select a Turnout-Group", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final int response = JOptionPane.showConfirmDialog(
                    TurnoutConfigurationDialog.this,
                    "Really remove Turnout-Group '" + groupToDelete.getName()
                            + "' ?", "Remove Turnout-Group",
                    JOptionPane.YES_NO_OPTION
            );
            if (response == JOptionPane.YES_OPTION) {
                turnoutManager.removeTurnoutGroup(groupToDelete);
                turnoutGroupConfig.setTurnoutGroup(null);
            }
        }
    }

    private class AddTurnoutAction extends AbstractAction {

        public AddTurnoutAction() {
            super("Add", ImageTools.createImageIconFromIconSet("list-add.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final TurnoutGroup selectedTurnoutGroup = turnoutGroupModel
                    .getSelection();
            if (selectedTurnoutGroup == null) {
                JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
                        "Please select a locomotive group", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Turnout newTurnout = TurnoutHelper.createDefaultTurnout(
                    turnoutManager);

            new TurnoutConfig(TurnoutConfigurationDialog.this, ctx, newTurnout,
                    selectedTurnoutGroup, true);
        }

    }

    private class RemoveTurnoutAction extends AbstractAction {

        public RemoveTurnoutAction() {
            super("Remove", ImageTools
                    .createImageIconFromIconSet("list-remove.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final int[] rows = turnoutsTable.getSelectedRows();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
                        "Please select a turnout", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            final int response = JOptionPane.showConfirmDialog(
                    TurnoutConfigurationDialog.this,
                    "Really remove turnouts(s) ?", "Remove turnout(s)",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                final Set<Turnout> turnoutsToRemove = new HashSet<Turnout>();
                for (final int row : rows) {

                    turnoutsToRemove.add(turnoutModel.getElementAt(row));
                }
                for (final Turnout turnout : turnoutsToRemove) {
                    turnoutManager.removeTurnout(turnout);
                }
                turnoutsTable.clearSelection();
            }
        }
    }

    private class EditTurnoutAction extends AbstractAction {

        @Override
        public void actionPerformed(final ActionEvent e) {

            final int row = turnoutsTable.getSelectedRow();
            final Turnout turnout = turnoutModel.getElementAt(row);
            new TurnoutConfig(TurnoutConfigurationDialog.this, ctx, turnout,
                    turnout.getTurnoutGroup(), false);
        }
    }

}
