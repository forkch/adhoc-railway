package ch.fork.RailControl.ui.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import ch.fork.RailControl.domain.switches.Switch;

public class DefaultSwitchCanvas extends SwitchCanvas {

	public DefaultSwitchCanvas(Switch mySwitch) {
		super(mySwitch);
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g3 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g3.drawImage(createImageIcon("icons/default_switch.png", "",
				this).getImage(), 0, 0, this);

		switch (mySwitch.getSwitchState()) {
		case STRAIGHT:
			g3.drawImage(createImageIcon("icons/LED_middle_yellow.png", "",
					this).getImage(), 28, 0, this);
			g3.drawImage(createImageIcon("icons/LED_up_white.png", "",
					this).getImage(), 28, 0, this);
			break;
		case LEFT:
		case RIGHT:
			g3.drawImage(createImageIcon("icons/LED_up_yellow.png", "",
					this).getImage(), 28, 0, this);
			g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
					this).getImage(), 28, 0, this);
			break;
		case UNDEF:
			g3.drawImage(createImageIcon("icons/LED_up_white.png", "",
					this).getImage(), 28, 0, this);
			g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
					this).getImage(), 28, 0, this);
			break;
		}
		g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
				this).getImage(), 0, 0, this);
		
		rotate(g2, img);
	}
}
