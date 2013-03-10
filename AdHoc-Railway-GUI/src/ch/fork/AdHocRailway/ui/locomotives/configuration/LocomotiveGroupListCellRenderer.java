package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;

/**
 * Used to renders LocomotiveGroups in JLists and JComboBoxes. If the combo box
 * selection is null, an empty text <code>""</code> is rendered.
 */
public class LocomotiveGroupListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Component component = super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);

		LocomotiveGroup group = (LocomotiveGroup) value;
		setText(group == null ? "" : group.getName());
		return component;
	}
}