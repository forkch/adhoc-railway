package ch.fork.RailControl.ui.switches.configurationtable;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.fork.RailControl.domain.switches.Switch.SwitchState;
import ch.fork.RailControl.ui.ImageTools;
import ch.fork.RailControl.ui.switches.SwitchConfigurationDialog;

public class SwitchDefaultStateCellRenderer implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel iconLabel = new JLabel();
		if (value.equals(SwitchState.STRAIGHT)) {
			iconLabel.setIcon(ImageTools.createStraightState(iconLabel,
					SwitchConfigurationDialog.class));
		} else {
			iconLabel.setIcon(ImageTools.createCurvedState(iconLabel,
					SwitchConfigurationDialog.class));
		}
		iconLabel.setPreferredSize(new Dimension(120, 40));
		return iconLabel;
	}
}
