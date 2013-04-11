package ch.fork.AdHocRailway.ui.power;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.technical.configuration.KeyBoardLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.SimpleInternalFrame;
import ch.fork.AdHocRailway.ui.context.PowerContext;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.power.SRCPPowerControl;
import de.dermoba.srcp.model.power.SRCPPowerState;
import de.dermoba.srcp.model.power.SRCPPowerSupply;
import de.dermoba.srcp.model.power.SRCPPowerSupplyChangeListener;
import de.dermoba.srcp.model.power.SRCPPowerSupplyException;

public class PowerControlPanel extends JPanel implements
		SRCPPowerSupplyChangeListener {

	private static final Logger LOGGER = Logger
			.getLogger(PowerControlPanel.class);
	private static final long serialVersionUID = -4402814993315460803L;
	private int numberOfBoosters;
	private final ImageIcon stopIcon;
	private final ImageIcon goIcon;
	private final ImageIcon shortcutIcon;
	private final SRCPPowerControl powerControl = SRCPPowerControl
			.getInstance();
	private final Map<Integer, JToggleButton> numberToPowerToggleButtons = new HashMap<Integer, JToggleButton>();
	private final Map<Integer, ActionListener> numberToActionListener = new HashMap<Integer, ActionListener>();
	private final Map<JToggleButton, Integer> powerToggleButtonsToNumber = new HashMap<JToggleButton, Integer>();
	private JPanel powerControlPanel;
	private JButton allBoostersOn;
	private JButton allBoostersOff;
	private final PowerContext ctx;

	public PowerControlPanel(final PowerContext ctx) {
		super();
		this.ctx = ctx;

		stopIcon = ImageTools.createImageIconFromIconSet("stop_22.png");
		goIcon = ImageTools.createImageIconFromIconSet("go_22.png");
		shortcutIcon = ImageTools.createImageIconFromIconSet("shortcut_22.png");

		powerControl.addPowerSupplyChangeListener(this);
		initGUI();
		setConnected(false);
	}

	private void initGUI() {
		final MigLayout layout = new MigLayout("wrap 2");
		powerControlPanel = new JPanel(layout);

		initKeyboardActions();
		update();

		final SimpleInternalFrame frame = new SimpleInternalFrame("Boosters");
		frame.add(powerControlPanel, BorderLayout.CENTER);
		setLayout(new BorderLayout());
		add(frame, BorderLayout.CENTER);

	}

	public void setConnected(final boolean b) {
		if (b) {
			allBoostersOn.setEnabled(true);
			allBoostersOff.setEnabled(true);
		} else {
			allBoostersOn.setEnabled(false);
			allBoostersOff.setEnabled(false);
		}
		for (final JToggleButton toggleButton : numberToPowerToggleButtons
				.values()) {
			toggleButton.setEnabled(b);
		}
	}

	@Override
	public void powerSupplyChanged(final SRCPPowerSupply powerSupply,
			final String freeText) {
		if (freeText == null || freeText.isEmpty()) {
			return;
		}
		if (freeText.toUpperCase().contains("AUTO")) {
			return;
		}
		LOGGER.info("Power freeText: " + freeText);

		final Map<Integer, BoosterState> boosterStates = new HashMap<Integer, BoosterState>();
		final StringTokenizer tokenizer = new StringTokenizer(freeText);
		while (tokenizer.hasMoreTokens()) {
			int boosterNumber = -1;
			if (tokenizer.hasMoreTokens()) {

				final String t = tokenizer.nextToken().trim();

				try {
					boosterNumber = Integer.parseInt(t);
				} catch (final NumberFormatException x) {
				}

			}
			if (tokenizer.hasMoreTokens()) {

				final String srcpState = tokenizer.nextToken().trim();
				if (BoosterState.isActive(srcpState)) {
					boosterStates.put(boosterNumber, BoosterState.ACTIVE);
				} else if (BoosterState.isShortcut(srcpState)) {
					boosterStates.put(boosterNumber, BoosterState.SHORTCUT);
				} else if (BoosterState.isInActive(srcpState)) {
					boosterStates.put(boosterNumber, BoosterState.INACTIVE);
				}
			}
		}

		for (final Entry<Integer, BoosterState> state : boosterStates
				.entrySet()) {
			final int boosterNumber = state.getKey();
			final BoosterState bs = state.getValue();
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
				} else {
					button.setSelected(false);
					if (bs.equals(BoosterState.SHORTCUT)) {
						button.setIcon(shortcutIcon);
					} else {
						button.setIcon(stopIcon);
					}
				}
				button.addActionListener(numberToActionListener
						.get(boosterNumber));
			}
		}

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

		powerControlPanel.add(allBoostersOn, "growx");
		powerControlPanel.add(allBoostersOff, "growx, wrap 15");

		for (int i = 0; i < numberOfBoosters; i++) {

			final JToggleButton boosterButton = new JToggleButton(
					"F" + (i + 1), stopIcon);
			boosterButton.setHorizontalAlignment(SwingConstants.LEADING);
			final ToggleBoosterAction action = new ToggleBoosterAction();
			boosterButton.addActionListener(action);
			powerControlPanel.add(boosterButton, "growx");

			numberToPowerToggleButtons.put(i, boosterButton);
			numberToActionListener.put(i, action);
			powerToggleButtonsToNumber.put(boosterButton, i);
			boosterButton.setFocusable(false);
		}
		initKeyboardActions();
	}

	private void initKeyboardActions() {

		final InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		// final KeyStroke[] keys = inputMap.keys();
		// if (keys != null) {
		// for (final KeyStroke ks : keys) {
		// getInputMap().remove(ks);
		// }
		// }

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

		final JToggleButton source = numberToPowerToggleButtons
				.get(boosterNumber);

		final Set<SRCPPowerSupply> powerSupplies = powerControl
				.getKnownPowerSupplies();
		SRCPPowerSupply bus1Supply = null;
		for (final SRCPPowerSupply supply : powerSupplies) {
			if (supply.getBus() == 1) {
				bus1Supply = supply;
			}
		}

		if (bus1Supply == null) {
			return;
		}
		try {
			if (on) {

				powerControl.setState(bus1Supply, SRCPPowerState.ON, ""
						+ boosterNumber);

				source.setIcon(goIcon);
			} else {
				powerControl.setState(bus1Supply, SRCPPowerState.OFF, ""
						+ boosterNumber);
				source.setIcon(stopIcon);
			}
		} catch (final SRCPPowerSupplyException e) {
			ExceptionProcessor.getInstance().processException(e);
		} catch (final SRCPModelException e) {
			ExceptionProcessor.getInstance().processException(e);
		}
	}

	class AllBoostersOnAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8642776345384105494L;

		public AllBoostersOnAction() {
			super();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				SRCPPowerControl.getInstance().setAllStates(SRCPPowerState.ON);
			} catch (final SRCPPowerSupplyException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			} catch (final SRCPModelException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}

		}
	}

	class AllBoostersOffAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5158494831586154924L;

		public AllBoostersOffAction() {
			super();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			try {
				powerControl.setAllStates(SRCPPowerState.OFF);
			} catch (final SRCPPowerSupplyException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			} catch (final SRCPModelException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}

		}
	}

	class ToggleBoosterAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1717254973356674480L;

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

		/**
		 * 
		 */
		private static final long serialVersionUID = -7583549489780694040L;
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
