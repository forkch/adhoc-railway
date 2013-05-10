package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveServiceListener;

public class SIOLocomotiveServiceEventHandler {

	private static Map<String, LocomotiveGroup> sioIdToLocomotiveGroupMap = new HashMap<String, LocomotiveGroup>();
	private static Map<String, Locomotive> sioIdToLocomotiveMap = new HashMap<String, Locomotive>();

	public static SortedSet<LocomotiveGroup> handleLocomotiveInit(
			final JSONObject data, final LocomotiveServiceListener listener)
			throws JSONException {
		final JSONArray locomotiveGroupsJ = data
				.getJSONArray("locomotiveGroups");
		final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
		for (int i = 0; i < locomotiveGroupsJ.length(); i++) {
			final JSONObject locomotiveGroupJSON = locomotiveGroupsJ
					.getJSONObject(i);
			final LocomotiveGroup locomotiveGroup = SIOLocomotiveMapper
					.mapLocomotiveGroupFromJSON(locomotiveGroupJSON);

			sioIdToLocomotiveGroupMap.put(locomotiveGroupJSON.getString("_id"),
					locomotiveGroup);

			for (final Locomotive locomotive : locomotiveGroup.getLocomotives()) {
				sioIdToLocomotiveMap.put(SIOLocomotiveMapper.locomotiveIdMap
						.get(locomotive.getId()), locomotive);
			}
			locomotiveGroups.add(locomotiveGroup);
		}

		listener.locomotivesUpdated(locomotiveGroups);
		return locomotiveGroups;

	}

	public static Locomotive handleLocomotiveAdded(
			final JSONObject locomotiveJSON,
			final LocomotiveServiceListener listener) throws JSONException {
		final Locomotive locomotive = SIOLocomotiveMapper
				.mapLocomotiveFromJSON(locomotiveJSON);
		sioIdToLocomotiveMap.put(locomotiveJSON.getString("_id"), locomotive);
		final LocomotiveGroup locomotiveGroup = sioIdToLocomotiveGroupMap
				.get(locomotiveJSON.get("group"));
		locomotive.setGroup(locomotiveGroup);
		locomotiveGroup.addLocomotive(locomotive);
		listener.locomotiveAdded(locomotive);
		return locomotive;
	}

	public static Locomotive handleLocomotiveUpdated(
			final JSONObject locomotiveJSON,
			final LocomotiveServiceListener listener) throws JSONException {
		final Locomotive locomotive = sioIdToLocomotiveMap.get(locomotiveJSON
				.getString("_id"));

		SIOLocomotiveMapper.mergeLocomotiveBaseInfo(locomotiveJSON, locomotive);
		locomotive.setGroup(sioIdToLocomotiveGroupMap.get(locomotiveJSON
				.getString("group")));
		listener.locomotiveUpdated(locomotive);
		return locomotive;

	}

	public static Locomotive handleLocomotiveRemoved(
			final JSONObject locomotiveJSON,
			final LocomotiveServiceListener listener) throws JSONException {
		final Locomotive locomotive = sioIdToLocomotiveMap
				.remove(locomotiveJSON.getString("_id"));
		SIOLocomotiveMapper.locomotiveIdMap.remove(locomotive.getId());

		final LocomotiveGroup locomotiveGroup = sioIdToLocomotiveGroupMap
				.get(locomotiveJSON.get("group"));
		locomotiveGroup.removeLocomotive(locomotive);
		listener.locomotiveRemoved(locomotive);

		return locomotive;
	}

	public static LocomotiveGroup handleLocomotiveGroupAdded(
			final JSONObject locomotiveGroupJSON,
			final LocomotiveServiceListener listener) throws JSONException {
		final LocomotiveGroup locomotiveGroup = SIOLocomotiveMapper
				.mapLocomotiveGroupFromJSON(locomotiveGroupJSON);
		sioIdToLocomotiveGroupMap.put(locomotiveGroupJSON.getString("_id"),
				locomotiveGroup);
		listener.locomotiveGroupAdded(locomotiveGroup);
		return locomotiveGroup;
	}

	public static LocomotiveGroup handleLocomotiveGroupUpdated(
			final JSONObject locomotiveGroupJSON,
			final LocomotiveServiceListener listener) throws JSONException {
		final LocomotiveGroup locomotiveGroup = sioIdToLocomotiveGroupMap
				.get(locomotiveGroupJSON.getString("_id"));
		SIOLocomotiveMapper.mergeLocomotiveGroupBaseInfo(locomotiveGroup,
				locomotiveGroupJSON);
		listener.locomotiveGroupUpdated(locomotiveGroup);
		return locomotiveGroup;
	}

	public static LocomotiveGroup handleLocomotiveGroupRemoved(
			final JSONObject locomotiveGroupJSON,
			final LocomotiveServiceListener listener) throws JSONException {
		final LocomotiveGroup locomotiveGroup = SIOLocomotiveMapper
				.mapLocomotiveGroupFromJSON(locomotiveGroupJSON);
		sioIdToLocomotiveGroupMap.remove(locomotiveGroupJSON.getString("_id"));
		SIOLocomotiveMapper.locomotiveGroupIdMap
				.remove(locomotiveGroup.getId());
		listener.locomotiveGroupRemoved(locomotiveGroup);
		return locomotiveGroup;
	}

	public static Locomotive getLocomotiveBySIOId(final String sioId) {
		return sioIdToLocomotiveMap.get(sioId);
	}

	public static String getSIOIdByLocomotive(final Locomotive locomotive) {
		return SIOLocomotiveMapper.locomotiveIdMap.get(locomotive.getId());
	}

	public static void addIdToLocomotive(final Locomotive locomotive,
			final String sioId) {
		final int id = sioId.hashCode();
		locomotive.setId(id);
		sioIdToLocomotiveMap.put(sioId, locomotive);
		SIOLocomotiveMapper.locomotiveIdMap.put(id, sioId);
	}

	public static void addIdToLocomotiveGroup(final LocomotiveGroup group,
			final String sioId) {
		final int id = sioId.hashCode();
		group.setId(id);
		sioIdToLocomotiveGroupMap.put(sioId, group);
		SIOLocomotiveMapper.locomotiveGroupIdMap.put(id, sioId);
	}
}
