package ch.fork.AdHocRailway.ui.locomotives;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.ui.ImageTools;

public class LocomotiveComboBoxRenderer extends JLabel implements
		ListCellRenderer<Locomotive> {

	public LocomotiveComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends Locomotive> list, Locomotive locomotive, int index,
			boolean isSelected, boolean cellHasFocus) {

		if (locomotive == null) {
			return this;
		}
		// Get the selected index. (The index param isn't
		// always valid, so just use the value.)

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		// Set the icon and text. If icon was null, say so.
		// setLocomotiveImage(locomotive);
		setText(locomotive.getName());

		return this;

	}

	public void setLocomotiveImage(Locomotive locomotive) {
		ImageIcon icon = ImageTools.getLocomotiveIcon(locomotive);
		setIcon(icon);
	}

}
