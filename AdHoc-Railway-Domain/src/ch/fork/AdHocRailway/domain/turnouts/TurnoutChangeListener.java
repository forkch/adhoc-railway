/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: TurnoutChangeListener.java 177 2008-04-24 19:28:06Z fork_ch $
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

import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;


public interface TurnoutChangeListener {
	public void turnoutChanged(Turnout changedTurnout, SRCPTurnoutState newState);
}
