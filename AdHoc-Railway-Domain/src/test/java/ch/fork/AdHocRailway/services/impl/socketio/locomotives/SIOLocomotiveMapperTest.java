package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import static ch.fork.AdHocRailway.services.impl.socketio.locomotives.LocomotiveTestHelper.assertLocomotiveBase;
import static ch.fork.AdHocRailway.services.impl.socketio.locomotives.LocomotiveTestHelper.createJSONLocomotive;
import static ch.fork.AdHocRailway.services.impl.socketio.locomotives.LocomotiveTestHelper.createJSONLocomotiveGroup;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

public class SIOLocomotiveMapperTest {

	@Test
	public void mapDigitalLocomotive() throws JSONException {
		final JSONObject locomotiveJSON = createJSONLocomotive("1", "DiGital",
				5);

		final Locomotive locomotive = SIOLocomotiveMapper
				.mapLocomotiveFromJSON(locomotiveJSON);

		assertLocomotiveBase(locomotive, "1", LocomotiveType.DIGITAL);
		assertEquals(5, locomotive.getFunctions().size());
	}

	@Test
	public void mapDeltaLocomotive() throws JSONException {
		final JSONObject locomotiveJSON = createJSONLocomotive("1", "Delta", 1);

		final Locomotive locomotive = SIOLocomotiveMapper
				.mapLocomotiveFromJSON(locomotiveJSON);

		assertLocomotiveBase(locomotive, "1", LocomotiveType.DELTA);
		assertEquals(1, locomotive.getFunctions().size());

	}

	@Test
	public void mapSimulatedMfxLocomotive() throws JSONException {
		final JSONObject locomotiveJSON = createJSONLocomotive("1",
				"simulated-mfx", 9);

		final Locomotive locomotive = SIOLocomotiveMapper
				.mapLocomotiveFromJSON(locomotiveJSON);

		assertLocomotiveBase(locomotive, "1", LocomotiveType.SIMULATED_MFX);
		assertEquals(9, locomotive.getFunctions().size());
	}

	@Test
	public void mapLocomotiveGroup() throws JSONException {
		final JSONObject locomotiveJSON0 = createJSONLocomotive("1",
				"simulated-mfx", 9);
		final JSONObject locomotiveJSON1 = createJSONLocomotive("2", "digital",
				5);

		final JSONObject jsonLocomotiveGroup = createJSONLocomotiveGroup(
				"1234", Arrays.asList(locomotiveJSON0, locomotiveJSON1));

		final LocomotiveGroup locomotiveGroup = SIOLocomotiveMapper
				.mapLocomotiveGroupFromJSON(jsonLocomotiveGroup);

		LocomotiveTestHelper.assertLocomotiveGroup(locomotiveGroup, "1234", 2);

	}

	@Test
	public void mapEmptyLocomotiveGroup() throws JSONException {

		final JSONObject jsonLocomotiveGroup = createJSONLocomotiveGroup(
				"1234", new ArrayList<JSONObject>());

		final LocomotiveGroup locomotiveGroup = SIOLocomotiveMapper
				.mapLocomotiveGroupFromJSON(jsonLocomotiveGroup);
		LocomotiveTestHelper.assertLocomotiveGroup(locomotiveGroup, "1234", 0);

	}

}
