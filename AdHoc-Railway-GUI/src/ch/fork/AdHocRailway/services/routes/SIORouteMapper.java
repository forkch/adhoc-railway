package ch.fork.AdHocRailway.services.routes;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.services.turnouts.SIOTurnoutServiceEventHandler;

public class SIORouteMapper {

	public static Map<Integer, String> routeIdMap = new HashMap<Integer, String>();
	public static Map<Integer, String> routeGroupIdMap = new HashMap<Integer, String>();

	public static RouteGroup mapRouteGroupFromJSON(JSONObject routeGroupJSON)
			throws JSONException {
		RouteGroup routeGroup = new RouteGroup();
		String sioId = routeGroupJSON.getString("_id");
		int id = sioId.hashCode();
		routeGroupIdMap.put(id, sioId);
		routeGroup.setId(id);

		mergeRouteGroupBaseInfo(routeGroup, routeGroupJSON);

		if (routeGroupJSON.has("routes")) {

			JSONObject routesJSON = routeGroupJSON.getJSONObject("routes");
			String[] routeIds = JSONObject.getNames(routesJSON);

			if (routeIds != null) {
				for (String routeId : routeIds) {
					Route route = mapRouteFromJSON(routesJSON
							.getJSONObject(routeId));
					route.setRouteGroup(routeGroup);
					routeGroup.addRoute(route);
				}
			}

		}
		return routeGroup;
	}

	public static Route mapRouteFromJSON(JSONObject routeJSON)
			throws JSONException {
		Route route = new Route();
		String sioId = routeJSON.getString("_id");
		int id = sioId.hashCode();
		routeIdMap.put(id, sioId);
		route.setId(id);
		mergeRouteBaseInfo(routeJSON, route);
		return route;
	}

	public static JSONObject mapRouteGroupToJSON(RouteGroup routeGroup)
			throws JSONException {
		JSONObject routeGroupJSON = new JSONObject();
		routeGroupJSON.put("name", routeGroup.getName());
		return routeGroupJSON;
	}

	public static JSONObject mapRouteToJSON(Route route) throws JSONException {
		JSONObject routeJSON = new JSONObject();
		routeJSON.put("_id", routeIdMap.get(route.getId()));
		routeJSON.put("number", route.getNumber());
		routeJSON.put("name", route.getName());
		routeJSON.put("orientation", route.getOrientation());
		routeJSON.put("group",
				routeGroupIdMap.get(route.getRouteGroup().getId()));
		JSONArray routedTurnouts = new JSONArray();
		for (RouteItem routeItem : route.getRouteItems()) {
			JSONObject routedTurnout = new JSONObject();
			routedTurnout.put("turnoutId", SIOTurnoutServiceEventHandler
					.getSIOIdByTurnout(routeItem.getTurnout()));
			routedTurnout.put("state", routeItem.getRoutedState().toString()
					.toLowerCase());
			routedTurnouts.put(routedTurnout);
		}
		routeJSON.put("routedTurnouts", routedTurnouts);
		return routeJSON;
	}

	public static void mergeRouteBaseInfo(JSONObject routeJSON, Route route)
			throws JSONException {
		route.setName(routeJSON.getString("name"));
		route.setOrientation(routeJSON.getString("orientation"));
		route.setNumber(routeJSON.getInt("number"));
		if (routeJSON.has("routedTurnouts")) {
			JSONArray routedTurnouts = routeJSON.getJSONArray("routedTurnouts");
			for (int i = 0; i < routedTurnouts.length(); i++) {
				JSONObject routedTurnout = routedTurnouts.getJSONObject(i);
				RouteItem routeItem = new RouteItem();
				routeItem.setId(routedTurnout.getString("_id").hashCode());
				routeItem
						.setTurnout(SIOTurnoutServiceEventHandler
								.getTurnoutBySIOId(routedTurnout
										.getString("turnoutId")));
				routeItem.setRoutedState(TurnoutState.fromString(routedTurnout
						.getString("state")));
				route.getRouteItems().add(routeItem);
				routeItem.setRoute(route);

			}
		}
	}

	public static void mergeRouteGroupBaseInfo(RouteGroup routeGroup,
			JSONObject data) throws JSONException {
		routeGroup.setName(data.getString("name"));

	}

}
