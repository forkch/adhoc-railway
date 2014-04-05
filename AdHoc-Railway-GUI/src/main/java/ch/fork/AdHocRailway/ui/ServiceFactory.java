package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.RouteService;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.services.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.services.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.impl.xml.XMLRouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLTurnoutService;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;

public class ServiceFactory {

    public static LocomotiveService createLocomotiveService(
            final boolean useAdHocServer, PersistenceManagerContext appContext) {

        if (useAdHocServer) {
            String endpointURL = getEndpointUrl(appContext);
            return new RestLocomotiveService(appContext.getAppUUID(), endpointURL);
        } else {
            return new XMLLocomotiveService();
        }
    }

    public static TurnoutService createTurnoutService(
            final boolean useAdHocServer, PersistenceManagerContext appContext) {
        if (useAdHocServer) {
            String endpointURL = getEndpointUrl(appContext);
            return new RestTurnoutService(appContext.getAppUUID(), endpointURL);
        } else {
            return new XMLTurnoutService();
        }
    }

    private static String getEndpointUrl(PersistenceManagerContext appContext) {
        String host = appContext.getPreferences().getStringValue(PreferencesKeys.ADHOC_SERVER_HOSTNAME);
        String port = appContext.getPreferences().getStringValue(PreferencesKeys.ADHOC_SERVER_PORT);
        return "http://" + host + ":" + port;
    }

    public static RouteService createRouteService(final boolean useAdHocServer) {

        if (useAdHocServer) {
            return new SIORouteService();
        } else {
            return new XMLRouteService();
        }

    }
}
