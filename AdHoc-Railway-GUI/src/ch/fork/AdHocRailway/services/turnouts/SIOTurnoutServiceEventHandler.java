package ch.fork.AdHocRailway.services.turnouts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

public class SIOTurnoutServiceEventHandler {

	private static Map<String, TurnoutGroup> idToTurnoutGroupMap = new HashMap<String, TurnoutGroup>();

	public static void handleTurnoutAdded(JSONObject turnoutJSON,
			TurnoutServiceListener listener) throws JSONException {
		Turnout turnout = SIOTurnoutMapper.mapTurnoutFromJSON(turnoutJSON);
		turnout.setTurnoutGroup(idToTurnoutGroupMap.get(turnoutJSON
				.get("group")));
		listener.turnoutAdded(turnout);

	}

	public static void handleTurnoutUpdated(JSONObject turnoutJSON,
			TurnoutServiceListener listener) throws JSONException {
		Turnout turnout = SIOTurnoutMapper.mapTurnoutFromJSON(turnoutJSON);
		turnout.setTurnoutGroup(idToTurnoutGroupMap.get(turnoutJSON
				.getString("group")));
		listener.turnoutUpdated(turnout);

	}

	public static void handleTurnoutRemoved(JSONObject turnoutJSON,
			TurnoutServiceListener listener) throws JSONException {
		Turnout turnout = SIOTurnoutMapper.mapTurnoutFromJSON(turnoutJSON);
		turnout.setTurnoutGroup(idToTurnoutGroupMap.get(turnoutJSON
				.getString("group")));
		SIOTurnoutMapper.turnoutIdMap.remove(turnout.getId());
		listener.turnoutRemoved(turnout);

	}

	public static void handleTurnoutGroupAdded(JSONObject turnoutGroupJSON,
			TurnoutServiceListener listener) throws JSONException {
		TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		idToTurnoutGroupMap
				.put(turnoutGroupJSON.getString("_id"), turnoutGroup);
		listener.turnoutGroupAdded(turnoutGroup);

	}

	public static void handleTurnoutGroupUpdated(JSONObject turnoutGroupJSON,
			TurnoutServiceListener listener) throws JSONException {
		TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		idToTurnoutGroupMap
				.put(turnoutGroupJSON.getString("_id"), turnoutGroup);
		listener.turnoutGroupUpdated(turnoutGroup);
	}

	public static void handleTurnoutGroupRemoved(JSONObject turnoutGroupJSON,
			TurnoutServiceListener listener) throws JSONException {
		TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		idToTurnoutGroupMap.remove(turnoutGroupJSON.getString("_id"));
		SIOTurnoutMapper.turnoutGroupIdMap.remove(turnoutGroup.getId());
		listener.turnoutGroupRemoved(turnoutGroup);
	}

	public static void handleTurnoutInit(JSONObject data,
			TurnoutServiceListener listener) throws JSONException {
		JSONArray turnoutGroupsJ = data.getJSONArray("turnoutGroups");
		List<TurnoutGroup> turnoutGroups = new LinkedList<TurnoutGroup>();
		for (int i = 0; i < turnoutGroupsJ.length(); i++) {
			JSONObject turnoutGroupJSON = turnoutGroupsJ.getJSONObject(i);
			TurnoutGroup turnoutGroup = SIOTurnoutMapper
					.mapTurnoutGroupFromJSON(turnoutGroupJSON);

			idToTurnoutGroupMap.put(turnoutGroupJSON.getString("_id"),
					turnoutGroup);
			turnoutGroups.add(turnoutGroup);
		}

		listener.turnoutsUpdated(turnoutGroups);

	}

}
