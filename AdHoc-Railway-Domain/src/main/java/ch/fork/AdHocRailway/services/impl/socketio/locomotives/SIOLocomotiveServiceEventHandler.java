package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.AdHocRailway.utils.GsonFactory;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SIOLocomotiveServiceEventHandler {

    private static Map<String, LocomotiveGroup> sioIdToLocomotiveGroupMap = new HashMap<String, LocomotiveGroup>();
    private static Map<String, Locomotive> sioIdToLocomotiveMap = new HashMap<String, Locomotive>();

    private static Gson gson = GsonFactory.createGson();

    public static SortedSet<LocomotiveGroup> handleLocomotiveInit(
            final JSONArray locomotiveGroupsJSON, final LocomotiveServiceListener listener)
            throws JSONException {

        final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
        for (int i = 0; i < locomotiveGroupsJSON.length(); i++) {
            final JSONObject locomotiveGroupJSON = locomotiveGroupsJSON
                    .getJSONObject(i);
            final LocomotiveGroup locomotiveGroup = deserializeLocomotiveGroup(locomotiveGroupJSON);

            sioIdToLocomotiveGroupMap.put(locomotiveGroup.getId(),
                    locomotiveGroup);

            for (final Locomotive locomotive : locomotiveGroup.getLocomotives()) {
                sioIdToLocomotiveMap.put(locomotive.getId(), locomotive);
                locomotive.setGroup(locomotiveGroup);
                if (locomotive.getFunctions().size() == 0) {
                    locomotive.setFunctions(LocomotiveFunction.getFunctionsForType(locomotive.getType()));
                }
            }
            locomotiveGroups.add(locomotiveGroup);
        }

        listener.locomotivesUpdated(locomotiveGroups);
        return locomotiveGroups;

    }

    public static Locomotive handleLocomotiveAdded(
            final JSONObject locomotiveJSON,
            final LocomotiveServiceListener listener) throws JSONException {
        final Locomotive locomotive = deserializeLocomotive(locomotiveJSON);
        sioIdToLocomotiveMap.put(locomotive.getId(), locomotive);
        final LocomotiveGroup locomotiveGroup = sioIdToLocomotiveGroupMap
                .get(locomotive.getGroupId());
        locomotive.setGroup(locomotiveGroup);
        if (locomotive.getFunctions().size() == 0) {
            locomotive.setFunctions(LocomotiveFunction.getFunctionsForType(locomotive.getType()));
        }
        locomotiveGroup.addLocomotive(locomotive);
        listener.locomotiveAdded(locomotive);
        return locomotive;
    }

    public static Locomotive handleLocomotiveUpdated(
            final JSONObject locomotiveJSON,
            final LocomotiveServiceListener listener) throws JSONException {
        final Locomotive locomotive = deserializeLocomotive(locomotiveJSON);

        locomotive.setGroup(sioIdToLocomotiveGroupMap.get(locomotive.getGroupId()));
        if (locomotive.getFunctions().size() == 0) {
            locomotive.setFunctions(LocomotiveFunction.getFunctionsForType(locomotive.getType()));
        }
        listener.locomotiveUpdated(locomotive);
        return locomotive;

    }

    public static Locomotive handleLocomotiveRemoved(
            final JSONObject locomotiveJSON,
            final LocomotiveServiceListener listener) throws JSONException {
        final Locomotive locomotive = deserializeLocomotive(locomotiveJSON);

        final LocomotiveGroup locomotiveGroup = sioIdToLocomotiveGroupMap
                .get(locomotive.getGroupId());
        locomotiveGroup.removeLocomotive(locomotive);
        listener.locomotiveRemoved(locomotive);

        return locomotive;
    }

    public static LocomotiveGroup handleLocomotiveGroupAdded(
            final JSONObject locomotiveGroupJSON,
            final LocomotiveServiceListener listener) throws JSONException {
        final LocomotiveGroup locomotiveGroup = deserializeLocomotiveGroup(locomotiveGroupJSON);
        sioIdToLocomotiveGroupMap.put(locomotiveGroup.getId(),
                locomotiveGroup);
        listener.locomotiveGroupAdded(locomotiveGroup);
        return locomotiveGroup;
    }

    public static LocomotiveGroup handleLocomotiveGroupUpdated(
            final JSONObject locomotiveGroupJSON,
            final LocomotiveServiceListener listener) throws JSONException {
        final LocomotiveGroup locomotiveGroup = deserializeLocomotiveGroup(locomotiveGroupJSON);
        sioIdToLocomotiveGroupMap.put(locomotiveGroup.getId(), locomotiveGroup);
        listener.locomotiveGroupUpdated(locomotiveGroup);
        return locomotiveGroup;
    }

    public static LocomotiveGroup handleLocomotiveGroupRemoved(
            final JSONObject locomotiveGroupJSON,
            final LocomotiveServiceListener listener) throws JSONException {
        final LocomotiveGroup locomotiveGroup = deserializeLocomotiveGroup(locomotiveGroupJSON);
        sioIdToLocomotiveGroupMap.remove(locomotiveGroup.getId());
        listener.locomotiveGroupRemoved(locomotiveGroup);
        return locomotiveGroup;
    }

    private static LocomotiveGroup deserializeLocomotiveGroup(JSONObject locomotiveGroupJSON) {
        return gson.fromJson(locomotiveGroupJSON.toString(), LocomotiveGroup.class);
    }

    private static Locomotive deserializeLocomotive(JSONObject locomotiveJSON) {
        return gson.fromJson(locomotiveJSON.toString(), Locomotive.class);
    }
}
