package ch.fork.adhocrailway.persistence.adhocserver.impl.rest;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
import ch.fork.adhocrailway.model.turnouts.RouteItem;
import ch.fork.adhocrailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.adhocrailway.persistence.adhocserver.impl.socketio.turnouts.SIORouteCallback;
import ch.fork.adhocrailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.adhocrailway.services.AdHocServiceException;
import ch.fork.adhocrailway.services.RouteService;
import ch.fork.adhocrailway.services.RouteServiceListener;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Created by fork on 4/4/14.
 */
public class RestRouteService implements RouteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestRouteService.class);

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
            listener.failure(new AdHocServiceException(e));
            return Sets.newTreeSet();
        }
    }

    @Override
    public void addRouteGroup(RouteGroup group) {
        RouteGroup addRouteGroup = null;
        try {
            addRouteGroup = restRouteServiceClient.addRouteGroup(group).execute().body();
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
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
            if (listenerOk()) {
                listener.routeGroupRemoved(group);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void updateRouteGroup(RouteGroup group) {
        try {
            restRouteServiceClient.updateRouteGroup(group).execute().body();
            if (listenerOk()) {
                listener.routeGroupUpdated(group);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
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
            route.setId(addTurnout.getId());
            if (listenerOk()) {
                listener.routeAdded(route);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void removeRoute(Route route) {
        try {
            Route deletedRoute = restRouteServiceClient.deleteRoute(route.getId()).execute().body();
            if (listenerOk()) {
                listener.routeRemoved(route);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void updateRoute(Route route) {
        try {
            Route updatedRoute = restRouteServiceClient.updateRoute(route).execute().body();
            route.setId(updatedRoute.getId());
            if (listenerOk()) {
                listener.routeUpdated(updatedRoute);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void clear() {
        SortedSet<RouteGroup> routeGroups;
        try {
            routeGroups = restRouteServiceClient.deleteAllRouteGroups().execute().body();
            if (listenerOk()) {
                listener.routesUpdated(routeGroups);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
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
