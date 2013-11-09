package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SIOTurnoutServiceEventHandler {

	private static Map<String, TurnoutGroup> sioIdToTurnoutGroupMap = new HashMap<String, TurnoutGroup>();
	private static Map<String, Turnout> sioIdToTurnoutMap = new HashMap<String, Turnout>();

	public static void handleTurnoutInit(final JSONObject data,
			final TurnoutServiceListener listener) throws JSONException {
		final JSONArray turnoutGroupsJ = data.getJSONArray("turnoutGroups");
		final SortedSet<TurnoutGroup> turnoutGroups = new TreeSet<TurnoutGroup>();
		for (int i = 0; i < turnoutGroupsJ.length(); i++) {
			final JSONObject turnoutGroupJSON = turnoutGroupsJ.getJSONObject(i);
			final TurnoutGroup turnoutGroup = SIOTurnoutMapper
					.mapTurnoutGroupFromJSON(turnoutGroupJSON);

			sioIdToTurnoutGroupMap.put(turnoutGroupJSON.getString("_id"),
					turnoutGroup);

			for (final Turnout turnout : turnoutGroup.getTurnouts()) {
				sioIdToTurnoutMap.put(
						SIOTurnoutMapper.turnoutIdMap.get(turnout.getId()),
						turnout);
			}
			turnoutGroups.add(turnoutGroup);
		}

		listener.turnoutsUpdated(turnoutGroups);

	}

	public static void handleTurnoutAdded(final JSONObject turnoutJSON,
			final TurnoutServiceListener listener) throws JSONException {
		final Turnout turnout = SIOTurnoutMapper
				.mapTurnoutFromJSON(turnoutJSON);
		sioIdToTurnoutMap.put(turnoutJSON.getString("_id"), turnout);
		final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
				.get(turnoutJSON.get("group"));
		turnout.setTurnoutGroup(turnoutGroup);
		turnoutGroup.addTurnout(turnout);
		listener.turnoutAdded(turnout);
	}

	public static void handleTurnoutUpdated(final JSONObject turnoutJSON,
			final TurnoutServiceListener listener) throws JSONException {
		final Turnout turnout = sioIdToTurnoutMap.get(turnoutJSON
				.getString("_id"));

		SIOTurnoutMapper.mergeTurnoutBaseInfo(turnoutJSON, turnout);
		turnout.setTurnoutGroup(sioIdToTurnoutGroupMap.get(turnoutJSON
				.getString("group")));
		listener.turnoutUpdated(turnout);

	}

	public static void handleTurnoutRemoved(final JSONObject turnoutJSON,
			final TurnoutServiceListener listener) throws JSONException {
		final Turnout turnout = sioIdToTurnoutMap.remove(turnoutJSON
				.getString("_id"));
		SIOTurnoutMapper.turnoutIdMap.remove(turnout.getId());

		final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
				.get(turnoutJSON.get("group"));
		turnoutGroup.removeTurnout(turnout);
		listener.turnoutRemoved(turnout);

	}

	public static void handleTurnoutGroupAdded(
			final JSONObject turnoutGroupJSON,
			final TurnoutServiceListener listener) throws JSONException {
		final TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		sioIdToTurnoutGroupMap.put(turnoutGroupJSON.getString("_id"),
				turnoutGroup);
		listener.turnoutGroupAdded(turnoutGroup);
	}

	public static void handleTurnoutGroupUpdated(
			final JSONObject turnoutGroupJSON,
			final TurnoutServiceListener listener) throws JSONException {
		final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
				.get(turnoutGroupJSON.getString("_id"));
		SIOTurnoutMapper.mergeTurnoutGroupBaseInfo(turnoutGroup,
				turnoutGroupJSON);
		listener.turnoutGroupUpdated(turnoutGroup);
	}

	public static void handleTurnoutGroupRemoved(
			final JSONObject turnoutGroupJSON,
			final TurnoutServiceListener listener) throws JSONException {
		final TurnoutGroup turnoutGroup = SIOTurnoutMapper
				.mapTurnoutGroupFromJSON(turnoutGroupJSON);
		sioIdToTurnoutGroupMap.remove(turnoutGroupJSON.getString("_id"));
		SIOTurnoutMapper.turnoutGroupIdMap.remove(turnoutGroup.getId());
		listener.turnoutGroupRemoved(turnoutGroup);
	}

	public static Turnout getTurnoutBySIOId(final String sioId) {
		return sioIdToTurnoutMap.get(sioId);
	}

	public static String getSIOIdByTurnout(final Turnout turnout) {
		return SIOTurnoutMapper.turnoutIdMap.get(turnout.getId());
	}

	public static void addIdToTurnout(final Turnout turnout, final String sioId) {
		final int id = sioId.hashCode();
		turnout.setId(id);
		sioIdToTurnoutMap.put(sioId, turnout);
		SIOTurnoutMapper.turnoutIdMap.put(id, sioId);
	}

	public static void addIdToTurnoutGroup(final TurnoutGroup group,
			final String sioId) {
		final int id = sioId.hashCode();
		group.setId(id);
		sioIdToTurnoutGroupMap.put(sioId, group);
		SIOTurnoutMapper.turnoutGroupIdMap.put(id, sioId);
	}
}
