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

import ch.fork.AdHocRailway.controllers.impl.brain.BrainController;
import ch.fork.AdHocRailway.controllers.impl.brain.BrainTurnoutControlAdapter;
import ch.fork.AdHocRailway.controllers.impl.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

import java.util.ArrayList;
import java.util.List;

public abstract class TurnoutController {

	protected final List<TurnoutChangeListener> listeners = new ArrayList<TurnoutChangeListener>();

	public void addTurnoutChangeListener(final TurnoutChangeListener listener) {
		listeners.add(listener);
	}

	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	public void removeTurnoutChangeListener(final TurnoutChangeListener listener) {
		System.out.println(listener);
		listeners.remove(listener);
	}

	protected void informListeners(final Turnout turnout) {

		for (final TurnoutChangeListener scl : listeners) {
			scl.turnoutChanged(turnout);
		}
	}

	public abstract void toggle(final Turnout turnout) throws TurnoutException;

	public abstract void toggleTest(final Turnout turnout)
			throws TurnoutException;

	public abstract void setDefaultState(final Turnout turnout)
			throws TurnoutException;

	public abstract void setStraight(final Turnout turnout)
			throws TurnoutException;

	public abstract void setCurvedLeft(final Turnout turnout)
			throws TurnoutException;

	public abstract void setCurvedRight(final Turnout turnout)
			throws TurnoutException;

	public abstract void setTurnoutWithAddress(final int address,
			final TurnoutState straight);

	public abstract void reloadConfiguration();

	public static TurnoutController createTurnoutController(
			final RailwayDevice railwayDevice) {

		if (railwayDevice == null) {
			return new NullTurnoutController();
		}
		switch (railwayDevice) {
		case ADHOC_BRAIN:
			return new BrainTurnoutControlAdapter(BrainController.getInstance());
		case SRCP:
			return new SRCPTurnoutControlAdapter();
		default:
			return new NullTurnoutController();
		}

	}

	public void setNonDefaultState(final Turnout turnout)
			throws TurnoutException {
		if (turnout.isThreeWay()) {
			return;
		}
		switch (turnout.getDefaultState()) {
		case LEFT:
		case RIGHT:
			setStraight(turnout);
			break;
		case STRAIGHT:
			setCurvedLeft(turnout);
			break;
		default:
			break;

		}
	}

	static class NullTurnoutController extends TurnoutController {

		@Override
		public void toggle(final Turnout turnout) throws TurnoutException {
			// TODO Auto-generated method stub

		}

		@Override
		public void toggleTest(final Turnout turnout) throws TurnoutException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setDefaultState(final Turnout turnout)
				throws TurnoutException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setStraight(final Turnout turnout) throws TurnoutException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setCurvedLeft(final Turnout turnout)
				throws TurnoutException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setCurvedRight(final Turnout turnout)
				throws TurnoutException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setTurnoutWithAddress(final int address,
				final TurnoutState straight) {
			// TODO Auto-generated method stub

		}

		@Override
		public void reloadConfiguration() {
			// TODO Auto-generated method stub

		}

	}

}