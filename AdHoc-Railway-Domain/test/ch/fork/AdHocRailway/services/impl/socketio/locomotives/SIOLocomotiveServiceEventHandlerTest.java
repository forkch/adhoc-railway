package ch.fork.AdHocRailway.services.impl.socketio.locomotives;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.domain.utils.Tuple;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveServiceListener;

public class SIOLocomotiveServiceEventHandlerTest {

	private static final String GROUP_ID = "1234";
	private static final String GROUP_ID2 = "12342";
	@Mock
	private LocomotiveServiceListener listenerMock;

	@Captor
	private final ArgumentCaptor<SortedSet<LocomotiveGroup>> locomotiveGroupsCaptor = null;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void locomotiveInitEvent() throws JSONException {
		final JSONObject createJSONLocomotive = LocomotiveTestHelper
				.createJSONLocomotive("1", "digital", 5);
		final JSONObject group1 = LocomotiveTestHelper
				.createJSONLocomotiveGroup(GROUP_ID,
						Arrays.asList(createJSONLocomotive));

		final JSONObject createJSONLocomotive1 = LocomotiveTestHelper
				.createJSONLocomotive("2", "digital", 5);
		final JSONObject group2 = LocomotiveTestHelper
				.createJSONLocomotiveGroup(GROUP_ID2,
						Arrays.asList(createJSONLocomotive1));

		final JSONObject initJSON = new JSONObject();
		initJSON.put("locomotiveGroups", Arrays.asList(group1, group2));

		SIOLocomotiveServiceEventHandler.handleLocomotiveInit(initJSON,
				listenerMock);

		verify(listenerMock).locomotivesUpdated(
				locomotiveGroupsCaptor.capture());
		final SortedSet<LocomotiveGroup> locomotiveGroups = locomotiveGroupsCaptor
				.getValue();
		assertEquals(2, locomotiveGroups.size());
	}

	@Test
	public void locomotiveGroupAddedEvent() throws JSONException {

		final ArgumentCaptor<LocomotiveGroup> captor = ArgumentCaptor
				.forClass(LocomotiveGroup.class);

		final JSONObject createJSONLocomotive = LocomotiveTestHelper
				.createJSONLocomotive("1", "digital", 5);
		final JSONObject createJSONLocomotiveGroup = LocomotiveTestHelper
				.createJSONLocomotiveGroup(GROUP_ID,
						Arrays.asList(createJSONLocomotive));

		SIOLocomotiveServiceEventHandler.handleLocomotiveGroupAdded(
				createJSONLocomotiveGroup, listenerMock);
		verify(listenerMock).locomotiveGroupAdded(captor.capture());

		final LocomotiveGroup locomotiveGroup = captor.getValue();
		assertEquals(GROUP_ID.hashCode(), locomotiveGroup.getId());
		assertEquals(1, locomotiveGroup.getLocomotives().size());
	}

	@Test
	public void locomotiveGroupRemovedEvent() throws JSONException {

		final ArgumentCaptor<LocomotiveGroup> captor = ArgumentCaptor
				.forClass(LocomotiveGroup.class);
		final JSONObject jsonEmptyLocomotiveGroup = addEmptyLocomotiveGroup()
				.getFirst();
		final LocomotiveGroup addEmptyLocomotiveGroup = addEmptyLocomotiveGroup()
				.getSecond();

		SIOLocomotiveServiceEventHandler.handleLocomotiveGroupRemoved(
				jsonEmptyLocomotiveGroup, listenerMock);

		verify(listenerMock).locomotiveGroupRemoved(captor.capture());
		final LocomotiveGroup locomotiveGroup = captor.getValue();
		assertEquals(addEmptyLocomotiveGroup, locomotiveGroup);
	}

	@Test
	public void locomotiveGroupUpdatedEvent() throws JSONException {

		final ArgumentCaptor<LocomotiveGroup> captor = ArgumentCaptor
				.forClass(LocomotiveGroup.class);
		final JSONObject jsonEmptyLocomotiveGroup = addEmptyLocomotiveGroup()
				.getFirst();

		final JSONObject updatedGroupJson = new JSONObject(
				jsonEmptyLocomotiveGroup.toString());
		updatedGroupJson.put("name", "newName");

		SIOLocomotiveServiceEventHandler.handleLocomotiveGroupUpdated(
				updatedGroupJson, listenerMock);

		verify(listenerMock).locomotiveGroupUpdated(captor.capture());
		final LocomotiveGroup locomotiveGroup = captor.getValue();
		assertEquals("newName", locomotiveGroup.getName());
	}

	@Test
	public void locomotiveAddedEvent() throws JSONException {

		final ArgumentCaptor<Locomotive> captor = ArgumentCaptor
				.forClass(Locomotive.class);

		final LocomotiveGroup addBaseLocomotiveGroup = addEmptyLocomotiveGroup()
				.getSecond();

		final JSONObject createJSONLocomotive2 = LocomotiveTestHelper
				.createJSONLocomotive("2", "digital", 5);
		createJSONLocomotive2.put("group", GROUP_ID);

		SIOLocomotiveServiceEventHandler.handleLocomotiveAdded(
				createJSONLocomotive2, listenerMock);

		verify(listenerMock).locomotiveAdded(captor.capture());
		final Locomotive locomotive = captor.getValue();
		LocomotiveTestHelper.assertLocomotiveBase(locomotive, "2",
				LocomotiveType.DIGITAL);
		assertEquals(addBaseLocomotiveGroup, locomotive.getGroup());

	}

	@Test
	public void locomotiveUpdatedEvent() throws JSONException {
		final ArgumentCaptor<Locomotive> captor = ArgumentCaptor
				.forClass(Locomotive.class);
		addEmptyLocomotiveGroup();
		final Tuple<JSONObject, Locomotive> addLocomotive = addLocomotive();

		final JSONObject updatedLocomotiveJSON = new JSONObject(addLocomotive
				.getFirst().toString());
		updatedLocomotiveJSON.put("name", "newName");

		SIOLocomotiveServiceEventHandler.handleLocomotiveUpdated(
				updatedLocomotiveJSON, listenerMock);

		verify(listenerMock).locomotiveUpdated(captor.capture());
		final Locomotive locomotive = captor.getValue();

		assertEquals("newName", locomotive.getName());
	}

	@Test
	public void locomotiveRemovedEvent() throws JSONException {
		final ArgumentCaptor<Locomotive> captor = ArgumentCaptor
				.forClass(Locomotive.class);
		addEmptyLocomotiveGroup();
		final Tuple<JSONObject, Locomotive> addLocomotive = addLocomotive();

		SIOLocomotiveServiceEventHandler.handleLocomotiveRemoved(
				addLocomotive.getFirst(), listenerMock);

		verify(listenerMock).locomotiveRemoved(captor.capture());
		final Locomotive locomotive = captor.getValue();
		LocomotiveTestHelper.assertLocomotiveBase(locomotive, "2",
				LocomotiveType.DELTA);
	}

	private Tuple<JSONObject, Locomotive> addLocomotive() throws JSONException {
		final JSONObject createJSONLocomotive2 = LocomotiveTestHelper
				.createJSONLocomotive("2", "delta", 5);
		createJSONLocomotive2.put("group", GROUP_ID);

		final Locomotive handleLocomotiveAdded = SIOLocomotiveServiceEventHandler
				.handleLocomotiveAdded(createJSONLocomotive2, listenerMock);
		verify(listenerMock).locomotiveAdded(any(Locomotive.class));

		return new Tuple<JSONObject, Locomotive>(createJSONLocomotive2,
				handleLocomotiveAdded);
	}

	private Tuple<JSONObject, LocomotiveGroup> addEmptyLocomotiveGroup()
			throws JSONException {
		final JSONObject createJSONLocomotiveGroup = LocomotiveTestHelper
				.createJSONLocomotiveGroup(GROUP_ID,
						new ArrayList<JSONObject>());

		return new Tuple<JSONObject, LocomotiveGroup>(
				createJSONLocomotiveGroup,
				SIOLocomotiveServiceEventHandler.handleLocomotiveGroupAdded(
						createJSONLocomotiveGroup, listenerMock));
	}
}
