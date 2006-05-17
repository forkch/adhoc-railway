package ch.fork.RailControl.ui.switches.configuration;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ch.fork.RailControl.domain.switches.Switch.SwitchState;
import ch.fork.RailControl.ui.ImageTools;
import ch.fork.RailControl.ui.switches.SwitchWidget;

public class SwitchDefaultStateComboBoxCellRenderer implements
    ListCellRenderer {

    public Component getListCellRendererComponent(JList list,
        Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(150, 38));
        if (value.equals(SwitchState.STRAIGHT)) {
            iconLabel.setIcon(ImageTools.createStraightState(
                iconLabel, SwitchWidget.class));
        } else {
            iconLabel.setIcon(ImageTools.createCurvedState(
                iconLabel, SwitchWidget.class));
        }
        return iconLabel;

    }

}
