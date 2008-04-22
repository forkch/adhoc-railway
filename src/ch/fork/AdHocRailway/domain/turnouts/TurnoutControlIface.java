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

package ch.fork.AdHocRailway.domain.turnouts;

public interface TurnoutControlIface {

	public void update();

	public void toggle(Turnout turnout) throws TurnoutException;

	public void setDefaultState(Turnout turnout) throws TurnoutException;

	public void setNonDefaultState(Turnout turnout) throws TurnoutException;

	public void setStraight(Turnout turnout) throws TurnoutException;

	public void setCurvedLeft(Turnout turnout) throws TurnoutException;

	public void setCurvedRight(Turnout turnout) throws TurnoutException;

	public void addTurnoutChangeListener(Turnout turnout,
			TurnoutChangeListener listener);

	public void removeTurnoutChangeListener(Turnout turnout);

	public void removeAllTurnoutChangeListener();

	public void removeTurnoutChangeListener(TurnoutChangeListener listener);

	public void undoLastChange() throws TurnoutException;

	public void previousDeviceToDefault() throws TurnoutException;

	public SRCPTurnoutState getTurnoutState(Turnout turnout);

	public void refresh(Turnout turnout) throws TurnoutException;

	public void setPersistence(TurnoutPersistenceIface persistence);

}