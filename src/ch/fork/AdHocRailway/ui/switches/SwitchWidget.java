
package ch.fork.AdHocRailway.ui.switches;

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

import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchChangeListener;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.switches.canvas.DefaultSwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.DoubleCrossSwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.SwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.canvas.ThreeWaySwitchCanvas;
import ch.fork.AdHocRailway.ui.switches.configuration.SwitchConfig;

public class SwitchWidget extends JPanel implements SwitchChangeListener {
    private static final long  serialVersionUID = 1L;
    private Switch             mySwitch;
    private JLabel             switchStateLabel;
    private JLabel             numberLabel;
    private JLabel             descLabel;
    private SwitchGroup        switchGroup;
    private SwitchCanvas       switchCanvas;
    private GridBagLayout      switchWidgetLayout;
    private GridBagConstraints switchWidgetConstraints;
    private boolean            horizontal;

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
            numberLabel = new JLabel(Integer.toString(mySwitch.getNumber()));
            numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
            switchWidgetLayout.setConstraints(numberLabel,
                switchWidgetConstraints);
            add(numberLabel);
            switchWidgetConstraints.gridx = 1;
            descLabel = new JLabel(mySwitch.getDesc());
            switchWidgetLayout.setConstraints(descLabel,
                switchWidgetConstraints);
            add(descLabel);
            switchWidgetConstraints.gridx = 0;
            switchWidgetConstraints.gridy = 1;
            switchWidgetConstraints.gridwidth = 2;
            switchWidgetLayout.setConstraints(switchCanvas,
                switchWidgetConstraints);
            add(switchCanvas);
        } else {
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setLayout(new FlowLayout());
            descLabel = new JLabel();
            numberLabel = new JLabel(Integer.toString(mySwitch.getNumber()));
            numberLabel.setFont(new Font("Dialog", Font.BOLD, 20));
            add(numberLabel);
            add(switchCanvas);
        }
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
                        SwitchControl.getInstance().unregisterSwitch(mySwitch);
                        SwitchControl.getInstance().registerSwitch(newSwitch);
                        mySwitch = newSwitch;
                        remove(switchCanvas);
                        switchWidgetConstraints.gridx = 0;
                        switchWidgetConstraints.gridy = 1;
                        switchWidgetConstraints.gridwidth = 2;
                        switchCanvas = null;
                        if (mySwitch instanceof DoubleCrossSwitch) {
                            switchCanvas = new DoubleCrossSwitchCanvas(mySwitch);
                        } else if (mySwitch instanceof DefaultSwitch) {
                            switchCanvas = new DefaultSwitchCanvas(mySwitch);
                        } else if (mySwitch instanceof ThreeWaySwitch) {
                            switchCanvas = new ThreeWaySwitchCanvas(mySwitch);
                        }
                        switchWidgetLayout.setConstraints(switchCanvas,
                            switchWidgetConstraints);
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