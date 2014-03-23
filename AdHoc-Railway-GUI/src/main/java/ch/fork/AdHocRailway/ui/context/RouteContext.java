package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;

public interface RouteContext extends ControllerContext {

    public abstract RouteController getRouteControl();

    public abstract RouteManager getRouteManager();

    public abstract TurnoutManager getTurnoutManager();

}