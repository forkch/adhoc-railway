package ch.fork.adhocrailway.ui.locomotives.configuration;

import ch.fork.adhocrailway.model.locomotives.LocomotiveGroup;
import ch.fork.adhocrailway.technical.configuration.Preferences;
import ch.fork.adhocrailway.technical.configuration.PreferencesKeys;
import ch.fork.adhocrailway.ui.utils.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Used to renders LocomotiveGroups in JLists and JComboBoxes. If the combo box
 * selection is null, an empty text <code>""</code> is rendered.
 */
public class LocomotiveGroupListCellRenderer extends JLabel implements
        ListCellRenderer<LocomotiveGroup> {

    private final boolean tabletMode;

    public LocomotiveGroupListCellRenderer() {
        this.tabletMode = Preferences.getInstance().getBooleanValue(PreferencesKeys.TABLET_MODE);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends LocomotiveGroup> list, LocomotiveGroup group, int index, boolean isSelected, boolean cellHasFocus) {
        setText(group == null ? "" : group.getName());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (tabletMode) {
            setPreferredSize(new Dimension(getWidth(), UIConstants.SIZE_TABLET));
        }
        return this;
    }
}
