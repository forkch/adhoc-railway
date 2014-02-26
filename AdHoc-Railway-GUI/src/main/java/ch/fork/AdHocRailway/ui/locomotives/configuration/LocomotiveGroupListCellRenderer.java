package ch.fork.AdHocRailway.ui.locomotives.configuration;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;

import javax.swing.*;
import java.awt.*;

/**
 * Used to renders LocomotiveGroups in JLists and JComboBoxes. If the combo box
 * selection is null, an empty text <code>""</code> is rendered.
 */
public class LocomotiveGroupListCellRenderer extends DefaultListCellRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 8121914367054500476L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);

        LocomotiveGroup group = (LocomotiveGroup) value;
        setText(group == null ? "" : group.getName());
        return component;
    }
}