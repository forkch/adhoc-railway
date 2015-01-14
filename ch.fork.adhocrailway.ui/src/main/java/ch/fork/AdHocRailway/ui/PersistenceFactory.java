package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestRouteService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLLocomotiveService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLRouteService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLTurnoutService;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.RouteService;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;

public class PersistenceFactory {

    public static LocomotiveService createLocomotiveService(
            final boolean useAdHocServer, PersistenceManagerContext appContext) {

        if (useAdHocServer) {
            String endpointURL = getEndpointUrl(appContext);
            return new RestLocomotiveService(endpointURL, appContext.getSioService(), appContext.getAppUUID());
        } else {
            return new XMLLocomotiveService();
        }
    }

    public static TurnoutService createTurnoutService(
            final boolean useAdHocServer, PersistenceManagerContext appContext) {
        if (useAdHocServer) {
            String endpointURL = getEndpointUrl(appContext);
            return new RestTurnoutService(endpointURL, appContext.getSioService(), appContext.getAppUUID());
        } else {
            return new XMLTurnoutService();
        }
    }


    public static RouteService createRouteService(final boolean useAdHocServer, PersistenceManagerContext appContext) {

        if (useAdHocServer) {
            String endpointURL = getEndpointUrl(appContext);
            return new RestRouteService(endpointURL, appContext.getSioService(), appContext.getAppUUID());
        } else {
            return new XMLRouteService();
        }

    }

    private static String getEndpointUrl(PersistenceManagerContext appContext) {
        String host = appContext.getPreferences().getStringValue(PreferencesKeys.ADHOC_SERVER_HOSTNAME);
        String port = appContext.getPreferences().getStringValue(PreferencesKeys.ADHOC_SERVER_PORT);
        return "http://" + host + ":" + port;
    }
}
