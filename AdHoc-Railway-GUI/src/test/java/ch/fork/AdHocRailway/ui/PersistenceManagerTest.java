package ch.fork.AdHocRailway.ui;

import org.junit.Assert;
import org.junit.Test;

import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIOTurnoutService;
import ch.fork.AdHocRailway.services.impl.xml.XMLLocomotiveService;
import ch.fork.AdHocRailway.services.impl.xml.XMLRouteService;
import ch.fork.AdHocRailway.services.impl.xml.XMLTurnoutService;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;

public class PersistenceManagerTest {

	private PersistenceManager testee;

	@Test
	public void loadingPersistenceForFileMode() {
		// given
		final PersistenceManagerContext applicationContext = createApplicationContextForFileLoading();
		testee = createTestee(applicationContext);

		// when
		whenLoadingPersistenceLayer();

		// then
		Assert.assertTrue(applicationContext.getLocomotiveManager()
				.getService() instanceof XMLLocomotiveService);
		Assert.assertTrue(applicationContext.getTurnoutManager().getService() instanceof XMLTurnoutService);
		Assert.assertTrue(applicationContext.getRouteManager().getService() instanceof XMLRouteService);
	}

	@Test
	public void loadingPersistenceForAdHocServerMode() {
		// given
		final PersistenceManagerContext applicationContext = createApplicationContextForAdHocServer();
		testee = createTestee(applicationContext);

		// when
		whenLoadingPersistenceLayer();

		// then
		Assert.assertTrue(applicationContext.getLocomotiveManager()
				.getService() instanceof SIOLocomotiveService);
		Assert.assertTrue(applicationContext.getTurnoutManager().getService() instanceof SIOTurnoutService);
		Assert.assertTrue(applicationContext.getRouteManager().getService() instanceof SIORouteService);
	}

	private PersistenceManagerContext createApplicationContextForAdHocServer() {
		final ApplicationContext applicationContext = new ApplicationContext();
		final Preferences preferences = Preferences.getInstance();
		preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, true);

		applicationContext.setPreferences(preferences);
		return applicationContext;
	}

	private void whenLoadingPersistenceLayer() {
		testee.loadPersistenceLayer();
	}

	private PersistenceManager createTestee(
			final PersistenceManagerContext persistenceManagerContext) {
		return new PersistenceManager(persistenceManagerContext);
	}

	private PersistenceManagerContext createApplicationContextForFileLoading() {
		final ApplicationContext applicationContext = new ApplicationContext();
		final Preferences preferences = Preferences.getInstance();
		preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, false);

		applicationContext.setPreferences(preferences);
		return applicationContext;
	}
}
