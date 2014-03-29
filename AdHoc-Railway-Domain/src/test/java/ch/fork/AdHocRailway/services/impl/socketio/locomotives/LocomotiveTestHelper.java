package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LocomotiveTestHelper {

    public static final String GROUP_NAME = "groupName";
    public static final int ADDRESS_2 = 2;
    public static final int ADDRESS_1 = 1;
    public static final int BUS = 1;
    public static final String IMAGE_PNG = "someImage.png";
    public static final String DESCRIPTION = "testDesc";
    public static final String NAME = "testName";

    public static JSONObject createJSONLocomotiveGroup(final String id,
                                                       final List<JSONObject> locomotivesJSON) throws JSONException {

        final JSONObject locomotiveGroupJSON = new JSONObject();
        locomotiveGroupJSON.put("_id", id);
        locomotiveGroupJSON.put("name", GROUP_NAME + id);

        if (locomotivesJSON.size() == 0) {
            return locomotiveGroupJSON;
        }
        final JSONObject locomotives = new JSONObject();
        for (final JSONObject locomotiveJSON : locomotivesJSON) {
            locomotiveJSON.put("group", id);
            locomotives.put(locomotiveJSON.getString("_id"), locomotiveJSON);
        }
        locomotiveGroupJSON.put("locomotives", locomotives);
        return locomotiveGroupJSON;
    }

    public static JSONObject createJSONLocomotive(final String id,
                                                  final String type, final int functionCount) throws JSONException {
        final JSONObject locomotiveJSON = new JSONObject();
        locomotiveJSON.put("_id", id);
        locomotiveJSON.put("name", NAME + id);
        locomotiveJSON.put("description", DESCRIPTION);
        locomotiveJSON.put("image", IMAGE_PNG);
        locomotiveJSON.put("bus", BUS);
        locomotiveJSON.put("address1", ADDRESS_1);
        locomotiveJSON.put("address2", ADDRESS_2);
        locomotiveJSON.put("type", type);

        final List<JSONObject> functions = new ArrayList<JSONObject>();
        for (int i = 0; i < functionCount; i++) {
            final JSONObject function = new JSONObject();
            function.put("number", i);
            function.put("description", "desc" + i);
            function.put("emergencyBrakeFunction", i % 2 == 0);
            function.put("deactivationDelay", i);
            functions.add(function);
        }
        locomotiveJSON.put("functions", functions);
        return locomotiveJSON;
    }

    public static void assertLocomotiveBase(final Locomotive locomotive,
                                            final String id, final LocomotiveType type) {
        assertEquals(id, locomotive.getId());
        assertEquals(NAME + id, locomotive.getName());
        assertEquals(DESCRIPTION, locomotive.getDesc());
        assertEquals(IMAGE_PNG, locomotive.getImage());
        assertEquals(BUS, locomotive.getBus());
        assertEquals(ADDRESS_1, locomotive.getAddress1());
        assertEquals(ADDRESS_2, locomotive.getAddress2());
        assertEquals(type, locomotive.getType());
    }

    public static void assertLocomotiveGroup(
            final LocomotiveGroup locomotiveGroup, final String id,
            final int locomotiveCount) {

        assertEquals(id, locomotiveGroup.getId());
        assertEquals(GROUP_NAME + id, locomotiveGroup.getName());
        assertEquals(locomotiveCount, locomotiveGroup.getLocomotives().size());
    }
}
