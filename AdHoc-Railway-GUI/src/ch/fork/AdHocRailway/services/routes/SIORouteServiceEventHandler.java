package ch.fork.AdHocRailway.services.routes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;

public class SIORouteServiceEventHandler {

	private static Map<String, RouteGroup> sioIdToRouteGroupMap = new HashMap<String, RouteGroup>();
	private static Map<String, Route> sioIdToRouteMap = new HashMap<String, Route>();

	public static void handleRouteInit(JSONObject data,
			RouteServiceListener listener) throws JSONException {
		JSONArray routeGroupsJ = data.getJSONArray("routeGroups");
		List<RouteGroup> routeGroups = new LinkedList<RouteGroup>();
		for (int i = 0; i < routeGroupsJ.length(); i++) {
			JSONObject routeGroupJSON = routeGroupsJ.getJSONObject(i);
			RouteGroup routeGroup = SIORouteMapper
					.mapRouteGroupFromJSON(routeGroupJSON);

			sioIdToRouteGroupMap.put(routeGroupJSON.getString("_id"),
					routeGroup);
			for (Route route : routeGroup.getRoutes()) {
				sioIdToRouteMap.put(
						SIORouteMapper.routeIdMap.get(route.getId()), route);
			}
			routeGroups.add(routeGroup);
		}

		listener.routesUpdated(routeGroups);

	}

	public static void handleRouteAdded(JSONObject routeJSON,
			RouteServiceListener listener) throws JSONException {
		Route route = SIORouteMapper.mapRouteFromJSON(routeJSON);
		sioIdToRouteMap.put(routeJSON.getString("_id"), route);
		route.setRouteGroup(sioIdToRouteGroupMap.get(routeJSON.get("group")));
		listener.routeAdded(route);
	}

	public static void handleRouteUpdated(JSONObject routeJSON,
			RouteServiceListener listener) throws JSONException {
		Route route = sioIdToRouteMap.get(routeJSON.getString("_id"));

		SIORouteMapper.mergeRouteBaseInfo(routeJSON, route);
		route.setRouteGroup(sioIdToRouteGroupMap.get(routeJSON
				.getString("group")));
		listener.routeUpdated(route);

	}

	public static void handleRouteRemoved(JSONObject routeJSON,
			RouteServiceListener listener) throws JSONException {
		Route route = sioIdToRouteMap.get(routeJSON.getString("_id"));
		SIORouteMapper.routeIdMap.remove(route.getId());
		listener.routeRemoved(route);
	}

	public static void handleRouteGroupAdded(JSONObject routeGroupJSON,
			RouteServiceListener listener) throws JSONException {
		RouteGroup routeGroup = SIORouteMapper
				.mapRouteGroupFromJSON(routeGroupJSON);
		sioIdToRouteGroupMap.put(routeGroupJSON.getString("_id"), routeGroup);
		listener.routeGroupAdded(routeGroup);
	}

	public static void handleRouteGroupUpdated(JSONObject data,
			RouteServiceListener listener) throws JSONException {
		RouteGroup routeGroup = sioIdToRouteGroupMap.get(data.getString("_id"));
		SIORouteMapper.mergeRouteGroupBaseInfo(routeGroup, data);
		listener.routeGroupUpdated(routeGroup);
	}

	public static void handleRouteGroupRemoved(JSONObject routeGroupJSON,
			RouteServiceListener listener) throws JSONException {
		RouteGroup routeGroup = SIORouteMapper
				.mapRouteGroupFromJSON(routeGroupJSON);
		sioIdToRouteGroupMap.remove(routeGroupJSON.getString("_id"));
		listener.routeGroupRemoved(routeGroup);

	}
}
