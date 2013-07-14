/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutControlIface.java 248 2012-12-28 17:08:16Z fork_ch $
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

package ch.fork.AdHocRailway.controllers;

import java.util.ArrayList;
import java.util.List;

import ch.fork.AdHocRailway.controllers.impl.brain.BrainController;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainTurnoutControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

public abstract class TurnoutController {

	private final List<TurnoutChangeListener> listeners = new ArrayList<TurnoutChangeListener>();

	public void addTurnoutChangeListener(final Turnout turnout,
			final TurnoutChangeListener listener) {
		listeners.add(listener);
	}

	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	public void removeTurnoutChangeListener(final TurnoutChangeListener listener) {
		listeners.remove(listener);
	}

	protected void informListeners(final Turnout turnout,
			final TurnoutState state) {

		for (final TurnoutChangeListener scl : listeners) {
			scl.turnoutChanged(turnout, state);
		}
	}

	public abstract void toggle(Turnout turnout) throws TurnoutException;

	public abstract void toggleTest(Turnout turnout) throws TurnoutException;

	public abstract void setDefaultState(Turnout turnout)
			throws TurnoutException;

	public abstract void setNonDefaultState(Turnout turnout)
			throws TurnoutException;

	public abstract void setStraight(Turnout turnout) throws TurnoutException;

	public abstract void setCurvedLeft(Turnout turnout) throws TurnoutException;

	public abstract void setCurvedRight(Turnout turnout)
			throws TurnoutException;

	public abstract void addOrUpdateTurnout(Turnout turnout);

	public abstract void reloadConfiguration();

	public static TurnoutController createTurnoutController(
			final RailwayDevice railwayDevice) {

		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainTurnoutControlAdapter(BrainController.getInstance());
		case SRCP:
			return new SRCPTurnoutControlAdapter();
		default:

			throw new IllegalArgumentException("unknown railway-device"
					+ railwayDevice);

		}

	}

}