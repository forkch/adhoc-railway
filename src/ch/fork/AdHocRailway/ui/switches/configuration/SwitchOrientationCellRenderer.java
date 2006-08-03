
package ch.fork.AdHocRailway.ui.switches.configuration;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.domain.switches.Switch.SwitchState;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.switches.SwitchWidget;

public class SwitchOrientationCellRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel iconLabel = new JLabel();
        if (value.equals(SwitchState.STRAIGHT)) {
            iconLabel.setIcon(ImageTools.createStraightState(iconLabel,
                SwitchWidget.class));
        } else {
            iconLabel.setIcon(ImageTools.createCurvedState(iconLabel,
                SwitchWidget.class));
        }
        return iconLabel;
    }
}
