package ch.fork.RailControl.ui.switches.configuration;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import ch.fork.RailControl.domain.switches.Address;

public class SwitchAddressCellEditor extends AbstractCellEditor implements
    TableCellEditor {

    private JTextField addressTextField1, addressTextField2;

    public SwitchAddressCellEditor() {
        addressTextField1 = new JTextField(3);
        addressTextField2 = new JTextField(3);
    }

    public Component getTableCellEditorComponent(JTable table,
        Object value, boolean isSelected, int row, int column) {

        Address addr = (Address) value;
        addressTextField1.setText(Integer.toString(addr.getAddress1()));
        addressTextField2.setText(Integer.toString(addr.getAddress2()));
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(addressTextField1);
        panel.add(addressTextField2);
        return panel;

    }

    public Object getCellEditorValue() {
        return new Address(Integer.parseInt(addressTextField1.getText()),
            Integer.parseInt(addressTextField2.getText()));
    }
}
