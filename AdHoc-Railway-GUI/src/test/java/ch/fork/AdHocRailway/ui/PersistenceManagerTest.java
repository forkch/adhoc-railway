package ch.fork.AdHocRailway.ui;

import org.junit.Test;

import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;

public class PersistenceManagerTest {

	private PersistenceManager testee;

	@Test
	public void loading_file() {
		// given
		testee = createTestee();
	}

	private PersistenceManager createTestee() {
		return new PersistenceManager(createApplicationContextForFileLoading());
	}

	private PersistenceManagerContext createApplicationContextForFileLoading() {
		final ApplicationContext applicationContext = new ApplicationContext();
		return applicationContext;

	}
}
