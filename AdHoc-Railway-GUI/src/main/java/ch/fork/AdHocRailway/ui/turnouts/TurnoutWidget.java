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

import ch.fork.AdHocRailway.controllers.TurnoutChangeListener;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TurnoutWidget extends JPanel implements TurnoutChangeListener {

	private static final long serialVersionUID = 6871966498091781447L;

	private Turnout turnout;

	private JLabel numberLabel;

	private TurnoutCanvas turnoutCanvas;

	private TurnoutState actualTurnoutState = TurnoutState.UNDEF;

	private boolean widgetEnabled;

	private final boolean testMode;

	private final boolean forHistory;

	private JPanel statePanel;

	private final TurnoutManager turnoutManager;

	private final TurnoutController turnoutControl;

	private final TurnoutContext ctx;

	public TurnoutWidget(final TurnoutContext ctx, final Turnout turnout,
			final boolean forHistory) {
		this(ctx, turnout, forHistory, false);
	}

	public TurnoutWidget(final TurnoutContext ctx, final Turnout turnout,
			final boolean forHistory, final boolean testMode) {
		this.ctx = ctx;
		this.turnout = turnout;
		this.forHistory = forHistory;
		this.testMode = testMode;

		turnoutControl = ctx.getTurnoutControl();
		turnoutManager = ctx.getTurnoutManager();
		widgetEnabled = true;

		initGUI();
		updateTurnout();
		TurnoutHelper.validateTurnout(turnoutManager, turnout, this);
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
			add(statePanel, "wrap, w 20!, h 7!, align right");
			add(turnoutCanvas, "span 2");
		}

	}

	public void updateTurnout() {
		turnoutControl.removeTurnoutChangeListener(this);

		numberLabel.setText(Integer.toString(turnout.getNumber()));

		final String turnoutDescription = TurnoutHelper
				.getTurnoutDescription(turnout);
		setToolTipText(turnoutDescription);
		turnoutCanvas.setToolTipText(turnoutDescription);
		turnoutControl.addTurnoutChangeListener(turnout, this);
	}

	private class MouseAction extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (!widgetEnabled) {
				return;
			}

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

				if (ctx.isEditingMode()) {
					displaySwitchConfig();
				}
			}
		}

		private void displaySwitchConfig() {
			if (testMode) {
				return;
			}

			turnoutControl.removeTurnoutChangeListener(TurnoutWidget.this);
			new TurnoutConfig(ctx.getMainFrame(), ctx, turnout,
					turnout.getTurnoutGroup());
			TurnoutHelper.validateTurnout(turnoutManager, turnout,
					TurnoutWidget.this);
			turnoutControl.addOrUpdateTurnout(turnout);
			turnoutControl
					.addTurnoutChangeListener(turnout, TurnoutWidget.this);

			turnoutChanged(turnout);
		}
	}

	public Turnout getTurnout() {
		return turnout;
	}

	@Override
	public void turnoutChanged(final Turnout changedTurnout) {
		if (turnout.equals(changedTurnout)) {
			actualTurnoutState = changedTurnout.getActualState();
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
							statePanel.setBackground(UIConstants.STATE_RED);
						} else {
							statePanel.setBackground(UIConstants.STATE_GREEN);
						}

						break;
					case STRAIGHT:
						if (turnout.getDefaultState().equals(
								TurnoutState.STRAIGHT)) {
							statePanel.setBackground(UIConstants.STATE_GREEN);
						} else {
							statePanel.setBackground(UIConstants.STATE_RED);
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

		turnoutControl.removeTurnoutChangeListener(this);
		if (!enabled) {
			setBackground(new Color(255, 177, 177));
		} else {
			turnoutControl.addTurnoutChangeListener(turnout, this);
		}
		widgetEnabled = enabled;
		turnoutCanvas.setTurnoutState(TurnoutState.UNDEF);
	}

	public void setTurnout(final Turnout turnout) {
		this.turnout = turnout;
		updateTurnout();
	}

	public void revalidateTurnout() {
		TurnoutHelper.validateTurnout(turnoutManager, turnout, this);
	}
}
