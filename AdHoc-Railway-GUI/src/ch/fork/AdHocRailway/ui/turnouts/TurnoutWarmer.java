/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SwitchProgrammer.java 153 2008-03-27 17:44:48Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.ui.turnouts;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ConfigurationDialog;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.TutorialUtils;
import de.dermoba.srcp.client.SRCPSession;

public class TurnoutWarmer extends ConfigurationDialog {
	private SRCPSession session;
	private JSpinner turnoutNumberField;
	private JToggleButton warmButton;
	private TurnoutPersistenceIface turnoutPersistence;
	private TurnoutControlIface turnoutControl;
	private TurnoutWarmupThread t;

	public TurnoutWarmer(JFrame owner, SRCPSession session) {
		super(owner, "Switch Programmer");
		this.session = session;
		initGUI();
	}

	private void initGUI() {
		turnoutPersistence = AdHocRailway.getInstance().getTurnoutPersistence();
		turnoutControl = AdHocRailway.getInstance().getTurnoutControl();
		JPanel mainPanel = new JPanel(new FlowLayout());

		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 1000, 1);
		turnoutNumberField = new JSpinner(spinnerModel);
		warmButton = new JToggleButton(new WarmupAction());
		mainPanel.add(turnoutNumberField);
		mainPanel.add(warmButton);
		addMainComponent(mainPanel);

		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (t != null)
					t.stopWarmup();
			}

		});
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (t != null)
					t.stopWarmup();
			}

		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				if (t != null)
					t.stopWarmup();
			}
			
		});

		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
	}

	class WarmupAction extends AbstractAction {

		public WarmupAction() {
			super("Start");
		}

		public void actionPerformed(ActionEvent e) {
			if (warmButton.isSelected()) {
				warmButton.setText("Stop");
				t = new TurnoutWarmupThread();
				t.start();

			} else {
				t.stopWarmup();
				warmButton.setText("Start");
			}
		}
	}

	private class TurnoutWarmupThread extends Thread {

		boolean enabled = true;

		public void stopWarmup() {
			enabled = false;
		}

		public void run() {
			try {

				while (enabled) {
					System.out.println(enabled);

					Turnout turnout = turnoutPersistence
							.getTurnoutByNumber((Integer) turnoutNumberField
									.getValue());
					turnoutControl.toggle(turnout);
					Thread.sleep(Preferences.getInstance().getIntValue(
							PreferencesKeys.ROUTING_DELAY));
				}
				System.out.println("DONE");
			} catch (TurnoutException e1) {
				ExceptionProcessor.getInstance().processException(e1);
				return;
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
		}
	}
}
