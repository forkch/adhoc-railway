
package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchTypeComboBoxCellRenderer implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(56, 38));
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
