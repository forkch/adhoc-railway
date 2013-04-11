package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import de.dermoba.srcp.client.SRCPSession;

public interface TurnoutContext {

	public abstract SRCPSession getSession();

	public abstract TurnoutControlIface getTurnoutControl();

	public abstract TurnoutManager getTurnoutManager();

}