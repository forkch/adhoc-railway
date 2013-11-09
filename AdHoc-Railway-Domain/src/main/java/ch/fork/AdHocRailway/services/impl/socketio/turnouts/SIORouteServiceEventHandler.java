package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.services.turnouts.RouteServiceListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SIORouteServiceEventHandler {

	private static Map<String, RouteGroup> sioIdToRouteGroupMap = new HashMap<String, RouteGroup>();
	private static Map<String, Route> sioIdToRouteMap = new HashMap<String, Route>();

	public static void handleRouteInit(final JSONObject data,
			final RouteServiceListener listener) throws JSONException {
		final JSONArray routeGroupsJ = data.getJSONArray("routeGroups");
		final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();
		for (int i = 0; i < routeGroupsJ.length(); i++) {
			final JSONObject routeGroupJSON = routeGroupsJ.getJSONObject(i);
			final RouteGroup routeGroup = SIORouteMapper
					.mapRouteGroupFromJSON(routeGroupJSON);

			sioIdToRouteGroupMap.put(routeGroupJSON.getString("_id"),
					routeGroup);
			for (final Route route : routeGroup.getRoutes()) {
				sioIdToRouteMap.put(
						SIORouteMapper.routeIdMap.get(route.getId()), route);
			}
			routeGroups.add(routeGroup);
		}

		listener.routesUpdated(routeGroups);

	}

	public static void handleRouteAdded(final JSONObject routeJSON,
			final RouteServiceListener listener) throws JSONException {
		final Route route = SIORouteMapper.mapRouteFromJSON(routeJSON);
		sioIdToRouteMap.put(routeJSON.getString("_id"), route);
		final RouteGroup routeGroup = sioIdToRouteGroupMap.get(routeJSON
				.get("group"));
		route.setRouteGroup(routeGroup);
		routeGroup.addRoute(route);
		listener.routeAdded(route);
	}

	public static void handleRouteUpdated(final JSONObject routeJSON,
			final RouteServiceListener listener) throws JSONException {
		final Route route = sioIdToRouteMap.get(routeJSON.getString("_id"));

		SIORouteMapper.mergeRouteBaseInfo(routeJSON, route);
		listener.routeUpdated(route);

	}

	public static void handleRouteRemoved(final JSONObject routeJSON,
			final RouteServiceListener listener) throws JSONException {
		final Route route = sioIdToRouteMap.get(routeJSON.getString("_id"));
		SIORouteMapper.routeIdMap.remove(route.getId());
		final RouteGroup routeGroup = sioIdToRouteGroupMap.get(routeJSON
				.getString("group"));
		routeGroup.removeRoute(route);
		listener.routeRemoved(route);
	}

	public static void handleRouteGroupAdded(final JSONObject routeGroupJSON,
			final RouteServiceListener listener) throws JSONException {
		final RouteGroup routeGroup = SIORouteMapper
				.mapRouteGroupFromJSON(routeGroupJSON);
		sioIdToRouteGroupMap.put(routeGroupJSON.getString("_id"), routeGroup);
		listener.routeGroupAdded(routeGroup);
	}

	public static void handleRouteGroupUpdated(final JSONObject data,
			final RouteServiceListener listener) throws JSONException {
		final RouteGroup routeGroup = sioIdToRouteGroupMap.get(data
				.getString("_id"));
		SIORouteMapper.mergeRouteGroupBaseInfo(routeGroup, data);
		listener.routeGroupUpdated(routeGroup);
	}

	public static void handleRouteGroupRemoved(final JSONObject routeGroupJSON,
			final RouteServiceListener listener) throws JSONException {
		final RouteGroup routeGroup = SIORouteMapper
				.mapRouteGroupFromJSON(routeGroupJSON);
		sioIdToRouteGroupMap.remove(routeGroupJSON.getString("_id"));
		listener.routeGroupRemoved(routeGroup);

	}

	public static void addIdToRoute(final Route route, final String sioId) {
		final int id = sioId.hashCode();
		route.setId(id);
		sioIdToRouteMap.put(sioId, route);
		SIORouteMapper.routeIdMap.put(id, sioId);

	}

	public static void addIdToRouteGroup(final RouteGroup group,
			final String sioId) {
		final int id = sioId.hashCode();
		group.setId(id);
		sioIdToRouteGroupMap.put(sioId, group);
		SIORouteMapper.routeGroupIdMap.put(id, sioId);
	}
}
