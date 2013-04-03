package ch.fork.AdHocRailway.ui.locomotives;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.ui.ImageTools;

public class LocomotiveComboBoxRenderer extends JPanel implements
		ListCellRenderer<Locomotive> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4561005713259042914L;
	private final JLabel textLabel;
	private final JLabel iconLabel;

	public LocomotiveComboBoxRenderer() {
		setOpaque(true);
		setLayout(new BorderLayout(3, 3));
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		textLabel = new JLabel();
		iconLabel = new JLabel();
		iconLabel.setHorizontalAlignment(JLabel.CENTER);

		add(textLabel, BorderLayout.NORTH);
		add(iconLabel, BorderLayout.SOUTH);

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
		// setLocomotiveImage(locomotive);
		textLabel.setText(locomotive.getName());
		iconLabel.setIcon(ImageTools.getLocomotiveIcon(locomotive, 120));

		return this;

	}

}
