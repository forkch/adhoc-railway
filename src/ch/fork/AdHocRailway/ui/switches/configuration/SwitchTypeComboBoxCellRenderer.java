package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchTypeComboBoxCellRenderer implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list,
        Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(56, 38));
        if (value.equals("DefaultSwitch")) {
            iconLabel.setIcon(ImageTools.createDefaultSwitch(
                iconLabel, SwitchWidget.class));
        } else if (value.equals("DoubleCrossSwitch")) {
            iconLabel.setIcon(ImageTools.createDoubleCrossSwitch(
                iconLabel, SwitchWidget.class));
        } else if (value.equals("ThreeWaySwitch")) {
            iconLabel.setIcon(ImageTools.createThreeWaySwitch(
                iconLabel, SwitchWidget.class));
        }
        return iconLabel;

    }
}
