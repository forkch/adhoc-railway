
package ch.fork.AdHocRailway.domain.routes;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;

public class RouteControl extends Control {

    private static RouteControl instance;
    private Map<String, Route>  routes;

    private RouteControl() {
        routes = new TreeMap<String, Route>();
    }

    public static RouteControl getInstance() {
        if (instance == null) {
            instance = new RouteControl();
        }
        return instance;
    }

    public void registerRoute(Route route) {
        routes.put(route.getName(), route);
    }

    public void unregisterRoute(Route route) {
        routes.remove(route.getName());
    }

    public void registerRoutes(Collection<Route> routesToRegister) {
        for (Route r : routesToRegister) {
            routes.put(r.getName(), r);
        }
    }

    public void unregisterAllRoutes() {
        routes.clear();
    }
    
    public Route getRoute(String name) {
        return routes.get(name);
    }
    public Map<String, Route> getRoutes() {
        return routes;
    }

    public void enableRoute(Route r) throws SwitchException {
        //System.out.println("enabling route: " + r);
        int waitTime = Preferences.getInstance().getIntValue(
            PreferencesKeys.ROUTING_DELAY);
        Thread switchRouter = new Router(r, true, waitTime);
        switchRouter.start();
    }

    public void disableRoute(Route r) throws SwitchException {
        //System.out.println("disabling route: " + r);
        int waitTime = Preferences.getInstance().getIntValue(
            PreferencesKeys.ROUTING_DELAY);
        Thread switchRouter = new Router(r, false, waitTime);
        switchRouter.start();
    }
}
