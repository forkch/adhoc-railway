package ch.fork.adhocrailway.ui.turnouts.configuration;

import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;

import javax.swing.*;
import java.awt.*;

/**
 * Used to renders TurnoutGroups in JLists and JComboBoxes. If the combo box
 * selection is null, an empty text <code>""</code> is rendered.
 */
public class TurnoutGroupListCellRenderer extends DefaultListCellRenderer {


    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);

        TurnoutGroup group = (TurnoutGroup) value;
        setText(group == null ? "" : (" " + group.getName()));
        return component;
    }
}
