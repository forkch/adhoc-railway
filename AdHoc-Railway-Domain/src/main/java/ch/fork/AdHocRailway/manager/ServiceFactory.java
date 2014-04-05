package ch.fork.AdHocRailway.manager;

import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.RouteService;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.services.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.services.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.impl.xml.XMLRouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLTurnoutService;

public class ServiceFactory {

    public static LocomotiveService createLocomotiveService(
            final boolean useAdHocServer, String uuid) {

        if (useAdHocServer) {
            return new RestLocomotiveService(uuid);
        } else {
            return new XMLLocomotiveService();
        }
    }

    public static TurnoutService createTurnoutService(
            final boolean useAdHocServer, String uuid) {
        if (useAdHocServer) {
            return new RestTurnoutService(uuid);
        } else {
            return new XMLTurnoutService();
        }
    }

    public static RouteService createRouteService(final boolean useAdHocServer) {

        if (useAdHocServer) {
            return new SIORouteService();
        } else {
            return new XMLRouteService();
        }

    }
}
