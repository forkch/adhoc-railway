package ch.fork.RailControl.ui.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchChangeListener;
import ch.fork.RailControl.domain.switches.SwitchControl;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.ui.ExceptionProcessor;
import ch.fork.RailControl.ui.switches.configuration.SwitchConfig;

public class SwitchWidget extends JPanel implements SwitchChangeListener {

    private static final long serialVersionUID = 1L;

    private Switch mySwitch;

    private SwitchCanvas switchState;

    private JLabel switchStateLabel;

    private JLabel numberLabel;

    private JLabel descLabel;

    private SwitchGroup switchGroup;

    private Map<Integer, Switch> switchNumberToSwitch;

    public SwitchWidget(Switch aSwitch, SwitchGroup switchGroup,
        Map<Integer, Switch> switchNumberToSwitch) {
        mySwitch = aSwitch;
        this.switchGroup = switchGroup;
        this.switchNumberToSwitch = switchNumberToSwitch;
        initGUI();
    }

    private void initGUI() {
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(layout);

        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        numberLabel = new JLabel(Integer.toString(mySwitch.getNumber()));
        numberLabel.setFont(new Font("Dialog", Font.BOLD, 40));
        layout.setConstraints(numberLabel, gbc);
        add(numberLabel);

        gbc.gridx = 1;
        descLabel = new JLabel(mySwitch.getDesc());
        layout.setConstraints(descLabel, gbc);
        add(descLabel);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        String icon = getSwitchIcon();

        switchStateLabel = new JLabel();
        switchStateLabel.setIcon(createImageIcon(icon, "", this));
        layout.setConstraints(switchStateLabel, gbc);
        add(switchStateLabel);
        switchStateLabel.addMouseListener(new MouseAction());
        addMouseListener(new MouseAction());
    }

    private String getSwitchIcon() {
        String icon = "icons/";
        if (mySwitch instanceof DoubleCrossSwitch) {
            icon += "double_cross_switch";
        } else if (mySwitch instanceof DefaultSwitch) {
            icon += "default_switch";
        } else if (mySwitch instanceof ThreeWaySwitch) {
            icon += "three_way_switch";
        }
        switch (mySwitch.getSwitchState()) {
        case STRAIGHT:
            icon += "_straight";
            break;
        case LEFT:
            if (mySwitch instanceof ThreeWaySwitch) {
                icon += "_left";
            } else {
                icon += "_curved";
            }
            break;
        case RIGHT:
            if (mySwitch instanceof ThreeWaySwitch) {
                icon += "_right";
            } else {
                icon += "_curved";
            }
            break;
        case UNDEF:
            icon += "_undef";
            break;
        }
        switch (mySwitch.getSwitchOrientation()) {
        case NORTH:
            icon += "_north";
            break;
        case EAST:
            icon += "_east";
            break;
        case SOUTH:
            icon += "_south";
            break;
        case WEST:
            icon += "_west";
            break;
        }
        icon += ".png";
        return icon;
    }

    public void switchChanged(Switch changedSwitch) {
        if (mySwitch.equals(changedSwitch)) {
            SwingUtilities.invokeLater(new SwitchWidgetUpdater());
        }
    }

    private class SwitchWidgetUpdater implements Runnable {

        public void run() {
            numberLabel.setText(Integer.toString(mySwitch.getNumber()));
            descLabel.setText(mySwitch.getDesc());
            String icon = getSwitchIcon();
            switchStateLabel.setIcon(createImageIcon(icon, "", this));
        }

    }

    private class MouseAction extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            try {
                if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON1) {

                    SwitchControl.getInstance().toggle(mySwitch);
                    SwitchWidget.this.revalidate();
                    SwitchWidget.this.repaint();
                } else if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON3) {
                    SwitchConfig switchConf = new SwitchConfig(null,
                        mySwitch);
                    if (switchConf.isOkPressed()) {

                        switchGroup.removeSwitch(mySwitch);
                        switchNumberToSwitch.remove(mySwitch.getNumber());

                        Switch newSwitch = switchConf.getSwitch();
                        switchNumberToSwitch.put(
                            newSwitch.getNumber(), newSwitch);
                        switchGroup.addSwitch(newSwitch);

                        newSwitch.setSession(mySwitch.getSession());

                        mySwitch = newSwitch;

                        if (mySwitch instanceof DoubleCrossSwitch) {
                            switchState = new DoubleCrossSwitchCanvas(
                                mySwitch);
                        } else if (mySwitch instanceof DefaultSwitch) {
                            switchState = new DefaultSwitchCanvas(mySwitch);
                        } else if (mySwitch instanceof ThreeWaySwitch) {
                            switchState = new ThreeWaySwitchCanvas(
                                mySwitch);
                        }

                        switchState.repaint();
                        switchState.addMouseListener(new MouseAction());
                        switchStateLabel.removeAll();
                        switchStateLabel.add(switchState);
                        switchChanged(mySwitch);
                    }
                }
            } catch (SwitchException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
        }
    }
}
