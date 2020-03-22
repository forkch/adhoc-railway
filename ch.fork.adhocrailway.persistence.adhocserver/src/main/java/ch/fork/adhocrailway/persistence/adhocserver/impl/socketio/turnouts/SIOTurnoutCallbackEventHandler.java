package ch.fork.adhocrailway.persistence.adhocserver.impl.socketio.turnouts;

import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;
import ch.fork.adhocrailway.persistence.adhocserver.util.GsonFactory;
import ch.fork.adhocrailway.services.TurnoutServiceListener;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SIOTurnoutCallbackEventHandler {

    private static Map<String, TurnoutGroup> sioIdToTurnoutGroupMap = new HashMap<String, TurnoutGroup>();
    private static Map<String, Turnout> sioIdToTurnoutMap = new HashMap<String, Turnout>();
    private static Gson gson = GsonFactory.createGson();

    public static void handleTurnoutInit(final JSONArray turnoutGroupsJ,
                                         final TurnoutServiceListener listener) throws JSONException {
        final SortedSet<TurnoutGroup> turnoutGroups = new TreeSet<TurnoutGroup>();
        for (int i = 0; i < turnoutGroupsJ.length(); i++) {
            final JSONObject turnoutGroupJSON = turnoutGroupsJ.getJSONObject(i);
            TurnoutGroup turnoutGroup = deserializeTurnoutGroup(turnoutGroupJSON);

            sioIdToTurnoutGroupMap.put(turnoutGroup.getId(),
                    turnoutGroup);

            for (final Turnout turnout : turnoutGroup.getTurnouts()) {
                sioIdToTurnoutMap.put(turnout.getId(),
                        turnout);
                turnout.setTurnoutGroup(turnoutGroup);
            }
            turnoutGroups.add(turnoutGroup);
        }

        listener.turnoutsUpdated(turnoutGroups);

    }


    public static void handleTurnoutAdded(final JSONObject turnoutJSON,
                                          final TurnoutServiceListener listener) throws JSONException {
        final Turnout turnout = deserializeTurnout(turnoutJSON);
        sioIdToTurnoutMap.put(turnout.getId(), turnout);
//        final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
//                .get(turnout.getGroupId());
//        turnoutGroup.addTurnout(turnout);
        turnout.setTurnoutGroup(null);
        listener.turnoutAdded(turnout);
    }


    public static void handleTurnoutUpdated(final JSONObject turnoutJSON,
                                            final TurnoutServiceListener listener) throws JSONException {
        final Turnout turnout = deserializeTurnout(turnoutJSON);
//        TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap.get(turnout.getGroupId());
//        turnoutGroup.addTurnout(turnout);

        turnout.setTurnoutGroup(null);
        listener.turnoutUpdated(turnout);

    }

    public static void handleTurnoutRemoved(final JSONObject turnoutJSON,
                                            final TurnoutServiceListener listener) throws JSONException {

        final Turnout turnout = deserializeTurnout(turnoutJSON);

//        final TurnoutGroup turnoutGroup = sioIdToTurnoutGroupMap
//                .get(turnout.getGroupId());
//        turnoutGroup.removeTurnout(turnout);
        turnout.setTurnoutGroup(null);
        listener.turnoutRemoved(turnout);

    }

    public static void handleTurnoutGroupAdded(
            final JSONObject turnoutGroupJSON,
            final TurnoutServiceListener listener) throws JSONException {
        final TurnoutGroup turnoutGroup = deserializeTurnoutGroup(turnoutGroupJSON);
        sioIdToTurnoutGroupMap.put(turnoutGroup.getId(),
                turnoutGroup);
        listener.turnoutGroupAdded(turnoutGroup);
    }

    public static void handleTurnoutGroupUpdated(
            final JSONObject turnoutGroupJSON,
            final TurnoutServiceListener listener) throws JSONException {
        final TurnoutGroup turnoutGroup = deserializeTurnoutGroup(turnoutGroupJSON);
        sioIdToTurnoutGroupMap.put(turnoutGroup.getId(),
                turnoutGroup);
        listener.turnoutGroupUpdated(turnoutGroup);
    }

    public static void handleTurnoutGroupRemoved(
            final JSONObject turnoutGroupJSON,
            final TurnoutServiceListener listener) throws JSONException {
        final TurnoutGroup turnoutGroup = deserializeTurnoutGroup(turnoutGroupJSON);
        sioIdToTurnoutGroupMap.remove(turnoutGroup.getId());
        listener.turnoutGroupRemoved(turnoutGroup);
    }

    public static Turnout getTurnoutBySIOId(final String sioId) {
        return sioIdToTurnoutMap.get(sioId);
    }

    private static TurnoutGroup deserializeTurnoutGroup(JSONObject turnoutGroupJSON) {
        return gson.fromJson(turnoutGroupJSON.toString(), TurnoutGroup.class);
    }

    private static Turnout deserializeTurnout(JSONObject turnoutJSON) {
        return gson.fromJson(turnoutJSON.toString(), Turnout.class);
    }
}
