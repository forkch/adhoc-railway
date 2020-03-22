package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.controllers.RouteController;
import ch.fork.adhocrailway.manager.RouteManager;
import ch.fork.adhocrailway.manager.TurnoutManager;

public interface RouteContext extends ControllerContext {

    public abstract RouteController getRouteControl();

    public abstract RouteManager getRouteManager();

    public abstract TurnoutManager getTurnoutManager();

}
