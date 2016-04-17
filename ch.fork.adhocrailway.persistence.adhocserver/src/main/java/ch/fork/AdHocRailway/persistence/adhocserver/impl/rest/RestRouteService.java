package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.model.turnouts.RouteItem;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.turnouts.SIORouteCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.AdHocRailway.services.RouteService;
import ch.fork.AdHocRailway.services.RouteServiceListener;
import org.apache.log4j.Logger;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Created by fork on 4/4/14.
 */
public class RestRouteService implements RouteService {

    private static final Logger LOGGER = Logger.getLogger(RestRouteService.class);

    private final SIORouteCallback sioRouteService;

    private final RestRouteServiceClient restRouteServiceClient;
    private RouteServiceListener listener;

    public RestRouteService(String endpointUrl, SIOService sioService, String uuid) {
        Retrofit retrofit = RestAdapterFactory.createRestAdapter(endpointUrl, uuid);
        restRouteServiceClient = retrofit.create(RestRouteServiceClient.class);
        sioRouteService = new SIORouteCallback(sioService);
    }

    @Override
    public void init(RouteServiceListener listener) {
        this.listener = listener;
        sioRouteService.init(listener);
    }

    @Override
    public SortedSet<RouteGroup> getAllRouteGroups() {
        try {
            return restRouteServiceClient.getAllRouteGroups().execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addRouteGroup(RouteGroup group) {
        RouteGroup addRouteGroup = null;
        try {
            addRouteGroup = restRouteServiceClient.addRouteGroup(group).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        group.setId(addRouteGroup.getId());
        if (listenerOk()) {
            listener.routeGroupAdded(group);
        }
    }

    @Override
    public void removeRouteGroup(RouteGroup group) {
        try {
            RouteGroup removedRouteGroup = restRouteServiceClient.deleteRouteGroup(group.getId()).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.routeGroupRemoved(group);
        }
    }

    @Override
    public void updateRouteGroup(RouteGroup group) {
        try {
            restRouteServiceClient.updateRouteGroup(group).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
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
        Route addTurnout = null;
        try {
            addTurnout = restRouteServiceClient.addRoute(route).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        route.setId(addTurnout.getId());
        if (listenerOk()) {
            listener.routeAdded(route);
        }
    }

    @Override
    public void removeRoute(Route route) {
        try {
            Route deletedRoute = restRouteServiceClient.deleteRoute(route.getId()).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.routeRemoved(route);
        }
    }

    @Override
    public void updateRoute(Route route) {
        try {
            Route updatedRoute = restRouteServiceClient.updateRoute(route).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.routeUpdated(route);
        }
    }

    @Override
    public void clear() {
        SortedSet<RouteGroup> routeGroups;
        try {
            routeGroups = restRouteServiceClient.deleteAllRouteGroups().execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
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
