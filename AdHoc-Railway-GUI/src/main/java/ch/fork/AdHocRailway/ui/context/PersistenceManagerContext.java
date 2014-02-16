package ch.fork.AdHocRailway.ui.context;

import com.google.common.eventbus.EventBus;

import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;

public interface PersistenceManagerContext {

	Preferences getPreferences();

	LocomotiveManager getLocomotiveManager();

	EventBus getMainBus();

	void setLocomotiveManager(final LocomotiveManager locomotiveManager);

	TurnoutManager getTurnoutManager();

	void setTurnoutManager(final TurnoutManager turnoutManager);

	RouteManager getRouteManager();

	void setRouteManager(final RouteManager routeManager);

}
