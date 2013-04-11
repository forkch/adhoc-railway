package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;

public interface RouteContext extends ControllerContext {

	public abstract RouteControlIface getRouteControl();

	public abstract RouteManager getRouteManager();

	public abstract TurnoutManager getTurnoutManager();

}