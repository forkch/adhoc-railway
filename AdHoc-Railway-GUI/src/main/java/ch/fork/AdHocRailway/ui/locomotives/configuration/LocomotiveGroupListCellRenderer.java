package ch.fork.AdHocRailway.ui.locomotives.configuration;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;

import javax.swing.*;
import java.awt.*;

/**
 * Used to renders LocomotiveGroups in JLists and JComboBoxes. If the combo box
 * selection is null, an empty text <code>""</code> is rendered.
 */
public class LocomotiveGroupListCellRenderer extends JLabel implements
        ListCellRenderer<LocomotiveGroup> {

    @Override
    public Component getListCellRendererComponent(JList<? extends LocomotiveGroup> list, LocomotiveGroup group, int index, boolean isSelected, boolean cellHasFocus) {
        setText(group == null ? "" : group.getName());
        return this;
    }
}