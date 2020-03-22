package ch.fork.adhocrailway.ui.locomotives;

import ch.fork.adhocrailway.model.locomotives.Locomotive;

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


    }

    @Override
    public Component getListCellRendererComponent(
            final JList<? extends Locomotive> list,
            final Locomotive locomotive, final int index,
            final boolean isSelected, final boolean cellHasFocus) {
        removeAll();
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

        textLabel.setText(locomotive.getName());

        if (index != -1) {
            iconLabel.setIcon(LocomotiveImageHelper.getLocomotiveIconScaledToWidth(locomotive, 100));

            add(textLabel, BorderLayout.NORTH);
            add(iconLabel, BorderLayout.CENTER);
        } else {

            add(textLabel, BorderLayout.CENTER);
        }

        return this;

    }

}
