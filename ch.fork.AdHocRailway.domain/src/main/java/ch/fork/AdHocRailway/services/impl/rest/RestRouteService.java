package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.model.turnouts.RouteItem;
import ch.fork.AdHocRailway.services.RouteService;
import ch.fork.AdHocRailway.services.RouteServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteCallback;
import ch.fork.AdHocRailway.utils.RestAdapterFactory;
import org.apache.log4j.Logger;
import retrofit.RestAdapter;

import java.util.SortedSet;

/**
 * Created by fork on 4/4/14.
 */
public class RestRouteService implements RouteService {

    private static final Logger LOGGER = Logger.getLogger(RestRouteService.class);

    private final SIORouteCallback sioRouteService;

    private final RestRouteServiceClient restRouteServiceClient;
    private RouteServiceListener listener;

    public RestRouteService(String endpointUrl, String uuid) {
        RestAdapter restAdapter = RestAdapterFactory.createRestAdapter(endpointUrl, uuid);
        restRouteServiceClient = restAdapter.create(RestRouteServiceClient.class);
        sioRouteService = new SIORouteCallback();
    }

    @Override
    public void init(RouteServiceListener listener) {
        this.listener = listener;
        sioRouteService.init(listener);
    }

    @Override
    public SortedSet<RouteGroup> getAllRouteGroups() {
        return restRouteServiceClient.getAllRouteGroups();
    }

    @Override
    public void addRouteGroup(RouteGroup group) {
        RouteGroup addRouteGroup = restRouteServiceClient.addRouteGroup(group);
        group.setId(addRouteGroup.getId());
        if (listenerOk()) {
            listener.routeGroupAdded(group);
        }
    }

    @Override
    public void removeRouteGroup(RouteGroup group) {
        RouteGroup removedRouteGroup = restRouteServiceClient.deleteRouteGroup(group.getId());
        if (listenerOk()) {
            listener.routeGroupRemoved(group);
        }
    }

    @Override
    public void updateRouteGroup(RouteGroup group) {
        restRouteServiceClient.updateRouteGroup(group);
        if (listenerOk()) {
            listener.routeGroupUpdated(group);
        }
    }

    @Override
    public void addRouteItem(RouteItem item) {

    }

    @Override
    public void removeRouteItem(RouteItem item) {

    }

    @Override
    public void updateRouteItem(RouteItem item) {

    }

    @Override
    public void addRoute(Route route) {
        Route addTurnout = restRouteServiceClient.addRoute(route);
        route.setId(addTurnout.getId());
        if (listenerOk()) {
            listener.routeAdded(route);
        }
    }

    @Override
    public void removeRoute(Route route) {
        Route deletedRoute = restRouteServiceClient.deleteRoute(route.getId());
        if (listenerOk()) {
            listener.routeRemoved(route);
        }
    }

    @Override
    public void updateRoute(Route route) {
        Route updatedRoute = restRouteServiceClient.updateRoute(route);
        if (listenerOk()) {
            listener.routeUpdated(route);
        }
    }

    @Override
    public void clear() {
        SortedSet<RouteGroup> routeGroups = restRouteServiceClient.deleteAllRouteGroups();
        if (listenerOk()) {
            listener.routesUpdated(routeGroups);
        }
    }


    @Override
    public void disconnect() {
        sioRouteService.disconnect();
    }

    private boolean listenerOk() {
        return listener != null;
    }
}
