package ch.fork.adhocrailway.ui.power;

import ch.fork.adhocrailway.controllers.PowerChangeListener;
import ch.fork.adhocrailway.controllers.PowerController;
import ch.fork.adhocrailway.model.power.Booster;
import ch.fork.adhocrailway.model.power.BoosterState;
import ch.fork.adhocrailway.model.power.PowerSupply;
import ch.fork.adhocrailway.technical.configuration.KeyBoardLayout;
import ch.fork.adhocrailway.technical.configuration.Preferences;
import ch.fork.adhocrailway.technical.configuration.PreferencesKeys;
import ch.fork.adhocrailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.adhocrailway.ui.context.PowerContext;
import ch.fork.adhocrailway.ui.utils.ImageTools;
import ch.fork.adhocrailway.ui.utils.UIConstants;
import ch.fork.adhocrailway.ui.widgets.SimpleInternalFrame;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class PowerControlPanel extends JPanel implements PowerChangeListener {

    public static final Color GREEN = Color.decode("#5dec5b");
    public static final Color RED = Color.decode("#ec5353");
    public static final Color ORANGE = Color.decode("#eccb52");
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerControlPanel.class);
    private final ImageIcon stopIcon;
    private final ImageIcon goIcon;
    private final ImageIcon shortcutIcon;
    private final Map<Integer, JToggleButton> numberToPowerToggleButtons = new HashMap<Integer, JToggleButton>();
    private final Map<Integer, ActionListener> numberToActionListener = new HashMap<Integer, ActionListener>();
    private final Map<JToggleButton, Integer> powerToggleButtonsToNumber = new HashMap<JToggleButton, Integer>();
    private final PowerContext ctx;
    private int numberOfBoosters;
    private JPanel powerControlPanel;
    private JButton allBoostersOn;
    private JButton allBoostersOff;
    private JLabel brainResetWarning;
    private JPanel brainResetPanel;
    private JPanel brainStatusPanel;
    private JLabel brainStatus;

    public PowerControlPanel(final PowerContext ctx) {
        super();
        this.ctx = ctx;
        final EventBus mainBus = ctx.getMainBus();
        mainBus.register(this);
        stopIcon = ImageTools.createImageIconFromIconSet("stop_22.png");
        goIcon = ImageTools.createImageIconFromIconSet("go_22.png");
        shortcutIcon = ImageTools.createImageIconFromIconSet("shortcut_22.png");
        initGUI();
    }

    private void initGUI() {
        powerControlPanel = new JPanel(new MigLayout("insets 5, wrap 2"));
        brainResetPanel = new JPanel(new BorderLayout(2, 2));
        brainResetPanel.setBackground(Color.decode("#26a69a"));
        brainResetWarning = new JLabel();
        brainResetWarning.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        brainResetWarning.setText("");
        brainResetPanel.add(brainResetWarning, BorderLayout.CENTER);

        brainStatusPanel = new JPanel(new BorderLayout(2, 2));
        brainStatusPanel.setBackground(GREEN);
        brainStatus = new JLabel();
        brainStatus.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        brainStatus.setText("");
        brainStatusPanel.add(brainStatus, BorderLayout.CENTER);

        initKeyboardActions();
        update();

        final SimpleInternalFrame frame = new SimpleInternalFrame("Boosters");
        frame.add(powerControlPanel, BorderLayout.CENTER);
        frame.add(brainResetPanel, BorderLayout.SOUTH);
        frame.add(brainStatusPanel, BorderLayout.NORTH);
        setLayout(new BorderLayout());
        add(frame, BorderLayout.CENTER);

    }

    @Subscribe
    public void connectedToRailwayDevice(final ConnectedToRailwayEvent event) {
        final boolean connected = event.isConnected();
        final PowerController powerController = ctx.getPowerControl();
        if (connected) {
            powerController.addPowerChangeListener(this);
            allBoostersOn.setEnabled(true);
            allBoostersOff.setEnabled(true);
        } else {
            powerController.removePowerChangeListener(this);
            allBoostersOn.setEnabled(false);
            allBoostersOff.setEnabled(false);
        }
        for (final JToggleButton toggleButton : numberToPowerToggleButtons
                .values()) {
            toggleButton.setEnabled(connected);
        }
        brainResetPanel.setBackground(GREEN);

    }

    public void update() {

        numberOfBoosters = Preferences.getInstance().getIntValue(
                PreferencesKeys.NUMBER_OF_BOOSTERS);
        powerControlPanel.removeAll();

        allBoostersOn = new JButton("All On", goIcon);
        allBoostersOff = new JButton("All Off", stopIcon);
        allBoostersOff.setFocusable(false);
        allBoostersOn.setFocusable(false);
        allBoostersOn.addActionListener(new AllBoostersOnAction());
        allBoostersOff.addActionListener(new AllBoostersOffAction());

        String params = "height 30, growx";
        if (Preferences.getInstance().getBooleanValue(PreferencesKeys.TABLET_MODE)) {
            params = "height " + UIConstants.SIZE_TABLET + ", growx";
        }
        powerControlPanel.add(allBoostersOn, params);
        powerControlPanel.add(allBoostersOff, params + ", wrap");

        for (int i = 0; i < numberOfBoosters; i++) {

            final JToggleButton boosterButton = new JToggleButton(
                    "F" + (i + 1), stopIcon);
            boosterButton.setHorizontalAlignment(SwingConstants.LEADING);
            final ToggleBoosterAction action = new ToggleBoosterAction();
            boosterButton.addActionListener(action);
            powerControlPanel.add(boosterButton, params);

            numberToPowerToggleButtons.put(i, boosterButton);
            numberToActionListener.put(i, action);
            powerToggleButtonsToNumber.put(boosterButton, i);
            boosterButton.setFocusable(false);
        }
        initKeyboardActions();
    }

    private void initKeyboardActions() {

        final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        final KeyBoardLayout kbl = Preferences.getInstance()
                .getKeyBoardLayout();

        for (int i = 0; i < numberOfBoosters; i++) {
            getActionMap().put("ToggleBooster" + i,
                    new ToggleBoosterKeyAction(i));
            kbl.assignKeys(inputMap, "ToggleBooster" + i);
        }

        ctx.getMainApp().registerEscapeKey(new AllBoostersOffAction());
    }

    private void toogleBooster(final int boosterNumber, final boolean on) {

        final PowerSupply bus1Supply = getPowerSupply();

        if (bus1Supply == null) {
            return;
        }
        final PowerController powerController = ctx.getPowerControl();
        powerController.toggleBooster(bus1Supply.getBooster(boosterNumber));
    }

    private PowerSupply getPowerSupply() {
        final PowerController powerController = ctx.getPowerControl();
        return powerController.getPowerSupply(1);
    }

    @Override
    public void powerChanged(final PowerSupply supply) {
        int activeBoosterCount = 0;
        for (final Booster booster : supply.getBoosters()) {
            final int boosterNumber = booster.getBoosterNumber();
            final BoosterState bs = booster.getState();
            if (boosterNumber != -1) {
                final JToggleButton button = numberToPowerToggleButtons
                        .get(boosterNumber);

                if (button == null) {
                    return;
                }
                button.removeActionListener(numberToActionListener
                        .get(boosterNumber));

                if (bs.equals(BoosterState.ACTIVE)) {
                    button.setSelected(true);
                    button.setIcon(goIcon);
                    activeBoosterCount++;
                } else {
                    button.setSelected(false);
                    if (bs.equals(BoosterState.SHORTCUT)) {
                        button.setIcon(shortcutIcon);
                        activeBoosterCount++;
                    } else {
                        button.setIcon(stopIcon);
                    }
                }
                button.addActionListener(numberToActionListener
                        .get(boosterNumber));
            }
        }
        ctx.setActiveBoosterCount(activeBoosterCount);
    }

    @Override
    public void reset(String resetMessage) {
        brainResetPanel.setBackground(RED);
        brainResetWarning.setText(resetMessage);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                brainResetPanel.setBackground(GREEN);
                brainResetWarning.setText("");
            }
        });

    }

    @Override
    public void message(String message) {
        if(message.contains("Brain OK")) {
            brainStatusPanel.setBackground(GREEN);
        } else {
            brainStatusPanel.setBackground(ORANGE);
        }
        brainStatus.setText(message);
    }

    class AllBoostersOnAction extends AbstractAction {

        public AllBoostersOnAction() {
            super();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final PowerController powerController = ctx.getPowerControl();
            final PowerSupply powerSupply = powerController.getPowerSupply(1);
            powerController.powerOn(powerSupply);

        }
    }

    class AllBoostersOffAction extends AbstractAction {

        public AllBoostersOffAction() {
            super();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            final PowerController powerController = ctx.getPowerControl();
            final PowerSupply powerSupply = powerController.getPowerSupply(1);
            powerController.powerOff(powerSupply);
        }
    }

    class ToggleBoosterAction extends AbstractAction {

        public ToggleBoosterAction() {
            super();
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {
            final JToggleButton source = (JToggleButton) arg0.getSource();

            final int boosterNumber = powerToggleButtonsToNumber.get(source);

            toogleBooster(boosterNumber, source.isSelected());
        }
    }

    class ToggleBoosterKeyAction extends AbstractAction {

        private final int boosterNumber;

        public ToggleBoosterKeyAction(final int number) {
            super();
            this.boosterNumber = number;
        }

        @Override
        public void actionPerformed(final ActionEvent arg0) {

            final JToggleButton source = numberToPowerToggleButtons
                    .get(boosterNumber);
            toogleBooster(boosterNumber, !source.isSelected());
        }
    }
}
