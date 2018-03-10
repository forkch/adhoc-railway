package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.manager.impl.LocomotiveManagerImpl;
import ch.fork.AdHocRailway.manager.impl.RouteManagerImpl;
import ch.fork.AdHocRailway.manager.impl.TurnoutManagerImpl;
import ch.fork.AdHocRailway.model.AdHocRailwayException;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.ServiceListener;
import ch.fork.AdHocRailway.persistence.xml.XMLServiceHelper;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLLocomotiveService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLRouteService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLTurnoutService;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.*;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PersistenceManager {

    private static final Logger LOGGER = Logger
            .getLogger(PersistenceManager.class);
    private static final String _ADHOC_SERVER_TCP_LOCAL = "_adhoc-server._tcp.local.";

    private final PersistenceManagerContext appContext;


    public PersistenceManager(final PersistenceManagerContext ctx) {
        this.appContext = ctx;
    }


    public void loadLastFileOrLoadDataFromAdHocServerIfRequested() {
        final Preferences preferences = appContext.getPreferences();

        final boolean useAdHocServer = preferences
                .getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER);
        if (!useAdHocServer
                && preferences.getBooleanValue(PreferencesKeys.OPEN_LAST_FILE)) {
            final String lastFile = preferences
                    .getStringValue(PreferencesKeys.LAST_OPENED_FILE);
            if (StringUtils.isNotBlank(lastFile)) {
                openFile(new File(
                        preferences
                                .getStringValue(PreferencesKeys.LAST_OPENED_FILE)
                ));
            }
        } else if (useAdHocServer
                && !appContext.getPreferences().getBooleanValue(
                PreferencesKeys.AUTO_DISCOVER)) {

            final String url = getAdHocServerURL();
            connectToAdHocServer(url);
        }
    }

    public void openFile(final File file) {
        disconnectFromCurrentPersistence();

        switchToFileMode();

        loadPersistenceLayer(appContext.getPreferences()
                .getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER));

        try {
            new XMLServiceHelper()
                    .loadFile((XMLLocomotiveService) appContext
                                    .getLocomotiveManager().getService(),
                            (XMLTurnoutService) appContext.getTurnoutManager()
                                    .getService(), (XMLRouteService) appContext
                                    .getRouteManager().getService(), file
                    );

            appContext.setActualFile(file);
            appContext.getPreferences().setStringValue(PreferencesKeys.LAST_OPENED_FILE,
                    file.getAbsolutePath());
            appContext.getPreferences().save();

            appContext.getMainBus().post(
                    new UpdateMainTitleEvent(AdHocRailway.TITLE + " ["
                            + file.getAbsolutePath() + "]")
            );
            appContext.getMainBus().post(
                    new CommandLogEvent(
                            "AdHoc-Railway configuration loaded ("
                                    + file + ")"
                    )
            );
        } catch (Exception x) {
            appContext.getPreferences().setStringValue(PreferencesKeys.LAST_OPENED_FILE,
                    null);
            appContext.getPreferences().setBooleanValue(PreferencesKeys.OPEN_LAST_FILE,
                    false);
            appContext.getPreferences().save();
            throw new AdHocRailwayException(
                    "error while loading previous AdHoc-Railway configuration.", x);
        }

    }

    public void openDatabase() throws IOException {
        disconnectFromCurrentPersistence();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switchToServerMode();
        connectToAdHocServer(getAdHocServerURL());
    }


    private void connectToAdHocServer(final String url) {
        final SIOService sioService = new SIOService(appContext.getAppUUID());
        sioService.connect(url, new ServiceListener() {

            @Override
            public void disconnected() {
                appContext.getMainBus().post(
                        new CommandLogEvent(
                                "Successfully disconnected from AdHoc-Server")
                );
            }

            @Override
            public void connectionError(final AdHocServiceException ex) {
                appContext.getMainBus().post(
                        new CommandLogEvent("Connection error: "
                                + ex.getMessage())
                );
                throw ex;
            }

            @Override
            public void connected() {
                appContext.setSIOService(sioService);

                loadPersistenceLayer(true);

                appContext.getMainBus().post(
                        new UpdateMainTitleEvent(AdHocRailway.TITLE + " ["
                                + url + "]")
                );

                appContext.getMainBus().post(
                        new CommandLogEvent(
                                "Successfully connected to AdHoc-Server: "
                                        + url
                        )
                );

            }
        });
    }

    public void disconnectFromCurrentPersistence() {
        if (appContext.getSioService() != null) {
            appContext.getSioService().disconnect();


        }
    }

    public void initializeEmpty() {
        disconnectFromCurrentPersistence();
        loadPersistenceLayer(false);
    }
    public void createNewFile() {
        disconnectFromCurrentPersistence();
        switchToFileMode();
        loadPersistenceLayer(appContext.getPreferences()
                .getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER));
    }

    private String getAdHocServerURL() {
        final Preferences preferences = appContext.getPreferences();
        final StringBuilder b = new StringBuilder();
        b.append("http://");

        b.append(preferences
                .getStringValue(PreferencesKeys.ADHOC_SERVER_HOSTNAME));
        b.append(":");
        b.append(preferences.getStringValue(PreferencesKeys.ADHOC_SERVER_PORT));
        final String url = b.toString();
        return url;
    }

    private void loadPersistenceLayer(boolean useAdHocServer) {


        createLocomotiveManagerOnContext(useAdHocServer);
        createTurnoutManagerOnContext(useAdHocServer);
        createRouteManagerOnContext(useAdHocServer, appContext.getTurnoutManager());

        appContext.getMainBus().post(new ConnectedToPersistenceEvent(true));

    }

    private void createRouteManagerOnContext(final boolean useAdHocServer,
                                             TurnoutManager turnoutManager) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Persistence Layer (Routes)"));

        RouteManager routeManager = new RouteManagerImpl(turnoutManager, PersistenceFactory
                .createRouteService(useAdHocServer, appContext));
        appContext.setRouteManager(routeManager);
        routeManager.initialize();
    }

    private void createTurnoutManagerOnContext(final boolean useAdHocServer) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Persistence Layer (Turnouts)"));

        TurnoutManager turnoutManager = new TurnoutManagerImpl(PersistenceFactory
                .createTurnoutService(useAdHocServer, appContext));
        appContext.setTurnoutManager(turnoutManager);

        turnoutManager.initialize();
    }

    private void createLocomotiveManagerOnContext(final boolean useAdHocServer) {
        appContext.getMainBus().post(
                new InitProceededEvent(
                        "Loading Persistence Layer (Locomotives)")
        );

        LocomotiveManager locomotiveManager = new LocomotiveManagerImpl(PersistenceFactory
                .createLocomotiveService(useAdHocServer, appContext));
        appContext.setLocomotiveManager(locomotiveManager);
        locomotiveManager.initialize();
    }

    private void switchToFileMode() {
        final Preferences preferences = appContext.getPreferences();
        preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, false);
        preferences.save();
    }

    private void switchToServerMode() throws FileNotFoundException, IOException {
        final Preferences preferences = appContext.getPreferences();
        preferences.setBooleanValue(PreferencesKeys.USE_ADHOC_SERVER, true);
        preferences.save();
    }

}
