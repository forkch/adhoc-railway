package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.controllers.RouteController;
import ch.fork.adhocrailway.controllers.TurnoutController;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.model.turnouts.Route;

import java.util.List;

public interface TurnoutContext extends ControllerContext {

    public abstract TurnoutController getTurnoutControl();

    public abstract TurnoutManager getTurnoutManager();

    public List<Route> getAllRoutes();

    RouteController getRouteControl();
    Route getRouteForNumber(int routeNumber);
}
