package ch.fork.RailControl.ui.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

import ch.fork.RailControl.domain.switches.Switch;

public class Test implements ImageObserver {
    public ImageIcon getImage(Switch mySwitch) {
        BufferedImage img = new BufferedImage(56, 35,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g3 = img.createGraphics();

        g3.drawImage(createImageIcon("icons/default_switch.png", "", this)
            .getImage(), 0, 0, this);

        switch (mySwitch.getSwitchState()) {
        case STRAIGHT:
            g3.drawImage(
                createImageIcon("icons/LED_middle_yellow.png", "", this)
                    .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("icons/LED_up_white.png", "", this)
                    .getImage(), 28, 0, this);
            break;
        case LEFT:
        case RIGHT:
            g3.drawImage(
                createImageIcon("icons/LED_up_yellow.png", "", this)
                    .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("icons/LED_middle_white.png", "", this)
                    .getImage(), 28, 0, this);
            break;
        case UNDEF:
            g3.drawImage(
                createImageIcon("icons/LED_up_white.png", "", this)
                    .getImage(), 28, 0, this);
            g3.drawImage(
                createImageIcon("icons/LED_middle_white.png", "", this)
                    .getImage(), 28, 0, this);
            break;
        }
        g3
            .drawImage(
                createImageIcon("icons/LED_middle_white.png", "", this)
                    .getImage(), 0, 0, this);
        
        BufferedImage img2 = new BufferedImage(100, 100,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = img.createGraphics();

        AffineTransform at = null;
        switch (mySwitch.getSwitchOrientation()) {
        case NORTH:
            at = AffineTransform.getRotateInstance(
                Math.PI / 2 * 3, (56 + 1) / 2, (56 + 1) / 2);
            break;
        case EAST:
            at = AffineTransform.getRotateInstance(0, 0, 0);
            break;
        case SOUTH:
            at = AffineTransform.getRotateInstance(
                Math.PI / 2, (56 + 1) / 2, (56 + 1) / 2);
            break;
        case WEST:
            at = AffineTransform.getRotateInstance(
                Math.PI, (56 + 1) / 2, (56 + 1) / 2);

            break;
        }
        g2.drawImage(img, 0, 0, this);
        return new ImageIcon(img2);
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y,
        int width, int height) {
        // TODO Auto-generated method stub
        return false;
    }
}
