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

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

public interface TurnoutController {

	public void toggle(Turnout turnout) throws TurnoutException;

	public void toggleTest(Turnout turnout) throws TurnoutException;

	public void setDefaultState(Turnout turnout) throws TurnoutException;

	public void setNonDefaultState(Turnout turnout) throws TurnoutException;

	public void setStraight(Turnout turnout) throws TurnoutException;

	public void setCurvedLeft(Turnout turnout) throws TurnoutException;

	public void setCurvedRight(Turnout turnout) throws TurnoutException;

	public abstract void addOrUpdateTurnout(Turnout turnout);

	public abstract void reloadConfiguration();

	public void addTurnoutChangeListener(Turnout turnout,
			TurnoutChangeListener listener);

	public void removeAllTurnoutChangeListener();

	public void removeTurnoutChangeListener(Turnout turnout);

	public void removeTurnoutChangeListener(TurnoutChangeListener listener);

}