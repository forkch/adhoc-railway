package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import de.dermoba.srcp.client.SRCPSession;

public interface RouteContext {

	public abstract SRCPSession getSession();

	public abstract RouteControlIface getRouteControl();

	public abstract RouteManager getRouteManager();

	public abstract TurnoutManager getTurnoutManager();

}