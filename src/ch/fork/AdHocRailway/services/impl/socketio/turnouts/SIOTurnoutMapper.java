package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public class SIOTurnoutMapper {

	public static Map<Integer, String> turnoutIdMap = new HashMap<Integer, String>();
	public static Map<Integer, String> turnoutGroupIdMap = new HashMap<Integer, String>();

	public static TurnoutGroup mapTurnoutGroupFromJSON(
			JSONObject turnoutGroupJSON) throws JSONException {
		TurnoutGroup turnoutGroup = new TurnoutGroup();
		String sioId = turnoutGroupJSON.getString("_id");
		int id = sioId.hashCode();
		turnoutGroupIdMap.put(id, sioId);
		turnoutGroup.setId(id);

		mergeTurnoutGroupBaseInfo(turnoutGroup, turnoutGroupJSON);

		if (turnoutGroupJSON.has("turnouts")) {
			JSONObject turnoutsJSON = turnoutGroupJSON
					.getJSONObject("turnouts");
			String[] turnoutIds = JSONObject.getNames(turnoutsJSON);
			if (turnoutIds != null) {
				for (String turnoutId : turnoutIds) {
					Turnout turnout = mapTurnoutFromJSON(turnoutsJSON
							.getJSONObject(turnoutId));
					turnout.setTurnoutGroup(turnoutGroup);
					turnoutGroup.addTurnout(turnout);
				}
			}
		}
		return turnoutGroup;
	}

	public static Turnout mapTurnoutFromJSON(JSONObject turnoutJSON)
			throws JSONException {
		Turnout turnout = new Turnout();
		String sioId = turnoutJSON.getString("_id");
		int id = sioId.hashCode();
		turnoutIdMap.put(id, sioId);
		turnout.setId(id);
		mergeTurnoutBaseInfo(turnoutJSON, turnout);

		return turnout;
	}

	public static void mergeTurnoutBaseInfo(JSONObject turnoutJSON,
			Turnout turnout) throws JSONException {
		turnout.setNumber(turnoutJSON.getInt("number"));
		turnout.setDescription(turnoutJSON.optString("description", ""));

		turnout.setBus1(turnoutJSON.getInt("bus1"));
		turnout.setBus2(turnoutJSON.optInt("bus2", 0));
		turnout.setAddress1(turnoutJSON.getInt("address1"));
		turnout.setAddress2(turnoutJSON.optInt("address2", 0));
		turnout.setAddress1Switched(turnoutJSON.getBoolean("address1switched"));
		turnout.setAddress2Switched(turnoutJSON.getBoolean("address2switched"));

		turnout.setOrientation(TurnoutOrientation.fromString(turnoutJSON
				.getString("orientation")));
		turnout.setDefaultState(TurnoutState.fromString(turnoutJSON
				.getString("defaultState")));
		turnout.setTurnoutType(TurnoutType.fromString(turnoutJSON
				.getString("type")));
	}

	public static void mergeTurnoutGroupBaseInfo(TurnoutGroup turnoutGroup,
			JSONObject turnoutGroupJSON) throws JSONException {

		turnoutGroup.setName(turnoutGroupJSON.getString("name"));

	}

	public static JSONObject mapTurnoutGroupToJSON(TurnoutGroup group)
			throws JSONException {
		JSONObject turnoutGroupJSON = new JSONObject();
		turnoutGroupJSON.put("_id", turnoutGroupIdMap.get(group.getId()));
		turnoutGroupJSON.put("name", group.getName());
		return turnoutGroupJSON;
	}

	public static JSONObject mapTurnoutToJSON(Turnout turnout)
			throws JSONException {
		JSONObject turnoutJSON = new JSONObject();
		turnoutJSON.put("_id", turnoutIdMap.get(turnout.getId()));
		turnoutJSON.put("number", turnout.getNumber());
		turnoutJSON.put("description", turnout.getDescription());
		turnoutJSON.put("bus1", turnout.getBus1());
		turnoutJSON.put("bus2", turnout.getBus2());
		turnoutJSON.put("address1", turnout.getAddress1());
		turnoutJSON.put("address2", turnout.getAddress2());
		turnoutJSON.put("address1switched", turnout.isAddress1Switched());
		turnoutJSON.put("address2switched", turnout.isAddress2Switched());
		turnoutJSON.put("orientation", turnout.getOrientation().name()
				.toLowerCase());
		turnoutJSON.put("defaultState", turnout.getDefaultState().name()
				.toLowerCase());
		turnoutJSON.put("type", turnout.getTurnoutType().name().toLowerCase());
		turnoutJSON.put("group",
				turnoutGroupIdMap.get(turnout.getTurnoutGroup().getId()));

		return turnoutJSON;

	}

}
