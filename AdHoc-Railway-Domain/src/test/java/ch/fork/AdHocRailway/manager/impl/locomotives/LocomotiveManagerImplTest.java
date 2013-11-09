package ch.fork.AdHocRailway.manager.impl.locomotives;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerListener;
import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;

public class LocomotiveManagerImplTest {

	private LocomotiveManagerImpl locomotiveManagerImpl;

	@Mock
	private SIOLocomotiveService serviceMock;
	@Mock
	private LocomotiveController controllerMock;
	@Mock
	private LocomotiveManagerListener listenerMock;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		locomotiveManagerImpl = new LocomotiveManagerImpl();
		locomotiveManagerImpl.setLocomotiveControl(controllerMock);
		locomotiveManagerImpl.setLocomotiveService(serviceMock);
		locomotiveManagerImpl.addLocomotiveManagerListener(listenerMock);
		locomotiveManagerImpl.initialize();

	}

	@Test
	public void addLocomotiveGroup() {

		final LocomotiveGroup newGroup = new LocomotiveGroup(1, "test");

		locomotiveManagerImpl.addLocomotiveGroup(newGroup);

		verify(serviceMock).addLocomotiveGroup(newGroup);

	}

	@Test(expected = IllegalArgumentException.class)
	public void addNullLocomotiveGroup() {
		locomotiveManagerImpl.addLocomotiveGroup(null);
	}

	@Test
	public void updateLocomotiveGroup() {

		final LocomotiveGroup newGroup = new LocomotiveGroup(1, "test");

		locomotiveManagerImpl.updateLocomotiveGroup(newGroup);

		verify(serviceMock).updateLocomotiveGroup(newGroup);

	}

	@Test(expected = IllegalArgumentException.class)
	public void updateNullLocomotiveGroup() {
		locomotiveManagerImpl.updateLocomotiveGroup(null);
	}

	@Test
	public void removeLocomotiveGroup() {

		final LocomotiveGroup newGroup = new LocomotiveGroup(1, "test");

		locomotiveManagerImpl.removeLocomotiveGroup(newGroup);
		verify(serviceMock).removeLocomotiveGroup(newGroup);
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeNullLocomotiveGroup() {
		locomotiveManagerImpl.removeLocomotiveGroup(null);
	}

	@Test
	public void addLocomotiveToGroup() {

		// given
		final LocomotiveGroup newGroup = new LocomotiveGroup(1, "test");
		final Locomotive locomotive = new Locomotive();

		// when
		locomotiveManagerImpl.addLocomotiveGroup(newGroup);
		locomotiveManagerImpl.addLocomotiveToGroup(locomotive, newGroup);

		// then
		verify(serviceMock).addLocomotiveGroup(newGroup);
		verify(serviceMock).addLocomotive(locomotive);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addNullLocomotive() {
		locomotiveManagerImpl.addLocomotiveToGroup(null, null);
	}

	@Test
	public void updateLocomotive() {

		// given
		final Locomotive locomotive = new Locomotive();

		// when
		locomotiveManagerImpl.updateLocomotive(locomotive);

		// then
		verify(serviceMock).updateLocomotive(locomotive);
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateNullLocomotive() {
		locomotiveManagerImpl.updateLocomotive(null);
	}

	@Test
	public void removeLocomotive() {

		// given
		final Locomotive locomotive = new Locomotive();
		final LocomotiveGroup newGroup = new LocomotiveGroup(1, "test");
		newGroup.addLocomotive(locomotive);
		locomotive.setGroup(newGroup);

		// when
		locomotiveManagerImpl.removeLocomotiveFromGroup(locomotive, newGroup);

		// then
		verify(serviceMock).removeLocomotive(locomotive);
		assertFalse(newGroup.getLocomotives().contains(locomotive));
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeNullLocomotive() {
		locomotiveManagerImpl.removeLocomotiveFromGroup(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeLocomotiveFromNullGroup() {
		locomotiveManagerImpl.removeLocomotiveFromGroup(new Locomotive(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void removeLocomotiveFromNotItsGroup() {
		final LocomotiveGroup newGroup = new LocomotiveGroup(1, "test");
		locomotiveManagerImpl.removeLocomotiveFromGroup(new Locomotive(),
				newGroup);
	}

	@Test
	public void locomotiveGroupAdded() {
		final LocomotiveGroup newGroup1 = new LocomotiveGroup(1, "test1");
		locomotiveManagerImpl.locomotiveGroupAdded(newGroup1);
		verify(listenerMock).locomotiveGroupAdded(newGroup1);

		final LocomotiveGroup newGroup2 = new LocomotiveGroup(2, "test2");
		locomotiveManagerImpl.locomotiveGroupAdded(newGroup2);
		verify(listenerMock).locomotiveGroupAdded(newGroup2);

		assertEquals(2, locomotiveManagerImpl.getAllLocomotiveGroups().size());
		assertEquals(newGroup1, locomotiveManagerImpl.getAllLocomotiveGroups()
				.first());
	}

	@Test
	public void locomotiveGroupRemoved() {
		final LocomotiveGroup newGroup1 = new LocomotiveGroup(1, "test");
		locomotiveManagerImpl.locomotiveGroupAdded(newGroup1);
		locomotiveManagerImpl.locomotiveGroupRemoved(newGroup1);

		assertEquals(0, locomotiveManagerImpl.getAllLocomotiveGroups().size());
		verify(listenerMock).locomotiveGroupAdded(newGroup1);
	}

	@Test
	public void locomotiveGroupUpdated() {
		final LocomotiveGroup newGroup1 = new LocomotiveGroup(1, "test");
		locomotiveManagerImpl.locomotiveGroupAdded(newGroup1);
		newGroup1.setName("new name");
		locomotiveManagerImpl.locomotiveGroupUpdated(newGroup1);
		verify(listenerMock).locomotiveGroupUpdated(newGroup1);

		assertEquals("new name", locomotiveManagerImpl.getAllLocomotiveGroups()
				.first().getName());
	}
}
