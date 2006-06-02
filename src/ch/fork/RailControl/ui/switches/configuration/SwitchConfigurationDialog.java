package ch.fork.RailControl.ui.switches.configuration;

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
import javax.swing.DefaultListModel;
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
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ch.fork.RailControl.domain.configuration.Preferences;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.Switch.SwitchOrientation;
import ch.fork.RailControl.domain.switches.Switch.SwitchState;
import ch.fork.RailControl.ui.TableResizer;

public class SwitchConfigurationDialog extends JDialog {

    private List<SwitchGroup> switchGroups;

    private Map<Integer, Switch> switchNumberToSwitch;

    private Preferences preferences;

    private DefaultListModel switchGroupListModel;

    private JPopupMenu switchGroupPopupMenu;

    private JList switchGroupList;

    private JPanel switchesPanel;

    private TableModel switchesTableModel;

    private JTable switchesTable;

    private Frame owner;

    private boolean cancelPressed = false;

    private boolean okPressed = false;

    public SwitchConfigurationDialog(Frame owner, Preferences preferences,
        Map<Integer, Switch> switchNumberToSwitch,
        List<SwitchGroup> switchGroups) {
        super(owner, "Switch Configuration", true);

        this.owner = owner;
        this.preferences = preferences;

        this.switchNumberToSwitch = new HashMap<Integer, Switch>();
        for (Switch s : switchNumberToSwitch.values()) {
            Switch clone = s.clone();
            this.switchNumberToSwitch.put(clone.getNumber(), clone);
        }
        this.switchGroups = new ArrayList<SwitchGroup>();
        for (SwitchGroup sg : switchGroups) {
            SwitchGroup clone = sg.clone();
            this.switchGroups.add(clone);
            for (Switch s : sg.getSwitches()) {
                clone.addSwitch(this.switchNumberToSwitch.get(s
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
        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    private JPanel createSwitchGroupPanel() {
        JPanel switchGroupPanel = new JPanel(new BorderLayout());
        TitledBorder title = BorderFactory
            .createTitledBorder("Switch Groups");
        switchGroupPanel.setBorder(title);
        switchGroupPanel.getInsets(new Insets(5, 5, 5, 5));

        switchGroupListModel = new DefaultListModel();
        switchGroupList = new JList(switchGroupListModel);
        for (SwitchGroup switchGroup : switchGroups) {
            switchGroupListModel.addElement(switchGroup);
        }

        switchGroupPopupMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add");

        JMenuItem removeItem = new JMenuItem("Remove");
        JMenuItem renameItem = new JMenuItem("Rename");
        switchGroupPopupMenu.add(addItem);
        switchGroupPopupMenu.add(removeItem);
        switchGroupPopupMenu.add(renameItem);

        JButton addSwitchGroupButton = new JButton("Add");
        JButton removeSwitchGroupButton = new JButton("Remove");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addSwitchGroupButton);
        buttonPanel.add(removeSwitchGroupButton);

        switchGroupPanel.add(switchGroupList, BorderLayout.CENTER);
        switchGroupPanel.add(buttonPanel, BorderLayout.SOUTH);

        /* Install ActionListeners */
        switchGroupList
            .addListSelectionListener(new ListSelectionListener() {
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
                    switchGroupPopupMenu.show(
                        e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        addItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addSwitchGroup();
            }
        });

        addSwitchGroupButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addSwitchGroup();
            }

        });

        removeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSwitchGroup();
            }
        });

        removeSwitchGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSwitchGroup();
            }
        });

        renameItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameSwitchGroup();
            }
        });

        return switchGroupPanel;
    }

    private void addSwitchGroup() {
        String newGroupName = JOptionPane.showInputDialog(
            SwitchConfigurationDialog.this,
            "Enter the name of the new Switch-Group", "Add Switch-Group",
            JOptionPane.QUESTION_MESSAGE);
        SwitchGroup newSection = new SwitchGroup(newGroupName);
        switchGroups.add(newSection);
        switchGroupListModel.addElement(newSection);
    }

    private void removeSwitchGroup() {
        SwitchGroup groupToDelete = (SwitchGroup) (switchGroupList
            .getSelectedValue());
        int response = JOptionPane.showConfirmDialog(
            SwitchConfigurationDialog.this, "Really remove Switch-Group '"
                + groupToDelete.getName() + "' ?", "Remove Switch-Group",
            JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            switchGroups.remove(groupToDelete);
            switchGroupListModel.removeElement(groupToDelete);
        }
    }

    private void renameSwitchGroup() {
        SwitchGroup groupToRename = (SwitchGroup) (switchGroupList
            .getSelectedValue());
        String newSectionName = JOptionPane.showInputDialog(
            SwitchConfigurationDialog.this, "Enter new name",
            "Rename Switch-Group", JOptionPane.QUESTION_MESSAGE);
        if (!newSectionName.equals("")) {
            groupToRename.setName(newSectionName);
            switchGroupList.revalidate();
            switchGroupList.repaint();
        }
    }

    private JPanel createSwitchesPanel() {
        switchesPanel = new JPanel(new BorderLayout());
        switchesPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "escapeAction");
        switchesPanel.getActionMap().put(
            "escapeAction", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed = true;
                    SwitchConfigurationDialog.this.setVisible(false);
                }
            });

        switchesPanel.getInsets(new Insets(5, 5, 5, 5));

        TitledBorder title = BorderFactory
            .createTitledBorder("Switch-Group");
        switchesPanel.setBorder(title);

        switchesTableModel = new SwitchesTableModel(switchNumberToSwitch);
        switchesTable = new JTable(switchesTableModel);
        switchesTable.setRowHeight(40);

        // SwitchType
        JComboBox switchTypeComboBox = new JComboBox();
        switchTypeComboBox.addItem("DefaultSwitch");
        switchTypeComboBox.addItem("DoubleCrossSwitch");
        switchTypeComboBox.addItem("ThreeWaySwitch");
        switchTypeComboBox
            .setRenderer(new SwitchTypeComboBoxCellRenderer());

        TableColumn typeColumn = switchesTable.getColumnModel().getColumn(
            1);
        typeColumn
            .setCellEditor(new DefaultCellEditor(switchTypeComboBox));
        typeColumn.setCellRenderer(new SwitchTypeCellRenderer());

        // SwitchAddress
        TableColumn addressColumn = switchesTable.getColumnModel()
            .getColumn(3);
        addressColumn
            .setCellEditor((TableCellEditor) new SwitchAddressCellEditor());

        // DefaultState
        JComboBox switchDefaultStateComboBox = new JComboBox();
        switchDefaultStateComboBox.addItem(SwitchState.STRAIGHT);
        switchDefaultStateComboBox.addItem(SwitchState.LEFT);
        switchDefaultStateComboBox
            .setRenderer(new SwitchDefaultStateComboBoxCellRenderer());

        TableColumn defaultStateColumn = switchesTable.getColumnModel()
            .getColumn(4);
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

        TableColumn switchOrientationColumn = switchesTable
            .getColumnModel().getColumn(5);
        switchOrientationColumn.setCellEditor(new DefaultCellEditor(
            switchOrientationComboBox));

        JScrollPane switchGroupTablePane = new JScrollPane(switchesTable);
        switchGroupTablePane.setPreferredSize(new Dimension(600, 400));
        switchesPanel.add(switchGroupTablePane, BorderLayout.CENTER);

        JButton addSwitchButton = new JButton("Add");
        JButton removeSwitchButton = new JButton("Remove");

        addSwitchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
                    .getSelectedValue());
                SortedSet<Integer> usedNumbers = new TreeSet<Integer>(
                    switchNumberToSwitch.keySet());
                int nextNumber = usedNumbers.last().intValue() + 1;
                Switch newSwitch = new DefaultSwitch(nextNumber,
                    selectedSwitchGroup.getName()
                        + nextNumber);
                selectedSwitchGroup.addSwitch(newSwitch);
                switchNumberToSwitch.put(newSwitch.getNumber(), newSwitch);
                updateSwitchesPanel();
            }
        });

        removeSwitchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (switchesTable.isEditing())
                    switchesTable.getCellEditor().stopCellEditing();

                SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
                    .getSelectedValue());
                Integer number = (Integer) switchesTable.getValueAt(
                    switchesTable.getSelectedRow(), 0);
                selectedSwitchGroup.removeSwitch(switchNumberToSwitch
                    .get(number));
                switchNumberToSwitch.remove(number);
                updateSwitchesPanel();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addSwitchButton);
        buttonPanel.add(removeSwitchButton);

        switchesPanel.add(buttonPanel, BorderLayout.SOUTH);
        return switchesPanel;
    }

    private void updateSwitchesPanel() {
        SwitchGroup selectedSwitchGroup = (SwitchGroup) (switchGroupList
            .getSelectedValue());
        if (selectedSwitchGroup == null) {
            ((TitledBorder) switchesPanel.getBorder())
                .setTitle("Switch-Group");
        } else {
            ((TitledBorder) switchesPanel.getBorder())
                .setTitle("Switch-Group '"
                    + selectedSwitchGroup.getName() + "'");
        }

        ((SwitchesTableModel) switchesTableModel)
            .setSwitchGroup(selectedSwitchGroup);
        TableResizer.adjustColumnWidths(switchesTable, 30);
        if (switchesTable.getRowCount() > 0) {
            TableResizer.adjustRowHeight(switchesTable);
        }
        switchesTable.revalidate();
        switchesTable.repaint();
        switchesPanel.revalidate();
        switchesPanel.repaint();
        pack();
    }

    public List<SwitchGroup> getSwitchGroups() {
        return switchGroups;
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public Map<Integer, Switch> getSwitchNumberToSwitch() {
        return switchNumberToSwitch;
    }

}
