package ch.fork.AdHocRailway.ui.routes;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.fork.AdHocRailway.ui.ImageTools;

public class RouteStateRenderer implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		boolean enabled = ((Boolean) value).booleanValue();

		JLabel label = new JLabel();

		if (enabled) {
			label.setIcon(ImageTools.createImageIcon("icons/route_start.png",
					"Enabled", this));
		} else {
			label.setIcon(ImageTools.createImageIcon("icons/route_stop.png",
					"Disabled", this));
		}

		return label;
	}
}
