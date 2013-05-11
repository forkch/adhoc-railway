package ch.fork.AdHocRailway.manager.impl.locomotives;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.impl.locomotives.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;

public class LocomotiveManagerImplTest {

	private LocomotiveManagerImpl locomotiveManagerImpl;
	private SIOLocomotiveService serviceMock;

	@Before
	public void setup() {
		locomotiveManagerImpl = new LocomotiveManagerImpl();
		serviceMock = mock(SIOLocomotiveService.class);
		locomotiveManagerImpl.setLocomotiveService(serviceMock);
	}

	@Test
	public void testAddLocomotiveGroup() {

		// given
		final LocomotiveGroup newGroup = new LocomotiveGroup();

		// when
		locomotiveManagerImpl.addLocomotiveGroup(newGroup);

		// then
		verify(serviceMock).addLocomotiveGroup(newGroup);
		assertEquals(1, locomotiveManagerImpl.getAllLocomotiveGroups().size());

	}
}
