package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.Route;

import java.util.List;

public interface TurnoutContext extends ControllerContext {

    public abstract TurnoutController getTurnoutControl();

    public abstract TurnoutManager getTurnoutManager();

    public List<Route> getAllRoutes();

    RouteController getRouteControl();
    Route getRouteForNumber(int routeNumber);
}