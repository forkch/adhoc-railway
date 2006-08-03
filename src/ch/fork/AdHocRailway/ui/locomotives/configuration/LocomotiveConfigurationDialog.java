
package ch.fork.AdHocRailway.ui.locomotives.configuration;

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
import java.util.Collection;
import java.util.List;
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

import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.NoneLocomotive;
import ch.fork.AdHocRailway.ui.ListListModel;
import ch.fork.AdHocRailway.ui.TableResizer;

public class LocomotiveConfigurationDialog extends JDialog {
    private Preferences           preferences;
    private Frame                 owner;
    private boolean               okPressed     = false;
    private boolean               cancelPressed = false;
    private LocomotiveControl     locomotiveControl;
    private TableModel            locomotiveTableModel;
    private List<LocomotiveGroup> locomotiveGroupsWorkCopy;
    private TableModel            locomotivesTableModel;
    private JTable                locomotivesTable;
    private JPopupMenu            switchGroupPopupMenu;
    private JList                 locomotiveGroupList;
    private JPanel                locomotivesPanel;
    private ListListModel         locomotiveGroupListModel;
    private SortedSet<Locomotive> locomotivesWorkCopy;

    public LocomotiveConfigurationDialog(Frame owner, Preferences preferences) {
        super(owner, "Locomotive Configuration", true);
        this.owner = owner;
        this.preferences = preferences;
        this.locomotiveControl = LocomotiveControl.getInstance();
        this.locomotivesWorkCopy = new TreeSet<Locomotive>();
        for (Locomotive l : locomotiveControl.getLocomotives()) {
            Locomotive clone = l.clone();
            this.locomotivesWorkCopy.add(clone);
        }
        this.locomotiveGroupsWorkCopy = new ArrayList<LocomotiveGroup>();
        for (LocomotiveGroup lg : locomotiveControl.getLocomotiveGroups()) {
            LocomotiveGroup clone = lg.clone();
            this.locomotiveGroupsWorkCopy.add(clone);
            for (Locomotive l : lg.getLocomotives()) {
                clone.addLocomotive(l);
            }
        }
        initGUI();
    }

    private void initGUI() {
        JPanel locomotiveGroupPanel = createLocomotiveGroupPanel();
        add(locomotiveGroupPanel, BorderLayout.WEST);
        JPanel locomotivesPanel = createLocomotivesPanel();
        add(locomotivesPanel, BorderLayout.CENTER);
        updateLocomotivesPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                LocomotiveConfigurationDialog.this.setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelPressed = false;
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed = true;
                LocomotiveConfigurationDialog.this.setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    private JPanel createLocomotiveGroupPanel() {
        JPanel locomotiveGroupPanel = new JPanel(new BorderLayout());
        TitledBorder title = BorderFactory
            .createTitledBorder("Locomotive-Groups");
        locomotiveGroupPanel.setBorder(title);
        locomotiveGroupPanel.getInsets(new Insets(5, 5, 5, 5));
        locomotiveGroupListModel = new ListListModel<LocomotiveGroup>(
            locomotiveGroupsWorkCopy);
        locomotiveGroupList = new JList(locomotiveGroupListModel);
        locomotiveGroupList
            .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                    switchGroupPopupMenu.show(e.getComponent(), e.getX(), e
                        .getY());
                }
            }
        });
        addItem.addActionListener(new AddLocomotiveGroupAction());
        addSwitchGroupButton.addActionListener(new AddLocomotiveGroupAction());
        removeItem.addActionListener(new RemoveLocomotiveGroupAction());
        removeSwitchGroupButton
            .addActionListener(new RemoveLocomotiveGroupAction());
        renameItem.addActionListener(new RenameLocomotiveGroupAction());
        moveUpItem.addActionListener(new MoveLocomotiveGroupAction(true));
        moveDownItem.addActionListener(new MoveLocomotiveGroupAction(false));
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
        locomotivesTableModel = new LocomotiveTableModel(locomotivesWorkCopy);
        locomotivesTable = new JTable(locomotivesTableModel);
        JScrollPane switchGroupTablePane = new JScrollPane(locomotivesTable);
        switchGroupTablePane.setPreferredSize(new Dimension(600, 400));
        locomotivesPanel.add(switchGroupTablePane, BorderLayout.CENTER);

        JComboBox locomotiveTypeCombobox = new JComboBox();
        locomotiveTypeCombobox.addItem("DeltaLocomotive");
        locomotiveTypeCombobox.addItem("DigitalLocomotive");
        TableColumn locomotiveTypeColumn = locomotivesTable.getColumnModel()
            .getColumn(1);
        locomotiveTypeColumn.setCellEditor(new DefaultCellEditor(
            locomotiveTypeCombobox));
        JButton addLocomotiveButton = new JButton("Add");
        JButton removeLocomotiveButton = new JButton("Remove");
        addLocomotiveButton.addActionListener(new AddLocomotiveAction());
        removeLocomotiveButton.addActionListener(new RemoveLocomotiveAction());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addLocomotiveButton);
        buttonPanel.add(removeLocomotiveButton);
        locomotivesPanel.add(buttonPanel, BorderLayout.SOUTH);
        return locomotivesPanel;
    }

    private void updateLocomotivesPanel() {
        LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) (locomotiveGroupList
            .getSelectedValue());
        if (selectedLocomotiveGroup == null) {
            ((TitledBorder) locomotivesPanel.getBorder())
                .setTitle("Locomotive-Group");
        } else {
            ((TitledBorder) locomotivesPanel.getBorder())
                .setTitle("Locomotive-Group '"
                    + selectedLocomotiveGroup.getName() + "'");
        }
        ((LocomotiveTableModel) locomotivesTableModel)
            .setLocomotiveGroup(selectedLocomotiveGroup);
        TableResizer.adjustColumnWidths(locomotivesTable, 30);
        if (locomotivesTable.getRowCount() > 0) {
            TableResizer.adjustRowHeight(locomotivesTable);
        }
        locomotivesTable.revalidate();
        locomotivesTable.repaint();
        pack();
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public Collection<Locomotive> getLocomotives() {
        return locomotivesWorkCopy;
    }

    public Collection<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroupsWorkCopy;
    }

    class AddLocomotiveGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent arg0) {
            String newGroupName = JOptionPane.showInputDialog(
                LocomotiveConfigurationDialog.this,
                "Enter the name of the new Locomotive-Group",
                "Add Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
            LocomotiveGroup newSection = new LocomotiveGroup(newGroupName);
            locomotiveGroupsWorkCopy.add(newSection);
            locomotiveGroupListModel.updated();
            locomotiveGroupList.setSelectedValue(newSection, true);
        }
    }
    class RemoveLocomotiveGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent arg0) {
            LocomotiveGroup groupToDelete = (LocomotiveGroup) (locomotiveGroupList
                .getSelectedValue());
            int response = JOptionPane.showConfirmDialog(
                LocomotiveConfigurationDialog.this,
                "Really remove Locomotive-Group '" + groupToDelete.getName()
                    + "' ?", "Remove Locomotive-Group",
                JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                locomotiveGroupsWorkCopy.remove(groupToDelete);
                locomotiveGroupListModel.updated();
            }
        }
    }
    class RenameLocomotiveGroupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            LocomotiveGroup groupToRename = (LocomotiveGroup) (locomotiveGroupList
                .getSelectedValue());
            String newSectionName = JOptionPane.showInputDialog(
                LocomotiveConfigurationDialog.this, "Enter new name",
                "Rename Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
            if (!newSectionName.equals("")) {
                groupToRename.setName(newSectionName);
                locomotiveGroupListModel.updated();
            }
        }
    }
    class AddLocomotiveAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) (locomotiveGroupList
                .getSelectedValue());
            Locomotive newLocomotive = new NoneLocomotive();

            LocomotiveConfig locomotiveConfig = new LocomotiveConfig(
                LocomotiveConfigurationDialog.this, newLocomotive);
            if (locomotiveConfig.isOkPressed()) {

                locomotivesWorkCopy.add(locomotiveConfig.getLocomotive());
                selectedLocomotiveGroup.addLocomotive(locomotiveConfig
                    .getLocomotive());
            }
            newLocomotive = null;
            updateLocomotivesPanel();

        }
    }
    class RemoveLocomotiveAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (locomotivesTable.isEditing())
                locomotivesTable.getCellEditor().stopCellEditing();
            LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) (locomotiveGroupList
                .getSelectedValue());
            Locomotive locomotiveToRemove = ((LocomotiveTableModel) locomotivesTableModel)
                .getLocomotiveAt(locomotivesTable.getSelectedRow());
            selectedLocomotiveGroup.removeLocomotive(locomotiveToRemove);
            locomotivesWorkCopy.remove(locomotiveToRemove);
            updateLocomotivesPanel();
        }
    }
    class MoveLocomotiveGroupAction extends AbstractAction {
        private boolean up;

        public MoveLocomotiveGroupAction(boolean up) {
            this.up = up;
        }

        public void actionPerformed(ActionEvent e) {
            LocomotiveGroup groupToMove = (LocomotiveGroup) (locomotiveGroupList
                .getSelectedValue());
            int oldIndex = locomotiveGroupsWorkCopy.indexOf(groupToMove);
            int newIndex = oldIndex;
            if (up) {
                if (oldIndex != 0) {
                    newIndex = oldIndex - 1;
                } else {
                    return;
                }
            } else {
                if (oldIndex != locomotiveGroupsWorkCopy.size() - 1) {
                    newIndex = oldIndex + 1;
                } else {
                    return;
                }
            }
            locomotiveGroupsWorkCopy.remove(oldIndex);
            locomotiveGroupsWorkCopy.add(newIndex, groupToMove);
            locomotiveGroupList.setSelectedIndex(newIndex);
            locomotiveGroupListModel.updated();
            updateLocomotivesPanel();
        }
    }
}
