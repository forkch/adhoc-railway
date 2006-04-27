package ch.fork.RailControl.ui.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.fork.RailControl.domain.switches.Switch;

public class DoubleCrossSwitchCanvas extends SwitchCanvas {

	public DoubleCrossSwitchCanvas(Switch mySwitch) {
		super(mySwitch);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		BufferedImage img = new BufferedImage(56, 35,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g3 = img.createGraphics();
		g.drawImage(createImageIcon("icons/double_cross_switch.png", "", this)
				.getImage(), 0, 0, this);
		switch (mySwitch.getSwitchState()) {
			case STRAIGHT :
				g3.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 0, 17, this);
				g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 0, 0, this);
				g3.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 28, 0, this);
				g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, this);
				break;
			case RIGHT :
			case LEFT:
				g3.drawImage(createImageIcon("icons/LED_middle_yellow.png", "",
						this).getImage(), 0, 0, this);
				g3.drawImage(createImageIcon("icons/LED_up_white.png", "", this)
						.getImage(), 0, 17, this);
				g3.drawImage(
						createImageIcon("icons/LED_up_yellow.png", "", this)
								.getImage(), 28, 0, this);
				g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, this);
				break;
			case UNDEF :
				g3.drawImage(
						createImageIcon("icons/LED_up_white.png", "", this)
								.getImage(), 0, 17, this);
				g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 0, 0, this);
				g3.drawImage(
						createImageIcon("icons/LED_up_white.png", "", this)
								.getImage(), 28, 0, this);
				g3.drawImage(createImageIcon("icons/LED_middle_white.png", "",
						this).getImage(), 28, 0, this);
				break;
		}
		
		rotate(g2, img);
	}

}
