package ch.fork.AdHocRailway.services.impl.socketio.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import ch.fork.AdHocRailway.utils.GsonFactory;
import com.google.gson.Gson;
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
    private static Gson gson = GsonFactory.createGson();

    public static void handleTurnoutInit(final JSONArray turnoutGroupsJ,
                                         final TurnoutServiceListener listener) throws JSONException {
        final SortedSet<TurnoutGroup> turnoutGroups = new TreeSet<TurnoutGroup>();
        for (int i = 0; i < turnoutGroupsJ.length(); i++) {
            final JSONObject turnoutGroupJSON = turnoutGroupsJ.getJSONObject(i);
            //final TurnoutGroup turnoutGroup = SIOTurnoutMapper
            //        .mapTurnoutGroupFromJSON(turnoutGroupJSON);
            String s = turnoutGroupJSON.toString();
            TurnoutGroup turnoutGroup = gson.fromJson(s, TurnoutGroup.class);

            sioIdToTurnoutGroupMap.put(turnoutGroup.getId(),
                    turnoutGroup);

            for (final Turnout turnout : turnoutGroup.getTurnouts()) {
                sioIdToTurnoutMap.put(turnout.getId(),
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
        sioIdToTurnoutMap.put(turnoutJSON.getString("id"), turnout);
        final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
                .get(turnoutJSON.get("groupId"));
        turnout.setTurnoutGroup(turnoutGroup);
        turnoutGroup.addTurnout(turnout);
        listener.turnoutAdded(turnout);
    }

    public static void handleTurnoutUpdated(final JSONObject turnoutJSON,
                                            final TurnoutServiceListener listener) throws JSONException {
        final Turnout turnout = sioIdToTurnoutMap.get(turnoutJSON
                .getString("id"));

        SIOTurnoutMapper.mergeTurnoutBaseInfo(turnoutJSON, turnout);
        turnout.setTurnoutGroup(sioIdToTurnoutGroupMap.get(turnoutJSON
                .getString("groupId")));
        listener.turnoutUpdated(turnout);

    }

    public static void handleTurnoutRemoved(final JSONObject turnoutJSON,
                                            final TurnoutServiceListener listener) throws JSONException {

        final Turnout turnout = sioIdToTurnoutMap.remove(turnoutJSON
                .getString("id"));

        final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
                .get(turnoutJSON.get("groupId"));
        turnoutGroup.removeTurnout(turnout);
        listener.turnoutRemoved(turnout);

    }

    public static void handleTurnoutGroupAdded(
            final JSONObject turnoutGroupJSON,
            final TurnoutServiceListener listener) throws JSONException {
        final TurnoutGroup turnoutGroup = SIOTurnoutMapper
                .mapTurnoutGroupFromJSON(turnoutGroupJSON);
        sioIdToTurnoutGroupMap.put(turnoutGroupJSON.getString("id"),
                turnoutGroup);
        listener.turnoutGroupAdded(turnoutGroup);
    }

    public static void handleTurnoutGroupUpdated(
            final JSONObject turnoutGroupJSON,
            final TurnoutServiceListener listener) throws JSONException {
        final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
                .get(turnoutGroupJSON.getString("id"));
        SIOTurnoutMapper.mergeTurnoutGroupBaseInfo(turnoutGroup,
                turnoutGroupJSON);
        listener.turnoutGroupUpdated(turnoutGroup);
    }

    public static void handleTurnoutGroupRemoved(
            final JSONObject turnoutGroupJSON,
            final TurnoutServiceListener listener) throws JSONException {
        final TurnoutGroup turnoutGroup = SIOTurnoutMapper
                .mapTurnoutGroupFromJSON(turnoutGroupJSON);
        sioIdToTurnoutGroupMap.remove(turnoutGroupJSON.getString("id"));
        SIOTurnoutMapper.turnoutGroupIdMap.remove(turnoutGroup.getId());
        listener.turnoutGroupRemoved(turnoutGroup);
    }

    public static Turnout getTurnoutBySIOId(final String sioId) {
        return sioIdToTurnoutMap.get(sioId);
    }


    public static void addIdToTurnout(final Turnout turnout, final String sioId) {
        final String id = sioId.toString();
        turnout.setId(id);
        sioIdToTurnoutMap.put(sioId, turnout);
    }

    public static void addIdToTurnoutGroup(final TurnoutGroup group,
                                           final String sioId) {
        final String id = sioId.toString();
        group.setId(id);
        sioIdToTurnoutGroupMap.put(sioId, group);
        SIOTurnoutMapper.turnoutGroupIdMap.put(id, sioId);
    }
}
