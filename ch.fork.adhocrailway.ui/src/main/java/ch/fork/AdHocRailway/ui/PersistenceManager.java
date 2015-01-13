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
import ch.fork.AdHocRailway.ui.bus.events.CommandLogEvent;
import ch.fork.AdHocRailway.ui.bus.events.InitProceededEvent;
import ch.fork.AdHocRailway.ui.bus.events.UpdateMainTitleEvent;
import ch.fork.AdHocRailway.ui.context.PersistenceManagerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
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

    public void loadPersistenceLayer() {

        final boolean useAdHocServer = appContext.getPreferences()
                .getBooleanValue(PreferencesKeys.USE_ADHOC_SERVER);

        createLocomotiveManagerOnContext(useAdHocServer);

        TurnoutManager turnoutManager = createTurnoutManagerOnContext(useAdHocServer);
        if (appContext.getRouteControl() != null) {
            appContext.getRouteControl().setTurnoutManager(turnoutManager);
        }
        createRouteManagerOnContext(useAdHocServer, turnoutManager);
    }

    private void createRouteManagerOnContext(final boolean useAdHocServer,
                                             TurnoutManager turnoutManager) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Persistence Layer (Routes)"));
        RouteManager routeManager = appContext.getRouteManager();

        if (routeManager == null) {
            routeManager = new RouteManagerImpl(turnoutManager);
        }
        appContext.setRouteManager(routeManager);
        routeManager.setRouteService(PersistenceFactory
                .createRouteService(useAdHocServer, appContext));
        routeManager.initialize();
    }

    private TurnoutManager createTurnoutManagerOnContext(final boolean useAdHocServer) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Persistence Layer (Turnouts)"));

            TurnoutManager turnoutManager = new TurnoutManagerImpl(PersistenceFactory
                    .createTurnoutService(useAdHocServer, appContext));
        appContext.setTurnoutManager(turnoutManager);

        turnoutManager.initialize();
        return turnoutManager;
    }

    private void createLocomotiveManagerOnContext(final boolean useAdHocServer) {
        appContext.getMainBus().post(
                new InitProceededEvent(
                        "Loading Persistence Layer (Locomotives)")
        );
        LocomotiveManager locomotiveManager = appContext.getLocomotiveManager();

        if (locomotiveManager == null) {
            locomotiveManager = new LocomotiveManagerImpl();
            appContext.setLocomotiveManager(locomotiveManager);
        }

        locomotiveManager.setLocomotiveService(PersistenceFactory
                .createLocomotiveService(useAdHocServer, appContext));
        locomotiveManager.initialize();
    }

    public void loadLastFileOrLoadDataFromAdHocServerIfRequested()
            throws IOException {
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

    public void openFile(final File file) throws IOException {
        disconnectFromCurrentPersistence();

        switchToFileMode();

        loadPersistenceLayer();

        appContext.getLocomotiveManager().clear();
        appContext.getTurnoutManager().clear();
        appContext.getRouteManager().clear();

        new XMLServiceHelper()
                .loadFile((XMLLocomotiveService) appContext
                                .getLocomotiveManager().getService(),
                        (XMLTurnoutService) appContext.getTurnoutManager()
                                .getService(), (XMLRouteService) appContext
                                .getRouteManager().getService(), file
                );


        appContext.setActualFile(file);

        appContext.getMainBus().post(
                new UpdateMainTitleEvent(AdHocRailway.TITLE + " ["
                        + file.getAbsolutePath() + "]")
        );

        appContext.getMainBus().post(
                new CommandLogEvent(
                        "AdHoc-Railway Configuration loaded ("
                                + file + ")"
                )
        );
    }

    public void openDatabase() throws IOException {
        disconnectFromCurrentPersistence();

        switchToServerMode();
        loadPersistenceLayer();
        loadLastFileOrLoadDataFromAdHocServerIfRequested();
    }

    public String getAdHocServerURL() {
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

    public void connectToAdHocServer(final String url) {
        SIOService.setUUID(appContext.getAppUUID());
        SIOService.getInstance().connect(url, new ServiceListener() {

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
                appContext.getPreferences().setBooleanValue(
                        PreferencesKeys.USE_ADHOC_SERVER, false);
                try {
                    appContext.getPreferences().save();
                } catch (final IOException e) {
                    throw new AdHocRailwayException(
                            "could not save preferences");
                }
                throw ex;
            }

            @Override
            public void connected() {

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
        appContext.getTurnoutManager().disconnect();
        appContext.getRouteManager().disconnect();
        appContext.getLocomotiveManager().disconnect();
        SIOService.getInstance().disconnect();
    }

    public void autoDiscoverAdHocServerConnect() {
        final JmDNS adhocServermDNS;
        try {
            adhocServermDNS = JmDNS.create();

            adhocServermDNS.addServiceListener(_ADHOC_SERVER_TCP_LOCAL,
                    new javax.jmdns.ServiceListener() {

                        @Override
                        public void serviceResolved(final ServiceEvent event) {
                            LOGGER.info("resolved AdHoc-Server on " + event);
                        }

                        @Override
                        public void serviceRemoved(final ServiceEvent event) {
                        }

                        @Override
                        public void serviceAdded(final ServiceEvent event) {
                            final ServiceInfo info = adhocServermDNS
                                    .getServiceInfo(event.getType(),
                                            event.getName(), true);
                            LOGGER.info("found AdHoc-Server on " + info);

                            final String url = "http://"
                                    + info.getInet4Addresses()[0]
                                    .getHostAddress() + ":"
                                    + info.getPort();

                            connectToAdHocServer(url);
                        }
                    }
            );
        } catch (final IOException e) {
            throw new AdHocRailwayException(
                    "failure during autodiscovery/autoconnect to AdHoc-Server",
                    e);
        }
    }

    public void createNewFile() throws FileNotFoundException, IOException {
        disconnectFromCurrentPersistence();
        switchToFileMode();
        loadPersistenceLayer();
    }

    private void switchToFileMode() throws FileNotFoundException, IOException {
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
