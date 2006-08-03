
package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchTypeCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        if (value.equals("DefaultSwitch")) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/default_switch_small.png", "Default Switch",
                SwitchWidget.class));
        } else if (value.equals("DoubleCrossSwitch")) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/double_cross_switch_small.png", "Double Cross Switch",
                SwitchWidget.class));
        } else if (value.equals("ThreeWaySwitch")) {
            iconLabel.setIcon(ImageTools.createImageIcon(
                "icons/three_way_switch_small.png", "Threeway Switch",
                SwitchWidget.class));
        }
        return iconLabel;
    }
}