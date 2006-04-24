package ch.fork.RailControl.ui.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import ch.fork.RailControl.domain.switches.Switch;

public class SwitchCanvas extends Canvas {

	protected Switch mySwitch;

	public SwitchCanvas(Switch mySwitch) {
		this.mySwitch = mySwitch;
	}

	protected void rotate(Graphics g, BufferedImage img) {
		Graphics2D g2 = (Graphics2D) g;

		AffineTransform at = null;
		switch (mySwitch.getSwitchOrientation()) {
		case NORTH:
			at = AffineTransform.getRotateInstance(Math.PI / 2 * 3,
					(56 + 1) / 2, (56 + 1) / 2);
			break;
		case EAST:
			at = AffineTransform.getRotateInstance(0, 0, 0);
			break;
		case SOUTH:
			at = AffineTransform.getRotateInstance(Math.PI / 2, (56 + 1) / 2,
					(56 + 1) / 2);
			break;
		case WEST:
			at = AffineTransform.getRotateInstance(Math.PI, (56 + 1) / 2,
					(56 + 1) / 2);

			break;
		}
		g2.drawImage(img, at, this);
	}

	public Dimension getPreferredSize() {
		return new Dimension(56, 56);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

}
