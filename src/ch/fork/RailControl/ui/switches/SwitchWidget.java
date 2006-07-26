package ch.fork.RailControl.ui.switches;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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

    private JLabel switchStateLabel;

    private JLabel numberLabel;

    private JLabel descLabel;

    private SwitchGroup switchGroup;

    private SwitchCanvas switchCanvas;

    private GridBagLayout switchWidgetLayout;

    private GridBagConstraints switchWidgetConstraints;

    private boolean horizontal;

    public SwitchWidget(Switch aSwitch, SwitchGroup switchGroup) {
        this(aSwitch, switchGroup, false);
    }

    public SwitchWidget(Switch aSwitch, SwitchGroup switchGroup,
        boolean horizontal) {
        mySwitch = aSwitch;
        this.switchGroup = switchGroup;
        this.horizontal = horizontal;
        initGUI();
    }

    private void initGUI() {
        switchCanvas = null;
        if (mySwitch instanceof DoubleCrossSwitch) {
            switchCanvas = new DoubleCrossSwitchCanvas(mySwitch);
        } else if (mySwitch instanceof DefaultSwitch) {
            switchCanvas = new DefaultSwitchCanvas(mySwitch);
        } else if (mySwitch instanceof ThreeWaySwitch) {
            switchCanvas = new ThreeWaySwitchCanvas(mySwitch);
        }

        switchCanvas.addMouseListener(new MouseAction());

        addMouseListener(new MouseAction());

        if (!horizontal) {
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            switchWidgetLayout = new GridBagLayout();
            switchWidgetConstraints = new GridBagConstraints();
            setLayout(switchWidgetLayout);

            switchWidgetConstraints.insets = new Insets(5, 5, 5, 5);

            switchWidgetConstraints.gridx = 0;
            numberLabel = new JLabel(Integer
                .toString(mySwitch.getNumber()));
            numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
            switchWidgetLayout.setConstraints(
                numberLabel, switchWidgetConstraints);
            add(numberLabel);

            switchWidgetConstraints.gridx = 1;
            descLabel = new JLabel(mySwitch.getDesc());
            switchWidgetLayout.setConstraints(
                descLabel, switchWidgetConstraints);
            add(descLabel);

            switchWidgetConstraints.gridx = 0;
            switchWidgetConstraints.gridy = 1;
            switchWidgetConstraints.gridwidth = 2;

            switchWidgetLayout.setConstraints(
                switchCanvas, switchWidgetConstraints);
            add(switchCanvas);
        } else {

            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setLayout(new FlowLayout());

            descLabel = new JLabel();
            numberLabel = new JLabel(Integer
                .toString(mySwitch.getNumber()));
            numberLabel.setFont(new Font("Dialog", Font.BOLD, 20));

            add(numberLabel);

            add(switchCanvas);
        }
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
            SwitchWidget.this.revalidate();
            SwitchWidget.this.repaint();
        }

    }

    private class MouseAction extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            try {
                if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON1) {
                    SwitchControl.getInstance().toggle(mySwitch);
                } else if (e.getClickCount() == 1
                    && e.getButton() == MouseEvent.BUTTON3) {
                    SwitchConfig switchConf = new SwitchConfig(mySwitch);
                    if (switchConf.isOkPressed()) {

                        switchGroup.removeSwitch(mySwitch);
                        Switch newSwitch = switchConf.getSwitch();
                        switchGroup.addSwitch(newSwitch);
                        newSwitch.setSession(mySwitch.getSession());
                        mySwitch = newSwitch;

                        remove(switchCanvas);
                        switchWidgetConstraints.gridx = 0;
                        switchWidgetConstraints.gridy = 1;
                        switchWidgetConstraints.gridwidth = 2;

                        switchCanvas = null;
                        if (mySwitch instanceof DoubleCrossSwitch) {
                            switchCanvas = new DoubleCrossSwitchCanvas(
                                mySwitch);
                        } else if (mySwitch instanceof DefaultSwitch) {
                            switchCanvas = new DefaultSwitchCanvas(
                                mySwitch);
                        } else if (mySwitch instanceof ThreeWaySwitch) {
                            switchCanvas = new ThreeWaySwitchCanvas(
                                mySwitch);
                        }

                        switchWidgetLayout.setConstraints(
                            switchCanvas, switchWidgetConstraints);
                        add(switchCanvas);
                        switchCanvas.addMouseListener(new MouseAction());
                        switchChanged(mySwitch);
                    }
                }
            } catch (SwitchException e1) {
                ExceptionProcessor.getInstance().processException(e1);
            }
        }
    }

    public Switch getMySwitch() {
        return mySwitch;
    }
}
