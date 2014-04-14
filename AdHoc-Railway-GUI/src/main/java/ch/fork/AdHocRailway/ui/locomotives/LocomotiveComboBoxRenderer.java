package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;

import javax.swing.*;
import java.awt.*;

public class LocomotiveComboBoxRenderer extends JPanel implements
        ListCellRenderer<Locomotive> {

    private final JLabel textLabel;
    private final JLabel iconLabel;

    public LocomotiveComboBoxRenderer() {
        setOpaque(true);
        setLayout(new BorderLayout(3, 3));
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        textLabel = new JLabel();
        iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setVerticalAlignment(JLabel.CENTER);

        add(textLabel, BorderLayout.NORTH);
        add(iconLabel, BorderLayout.CENTER);

    }

    @Override
    public Component getListCellRendererComponent(
            final JList<? extends Locomotive> list,
            final Locomotive locomotive, final int index,
            final boolean isSelected, final boolean cellHasFocus) {

        textLabel.setText("");
        iconLabel.setIcon(null);
        if (locomotive == null) {
            return this;
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        // Set the icon and text. If icon was null, say so.
        textLabel.setText(locomotive.getName());
        iconLabel.setIcon(LocomotiveImageHelper.getLocomotiveIcon(locomotive, 120));

        return this;

    }

}
