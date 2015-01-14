package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestLocomotiveService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestRouteService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.rest.RestTurnoutService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLLocomotiveService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLRouteService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLTurnoutService;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.context.ApplicationContext;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PersistenceManagerTest {

    private PersistenceManager testee;

    @Test
    public void loadingPersistenceForFileMode() throws IOException {
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
    public void loadingPersistenceForAdHocServerMode() throws IOException {
        // given
        final PersistenceManagerContext applicationContext = createApplicationContextForAdHocServer();
        testee = createTestee(applicationContext);

        // when
        whenLoadingPersistenceLayer();

        // then
        Assert.assertTrue(applicationContext.getLocomotiveManager()
                .getService() instanceof RestLocomotiveService);
        Assert.assertTrue(applicationContext.getTurnoutManager().getService() instanceof RestTurnoutService);
        Assert.assertTrue(applicationContext.getRouteManager().getService() instanceof RestRouteService);
    }

    private PersistenceManagerContext createApplicationContextForAdHocServer() {
        final ApplicationContext applicationContext = new ApplicationContext();
        final Preferences preferences = Preferences.getInstance();
        preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, true);

        applicationContext.setPreferences(preferences);
        return applicationContext;
    }

    private void whenLoadingPersistenceLayer() throws IOException {
        testee.loadLastFileOrLoadDataFromAdHocServerIfRequested();
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
