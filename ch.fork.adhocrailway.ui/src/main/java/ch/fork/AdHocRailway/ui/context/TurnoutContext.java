package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.TurnoutManager;

public interface TurnoutContext extends ControllerContext {

    public abstract TurnoutController getTurnoutControl();

    public abstract TurnoutManager getTurnoutManager();

}