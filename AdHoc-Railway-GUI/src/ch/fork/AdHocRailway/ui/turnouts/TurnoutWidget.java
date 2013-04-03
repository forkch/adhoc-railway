/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutHelper;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;

public class TurnoutWidget extends JPanel implements TurnoutChangeListener {

	private static final long serialVersionUID = 6871966498091781447L;

	private Turnout turnout;

	private JLabel numberLabel;

	private TurnoutCanvas turnoutCanvas;

	private SRCPTurnoutState actualTurnoutState = SRCPTurnoutState.UNDEF;

	private boolean widgetEnabled;

	private final boolean testMode;

	private final boolean forHistory;

	private JPanel statePanel;

	public TurnoutWidget(final Turnout turnout, final boolean forHistory) {
		this(turnout, forHistory, false);
	}

	public TurnoutWidget(final Turnout turnout, final boolean forHistory,
			final boolean testMode) {
		this.turnout = turnout;
		this.forHistory = forHistory;
		this.testMode = testMode;

		widgetEnabled = true;

		initGUI();
		updateTurnout();
		TurnoutHelper.validateTurnout(AdHocRailway.getInstance()
				.getTurnoutPersistence(), turnout, this);
		setEnabled(true);
	}

	private void initGUI() {
		turnoutCanvas = new TurnoutCanvas(turnout);
		turnoutCanvas.addMouseListener(new MouseAction());
		addMouseListener(new MouseAction());

		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		numberLabel = new JLabel();
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 25));
		statePanel = new JPanel();

		setLayout(new MigLayout());

		if (forHistory) {
			add(numberLabel);
			add(turnoutCanvas);
		} else {
			add(numberLabel, "");
			add(statePanel, "wrap, w 15!, h 5!, align right");
			add(turnoutCanvas, "span 2");
		}
	}

	public void updateTurnout() {
		numberLabel.setText(Integer.toString(turnout.getNumber()));
	}

	private class MouseAction extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (!widgetEnabled) {
				return;
			}
			final TurnoutControlIface turnoutControl = AdHocRailway
					.getInstance().getTurnoutControl();

			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				try {
					if (!testMode) {
						turnoutControl.toggle(turnout);
					} else {
						turnoutControl.toggleTest(turnout);
					}
				} catch (final TurnoutException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
			} else if (e.getClickCount() == 1
					&& e.getButton() == MouseEvent.BUTTON3) {

				if (AdHocRailway.getInstance().isEditingMode()) {
					displaySwitchConfig();
				}
			}

		}

		private void displaySwitchConfig() {
			if (testMode) {
				return;
			}
			final TurnoutControlIface turnoutControl = AdHocRailway
					.getInstance().getTurnoutControl();

			turnoutControl.removeTurnoutChangeListener(TurnoutWidget.this);
			new TurnoutConfig(AdHocRailway.getInstance(), turnout,
					turnout.getTurnoutGroup());
			TurnoutHelper.validateTurnout(AdHocRailway.getInstance()
					.getTurnoutPersistence(), turnout, TurnoutWidget.this);
			turnoutControl.addOrUpdateTurnout(turnout);
			turnoutControl
					.addTurnoutChangeListener(turnout, TurnoutWidget.this);

			turnoutChanged(turnout, actualTurnoutState);
		}
	}

	public Turnout getTurnout() {
		return turnout;
	}

	@Override
	public void turnoutChanged(final Turnout changedTurnout,
			final SRCPTurnoutState newState) {
		if (turnout.equals(changedTurnout)) {
			actualTurnoutState = newState;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					numberLabel.setText(Integer.toString(turnout.getNumber()));
					turnoutCanvas.setTurnoutState(actualTurnoutState);
					switch (actualTurnoutState) {
					case LEFT:
					case RIGHT:
						if (turnout.getDefaultState().equals(
								TurnoutState.STRAIGHT)) {
							statePanel.setBackground(Color.RED);
						} else {
							statePanel.setBackground(Color.GREEN);
						}

						break;
					case STRAIGHT:
						if (turnout.getDefaultState().equals(
								TurnoutState.STRAIGHT)) {
							statePanel.setBackground(Color.GREEN);
						} else {
							statePanel.setBackground(Color.RED);
						}

						break;
					case UNDEF:
					default:
						statePanel.setBackground(Color.GRAY);
						break;

					}
					TurnoutWidget.this.revalidate();
					TurnoutWidget.this.repaint();
				}
			});
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);

		final TurnoutControlIface turnoutControl = AdHocRailway.getInstance()
				.getTurnoutControl();
		turnoutControl.removeTurnoutChangeListener(this);
		if (!enabled) {
			setBackground(new Color(255, 177, 177));
		} else {
			turnoutControl.addTurnoutChangeListener(turnout, this);
		}
		widgetEnabled = enabled;
		turnoutCanvas.setTurnoutState(SRCPTurnoutState.UNDEF);
	}

	public void setTurnout(final Turnout turnout) {
		this.turnout = turnout;
		updateTurnout();
	}
}
