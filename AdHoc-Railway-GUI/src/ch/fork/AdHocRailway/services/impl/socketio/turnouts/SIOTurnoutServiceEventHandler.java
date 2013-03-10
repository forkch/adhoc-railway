package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;

public class SIOTurnoutServiceEventHandler {

	private static Map<String, TurnoutGroup> sioIdToTurnoutGroupMap = new HashMap<String, TurnoutGroup>();
	private static Map<String, Turnout> sioIdToTurnoutMap = new HashMap<String, Turnout>();

	public static void handleTurnoutInit(JSONObject data,
			TurnoutServiceListener listener) throws JSONException {
		JSONArray turnoutGroupsJ = data.getJSONArray("turnoutGroups");
		List<TurnoutGroup> turnoutGroups = new LinkedList<TurnoutGroup>();
		for (int i = 0; i < turnoutGroupsJ.length(); i++) {
			JSONObject turnoutGroupJSON = turnoutGroupsJ.getJSONObject(i);
			TurnoutGroup turnoutGroup = SIOTurnoutMapper
					.mapTurnoutGroupFromJSON(turnoutGroupJSON);

			sioIdToTurnoutGroupMap.put(turnoutGroupJSON.getString("_id"),
					turnoutGroup);

			for (Turnout turnout : turnoutGroup.getTurnouts()) {
				sioIdToTurnoutMap.put(
						SIOTurnoutMapper.turnoutIdMap.get(turnout.getId()),
						turnout);
			}
			turnoutGroups.add(turnoutGroup);
		}

		listener.turnoutsUpdated(turnoutGroups);

	}

	public static void handleTurnoutAdded(JSONObject turnoutJSON,
			TurnoutServiceListener listener) throws JSONException {
		Turnout turnout = SIOTurnoutMapper.mapTurnoutFromJSON(turnoutJSON);
		sioIdToTurnoutMap.put(turnoutJSON.getString("_id"), turnout);
		TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap.get(turnoutJSON
				.get("group"));
		turnout.setTurnoutGroup(turnoutGroup);
		turnoutGroup.addTurnout(turnout);
		listener.turnoutAdded(turnout);
	}

	public static void handleTurnoutUpdated(JSONObject turnoutJSON,
			TurnoutServiceListener listener) throws JSONException {
		Turnout turnout = sioIdToTurnoutMap.get(turnoutJSON.getString("_id"));

		SIOTurnoutMapper.mergeTurnoutBaseInfo(turnoutJSON, turnout);
		turnout.setTurnoutGroup(sioIdToTurnoutGroupMap.get(turnoutJSON
				.getString("group")));
		listener.turnoutUpdated(turnout);

	}

	public static void handleTurnoutRemoved(JSONObject turnoutJSON,
			TurnoutServiceListener listener) throws JSONException {
		Turnout turnout = sioIdToTurnoutMap
				.remove(turnoutJSON.getString("_id"));
		SIOTurnoutMapper.turnoutIdMap.remove(turnout.getId());

		TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap.get(turnoutJSON
				.get("group"));
		turnoutGroup.removeTurnout(turnout);
		listener.turnoutRemoved(turnout);

	}

	public static void handleTurnoutGroupAdded(JSONObject turnoutGroupJSON,
			TurnoutServiceListener listener) throws JSONException {
		TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		sioIdToTurnoutGroupMap.put(turnoutGroupJSON.getString("_id"),
				turnoutGroup);
		listener.turnoutGroupAdded(turnoutGroup);
	}

	public static void handleTurnoutGroupUpdated(JSONObject turnoutGroupJSON,
			TurnoutServiceListener listener) throws JSONException {
		TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap.get(turnoutGroupJSON
				.getString("_id"));
		SIOTurnoutMapper.mergeTurnoutGroupBaseInfo(turnoutGroup,
				turnoutGroupJSON);
		listener.turnoutGroupUpdated(turnoutGroup);
	}

	public static void handleTurnoutGroupRemoved(JSONObject turnoutGroupJSON,
			TurnoutServiceListener listener) throws JSONException {
		TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		sioIdToTurnoutGroupMap.remove(turnoutGroupJSON.getString("_id"));
		SIOTurnoutMapper.turnoutGroupIdMap.remove(turnoutGroup.getId());
		listener.turnoutGroupRemoved(turnoutGroup);
	}

	public static Turnout getTurnoutBySIOId(String sioId) {
		return sioIdToTurnoutMap.get(sioId);
	}

	public static String getSIOIdByTurnout(Turnout turnout) {
		return SIOTurnoutMapper.turnoutIdMap.get(turnout.getId());
	}

	public static void addIdToTurnout(Turnout turnout, String sioId) {
		int id = sioId.hashCode();
		turnout.setId(id);
		sioIdToTurnoutMap.put(sioId, turnout);
		SIOTurnoutMapper.turnoutIdMap.put(id, sioId);
	}

	public static void addIdToTurnoutGroup(TurnoutGroup group, String sioId) {
		int id = sioId.hashCode();
		group.setId(id);
		sioIdToTurnoutGroupMap.put(sioId, group);
		SIOTurnoutMapper.turnoutGroupIdMap.put(id, sioId);
	}
}
