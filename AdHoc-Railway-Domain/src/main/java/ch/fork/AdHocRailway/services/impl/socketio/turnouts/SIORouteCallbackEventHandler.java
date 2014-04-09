package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.*;
import ch.fork.AdHocRailway.services.RouteServiceListener;
import ch.fork.AdHocRailway.utils.GsonFactory;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SIORouteCallbackEventHandler {

    private static Map<String, RouteGroup> sioIdToRouteGroupMap = new HashMap<String, RouteGroup>();
    private static Map<String, Route> sioIdToRouteMap = new HashMap<String, Route>();
    private static Gson gson = GsonFactory.createGson();

    public static void handleRouteInit(final JSONArray routeGroupsJ,
                                       final RouteServiceListener listener) throws JSONException {
        final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();
        for (int i = 0; i < routeGroupsJ.length(); i++) {
            final JSONObject routeGroupJSON = routeGroupsJ.getJSONObject(i);
            final RouteGroup routeGroup = deserializeRouteGroup(routeGroupJSON);

            sioIdToRouteGroupMap.put(routeGroup.getId(),
                    routeGroup);
            for (final Route route : routeGroup.getRoutes()) {
                sioIdToRouteMap.put(route.getId(), route);
                route.setRouteGroup(routeGroup);
            }
            routeGroups.add(routeGroup);
        }

        listener.routesUpdated(routeGroups);
    }

    public static void handleRouteAdded(final JSONObject routeJSON,
                                        final RouteServiceListener listener) throws JSONException {
        final Route route = deserializeRoute(routeJSON);
        sioIdToRouteMap.put(route.getId(), route);
        final RouteGroup routeGroup = sioIdToRouteGroupMap.get(route.getGroupId());
        route.setRouteGroup(routeGroup);
        routeGroup.addRoute(route);
        listener.routeAdded(route);
    }

    public static void handleRouteUpdated(final JSONObject routeJSON,
                                          final RouteServiceListener listener) throws JSONException {
        final Route route = deserializeRoute(routeJSON);
        route.setRouteGroup(sioIdToRouteGroupMap.get(route.getGroupId()));

        listener.routeUpdated(route);

    }

    public static void handleRouteRemoved(final JSONObject routeJSON,
                                          final RouteServiceListener listener) throws JSONException {
        final Route route = deserializeRoute(routeJSON);
        final RouteGroup routeGroup = sioIdToRouteGroupMap.get(route.getGroupId());
        routeGroup.removeRoute(route);
        listener.routeRemoved(route);
    }

    public static void handleRouteGroupAdded(final JSONObject routeGroupJSON,
                                             final RouteServiceListener listener) throws JSONException {
        final RouteGroup routeGroup = deserializeRouteGroup(routeGroupJSON);
        sioIdToRouteGroupMap.put(routeGroup.getId(), routeGroup);
        listener.routeGroupAdded(routeGroup);
    }

    public static void handleRouteGroupUpdated(final JSONObject routeGroupJSON,
                                               final RouteServiceListener listener) throws JSONException {
        final RouteGroup routeGroup = deserializeRouteGroup(routeGroupJSON);
        sioIdToRouteGroupMap.put(routeGroup.getId(), routeGroup);
        listener.routeGroupUpdated(routeGroup);
    }

    public static void handleRouteGroupRemoved(final JSONObject routeGroupJSON,
                                               final RouteServiceListener listener) throws JSONException {
        final RouteGroup routeGroup = deserializeRouteGroup(routeGroupJSON);
        sioIdToRouteGroupMap.remove(routeGroup.getId());
        listener.routeGroupRemoved(routeGroup);

    }

    private static RouteGroup deserializeRouteGroup(JSONObject routeGroupJson) {
        return gson.fromJson(routeGroupJson.toString(), RouteGroup.class);
    }

    private static Route deserializeRoute(JSONObject routeJson) {
        return gson.fromJson(routeJson.toString(), Route.class);
    }
}

