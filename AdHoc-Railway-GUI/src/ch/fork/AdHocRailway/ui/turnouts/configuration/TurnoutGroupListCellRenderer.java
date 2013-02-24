package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

/**
 * Used to renders TurnoutGroups in JLists and JComboBoxes. If the combo box
 * selection is null, an empty text <code>""</code> is rendered.
 */
public class TurnoutGroupListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Component component = super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);

		TurnoutGroup group = (TurnoutGroup) value;
		setText(group == null ? "" : (" " + group.getName()));
		return component;
	}
}