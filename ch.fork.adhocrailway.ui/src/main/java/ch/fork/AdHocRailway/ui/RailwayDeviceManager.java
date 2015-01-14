package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.controllers.*;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyLocomotiveController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyPowerController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyRouteController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyTurnoutController;
import ch.fork.AdHocRailway.model.AdHocRailwayException;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import ch.fork.AdHocRailway.railway.brain.brain.BrainController;
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.bus.events.CommandLogEvent;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.bus.events.InitProceededEvent;
import ch.fork.AdHocRailway.ui.context.RailwayDeviceManagerContext;
import de.dermoba.srcp.client.CommandDataListener;
import de.dermoba.srcp.client.InfoDataListener;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import org.apache.log4j.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

public class RailwayDeviceManager implements CommandDataListener,
        InfoDataListener, PreferencesKeys {

    private static final Logger LOGGER = Logger
            .getLogger(RailwayDeviceManager.class);
    private static final String SRCP_SERVER_TCP_LOCAL = "_srcpd._tcp.local.";
    private final RailwayDeviceManagerContext appContext;
    private JmDNS adhocServermDNS;
    private boolean connected = false;

    public RailwayDeviceManager(final RailwayDeviceManagerContext appContext) {
        this.appContext = appContext;
        appContext.getMainBus().register(this);
    }


    public void connect() {

        loadControlLayer();

        final Preferences preferences = appContext.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        final RailwayDevice railwayDevive = RailwayDevice
                .fromString(railwayDeviceString);
        if (RailwayDevice.SRCP.equals(railwayDevive)) {
            final String host = preferences.getStringValue(SRCP_HOSTNAME);
            final int port = preferences.getIntValue(SRCP_PORT);
            connectToSRCPServer(host, port);
        } else if (RailwayDevice.ADHOC_BRAIN.equals(railwayDevive)) {
            connectToBrain(preferences.getStringValue(ADHOC_BRAIN_PORT));
        } else {
            connectToNullDevice();
        }

        connected = true;
        appContext.getMainBus().post(new ConnectedToRailwayEvent(true));
    }

    public void disconnect() {

        final Preferences preferences = appContext.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        final RailwayDevice railwayDevive = RailwayDevice
                .fromString(railwayDeviceString);
        if (railwayDevive.equals(RailwayDevice.SRCP)) {
            disconnectFromSRCPServer();
        } else {
            disconnectFromBrain();
        }

        appContext.setLocomotiveControl(new DummyLocomotiveController());
        appContext.setTurnoutControl(new DummyTurnoutController());
        appContext.setRouteControl(new DummyRouteController(appContext.getTurnoutControl(), appContext.getTurnoutManager()));
        appContext.setPowerController(new DummyPowerController());

        connected = false;

        appContext.getMainBus().post(new ConnectedToRailwayEvent(false));


    }

    public void autoConnectToRailwayDeviceIfRequested() {
        final Preferences preferences = appContext.getPreferences();
        if (preferences.getBooleanValue(AUTOCONNECT_TO_RAILWAY)
                && !preferences
                .getBooleanValue(PreferencesKeys.AUTO_DISCOVER)) {
            try {
                connect();
            } catch (final Exception x) {

            }
        } else if (preferences
                .getBooleanValue(PreferencesKeys.AUTO_DISCOVER)) {

        }
    }

    public boolean isBrainAvailable() {
        final BrainController brainController = BrainController.getInstance();
        try {
            brainController.getAvailableSerialPortsAsString();
            return true;
        } catch (final Exception x) {
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void connectToNullDevice() {

    }

    private void connectToBrain(final String stringValue) {
        final BrainController brainController = BrainController.getInstance();
        brainController.connect(stringValue);
    }

    private void disconnectFromBrain() {
        BrainController.getInstance().disconnect();

    }

    private void connectToSRCPServer(final String host, final int port) {
        try {
            final SRCPSession session = new SRCPSession(host, port, false);
            appContext.setSession(session);
            session.getCommandChannel().addCommandDataListener(this);
            session.getInfoChannel().addInfoDataListener(this);
            setSessionOnControllers(session);
            session.connect();

            appContext.getMainBus().post(
                    new CommandLogEvent("Connected to server " + host
                            + " on port " + port)
            );

            final SRCPTurnoutControlAdapter srcpTurnoutControlAdapter = (SRCPTurnoutControlAdapter) appContext
                    .getTurnoutControl();
            srcpTurnoutControlAdapter.registerTurnouts(appContext
                    .getTurnoutManager().getAllTurnouts());

            final SRCPRouteControlAdapter srcpRouteControlAdapter = (SRCPRouteControlAdapter) appContext
                    .getRouteControl();
            srcpRouteControlAdapter.registerRoutes(appContext.getRouteManager()
                    .getAllRoutes());

            final SRCPLocomotiveControlAdapter srcpLocomotiveControlAdapter = (SRCPLocomotiveControlAdapter) appContext
                    .getLocomotiveControl();
            srcpLocomotiveControlAdapter.registerLocomotives(appContext
                    .getLocomotiveManager().getAllLocomotives());

        } catch (final SRCPException e) {
            throw new AdHocRailwayException("failed to connect to SRCP server",
                    e);
        }

    }

    private void disconnectFromSRCPServer() {
        try {
            final Preferences preferences = appContext.getPreferences();
            final String host = preferences.getStringValue(SRCP_HOSTNAME);
            final int port = preferences.getIntValue(SRCP_PORT);

            //appContext.getLocomotiveControl().emergencyStopActiveLocos();
            SRCPSession session = appContext.getSession();
            if(session!= null) {
                session.disconnect();
                session = null;
                setSessionOnControllers(session);
            }

            appContext.getMainBus().post(
                    new CommandLogEvent("Disconnected from server " + host
                            + " on port " + port)
            );
        } catch (final SRCPException e) {
            throw new AdHocRailwayException(
                    "failed to disconnect from SRCP server", e);

        }
    }

    private void loadControlLayer() {
        final Preferences preferences = appContext.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        final RailwayDevice railwayDevive = RailwayDevice
                .fromString(railwayDeviceString);

        createPowerControllerOnContext(railwayDevive);

        createLocomotiveControllerOnContext(railwayDevive);

        final TurnoutController turnoutControl = createTurnoutControllerOnContext(railwayDevive);

        createRouteControllerOnContext(railwayDevive, turnoutControl);

        createLockControllerOnContext();
    }

    private void createLockControllerOnContext() {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Locks)"));
        appContext.setLockControl(SRCPLockControl.getInstance());
    }

    private void createRouteControllerOnContext(
            final RailwayDevice railwayDevive,
            final TurnoutController turnoutControl) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Routes)"));
        final RouteController routeControl = RailwayDeviceFactory
                .createRouteController(railwayDevive, turnoutControl, appContext.getTurnoutManager());
        routeControl.setRoutingDelay(Preferences.getInstance().getIntValue(
                PreferencesKeys.ROUTING_DELAY));
        appContext.setRouteControl(routeControl);
    }

    private TurnoutController createTurnoutControllerOnContext(
            final RailwayDevice railwayDevive) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Turnouts)"));
        final TurnoutController turnoutControl = RailwayDeviceFactory
                .createTurnoutController(railwayDevive);
        appContext.setTurnoutControl(turnoutControl);
        return turnoutControl;
    }

    private void createLocomotiveControllerOnContext(
            final RailwayDevice railwayDevive) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Locomotives)"));
        final LocomotiveController locomotiveControl = RailwayDeviceFactory
                .createLocomotiveController(railwayDevive);

        appContext.setLocomotiveControl(locomotiveControl);
    }

    private void createPowerControllerOnContext(
            final RailwayDevice railwayDevive) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Power)"));

        final PowerController powerControl = RailwayDeviceFactory
                .createPowerController(railwayDevive);

        powerControl.addOrUpdatePowerSupply(new PowerSupply(1));
        appContext.setPowerController(powerControl);
    }

    private void setSessionOnControllers(final SRCPSession session) {
        ((SRCPPowerControlAdapter) appContext.getPowerControl())
                .setSession(session);
        ((SRCPTurnoutControlAdapter) appContext.getTurnoutControl())
                .setSession(session);
        ((SRCPLocomotiveControlAdapter) appContext.getLocomotiveControl())
                .setSession(session);
        ((SRCPRouteControlAdapter) appContext.getRouteControl())
                .setSession(session);
        appContext.getLockControl().setSession(session);
    }

    @Override
    public void commandDataSent(final String commandData) {
        final Preferences preferences = appContext.getPreferences();
        if (preferences.getBooleanValue(LOGGING)) {
            appContext.getMainBus().post(
                    new CommandLogEvent("Command sent: " + commandData));
        }
        LOGGER.info("Command sent: " + commandData.trim());
    }

    @Override
    public void commandDataReceived(final String response) {
        final Preferences preferences = appContext.getPreferences();
        if (preferences.getBooleanValue(LOGGING)) {
            appContext.getMainBus().post(
                    new CommandLogEvent("Command received: " + response));
        }
        LOGGER.info("Command received: " + response.trim());
    }

    @Override
    public void infoDataSent(final String infoData) {
        final Preferences preferences = appContext.getPreferences();
        if (preferences.getBooleanValue(LOGGING)) {
            appContext.getMainBus().post(
                    new CommandLogEvent("Info sent: " + infoData));
        }
        LOGGER.info("Info sent: " + infoData.trim());
    }

    @Override
    public void infoDataReceived(final String infoData) {
        final Preferences preferences = appContext.getPreferences();
        if (preferences.getBooleanValue(LOGGING)) {
            appContext.getMainBus().post(
                    new CommandLogEvent("Info received: " + infoData));
        }
        LOGGER.info("Info received " + infoData.trim());
    }


}
