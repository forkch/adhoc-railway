package ch.fork.AdHocRailway.ui.power;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.SimpleInternalFrame;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.power.SRCPPowerControl;
import de.dermoba.srcp.model.power.SRCPPowerState;
import de.dermoba.srcp.model.power.SRCPPowerSupply;
import de.dermoba.srcp.model.power.SRCPPowerSupplyChangeListener;
import de.dermoba.srcp.model.power.SRCPPowerSupplyException;

public class PowerControlPanel extends JPanel implements
		SRCPPowerSupplyChangeListener {

	private int numberOfBoosters;
	private ImageIcon stopIcon;
	private ImageIcon goIcon;
	private ImageIcon shortcutIcon;
	private SRCPPowerControl powerControl = SRCPPowerControl.getInstance();
	private Map<Integer, JToggleButton> numberToPowerToggleButtons = new HashMap<Integer, JToggleButton>();
	private Map<Integer, ActionListener> numberToActionListener = new HashMap<Integer, ActionListener>();
	private Map<JToggleButton, Integer> powerToggleButtonsToNumber = new HashMap<JToggleButton, Integer>();
	private JPanel powerControlPanel;
	private JButton allBoostersOn;
	private JButton allBoostersOff;

	public PowerControlPanel() {
		super();

		stopIcon = ImageTools.createImageIconFromIconSet("stop_32.png");
		goIcon = ImageTools.createImageIconFromIconSet("go_32.png");
		shortcutIcon = ImageTools.createImageIconFromIconSet("shortcut_32.png");

		powerControl.addPowerSupplyChangeListener(this);
		initGUI();
	}

	private void initGUI() {

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// BetterFlowLayout layout = new BetterFlowLayout();
		MigLayout layout = new MigLayout("wrap 2");
		powerControlPanel = new JPanel(layout);

		update();

		SimpleInternalFrame frame = new SimpleInternalFrame("Power");
		frame.add(powerControlPanel, BorderLayout.CENTER);
		add(frame, BorderLayout.CENTER);

	}

	public void setConnected(boolean b) {
		if (b) {
			allBoostersOn.setEnabled(true);
			allBoostersOff.setEnabled(true);
		} else {
			allBoostersOn.setEnabled(false);
			allBoostersOff.setEnabled(false);
		}
		for (JToggleButton toggleButton : numberToPowerToggleButtons.values()) {
			toggleButton.setEnabled(b);
		}
	}

	@Override
	public void powerSupplyChanged(SRCPPowerSupply powerSupply, String freeText) {
		if (freeText == null || freeText.isEmpty()) {

		} else {
			if (freeText.toUpperCase().contains("AUTO"))
				return;
			int boosterNumber = -1;
			StringTokenizer tokenizer = new StringTokenizer(freeText);
			if (tokenizer.hasMoreTokens()) {

				String t = tokenizer.nextToken().trim();

				try {
					boosterNumber = Integer.parseInt(t);
				} catch (NumberFormatException x) {
				}

			}
			boolean shortcut = false;
			if (tokenizer.hasMoreTokens()) {

				String t = tokenizer.nextToken().trim();
				if (t.toUpperCase().equals("SHORTCUT")) {
					shortcut = true;
				}
			}

			if (boosterNumber != -1) {
				JToggleButton button = numberToPowerToggleButtons
						.get(boosterNumber);

				if (button == null)
					return;
				button.removeActionListener(numberToActionListener
						.get(boosterNumber));

				if (powerSupply.getState().equals(SRCPPowerState.ON)) {
					button.setSelected(true);
					button.setIcon(goIcon);
				} else {
					button.setSelected(false);
					if (shortcut) {
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
				Preferences.NUMBER_OF_BOOSTERS);
		powerControlPanel.removeAll();

		allBoostersOn = new JButton("All Boosters On", goIcon);
		allBoostersOff = new JButton("All Boosters Off", stopIcon);

		allBoostersOn.addActionListener(new AllBoostersOnAction());
		allBoostersOff.addActionListener(new AllBoostersOffAction());

		powerControlPanel.add(allBoostersOn, "");
		powerControlPanel.add(allBoostersOff, "");

		for (int i = 0; i < numberOfBoosters; i++) {

			JToggleButton boosterButton = new JToggleButton("Booster "
					+ (i + 1), stopIcon);
			ToggleBoosterAction action = new ToggleBoosterAction();
			boosterButton.addActionListener(action);
			powerControlPanel.add(boosterButton, "grow");

			numberToPowerToggleButtons.put(i, boosterButton);
			numberToActionListener.put(i, action);
			powerToggleButtonsToNumber.put(boosterButton, i);
		}
	}

	class AllBoostersOnAction extends AbstractAction {
		public AllBoostersOnAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			try {
				powerControl.getInstance().setAllStates(SRCPPowerState.ON);
			} catch (SRCPPowerSupplyException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			} catch (SRCPModelException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}

		}
	}

	class AllBoostersOffAction extends AbstractAction {
		public AllBoostersOffAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			try {
				powerControl.getInstance().setAllStates(SRCPPowerState.OFF);
			} catch (SRCPPowerSupplyException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			} catch (SRCPModelException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}

		}
	}

	class ToggleBoosterAction extends AbstractAction {
		public ToggleBoosterAction() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JToggleButton source = (JToggleButton) arg0.getSource();

			Set<SRCPPowerSupply> powerSupplies = powerControl
					.getKnownPowerSupplies();
			System.out.println(powerSupplies);
			SRCPPowerSupply bus1Supply = null;
			for (SRCPPowerSupply supply : powerSupplies) {
				if (supply.getBus() == 1) {
					bus1Supply = supply;
				}
			}

			int boosterNumber = powerToggleButtonsToNumber.get(source);
			if (bus1Supply == null) {
				return;
			}
			try {
				if (source.isSelected()) {

					powerControl.setState(bus1Supply, SRCPPowerState.ON, ""
							+ boosterNumber);

					source.setIcon(goIcon);
				} else {
					powerControl.setState(bus1Supply, SRCPPowerState.OFF, ""
							+ boosterNumber);
					source.setIcon(stopIcon);
				}
			} catch (SRCPPowerSupplyException e) {
				ExceptionProcessor.getInstance().processException(e);
			} catch (SRCPModelException e) {
				ExceptionProcessor.getInstance().processException(e);
			}
		}
	}
}
