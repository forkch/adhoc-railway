package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.controllers.*;
import ch.fork.AdHocRailway.controllers.impl.dummy.*;
import ch.fork.AdHocRailway.model.AdHocRailwayException;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import ch.fork.AdHocRailway.railway.brain.brain.BrainController;
import ch.fork.AdHocRailway.railway.brain.brain.BrainListener;
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

public class RailwayDeviceManager implements CommandDataListener,
        InfoDataListener, PreferencesKeys, BrainListener, DummyListener {

    private static final Logger LOGGER = Logger
            .getLogger(RailwayDeviceManager.class);
    private final RailwayDeviceManagerContext appContext;
    private boolean connected = false;


    public RailwayDeviceManager(final RailwayDeviceManagerContext appContext) {
        this.appContext = appContext;
        appContext.getMainBus().register(this);

        putNullDeviceInitially();

    }

    private void putNullDeviceInitially() {
        loadControlLayer(RailwayDevice.NULL_DEVICE);
    }


    public void connect() {
        if (connected)
            disconnect();

        loadControlLayer(getRailwayDevice());

        final Preferences preferences = appContext.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        final RailwayDevice railwayDevive = RailwayDevice
                .fromString(railwayDeviceString);
        if (RailwayDevice.SRCP == railwayDevive) {
            final String host = preferences.getStringValue(SRCP_HOSTNAME);
            final int port = preferences.getIntValue(SRCP_PORT);
            connectToSRCPServer(host, port);
        } else if (RailwayDevice.ADHOC_BRAIN == railwayDevive) {
            connectToBrain(preferences.getStringValue(ADHOC_BRAIN_PORT));
        } else {
            connectToNullDevice();
        }

        connected = true;
        appContext.getMainBus().post(new ConnectedToRailwayEvent(true));
    }

    public void disconnect() {

        final RailwayDevice railwayDevive = getRailwayDevice();
        if (railwayDevive.equals(RailwayDevice.SRCP)) {
            disconnectFromSRCPServer();
        } else {
            disconnectFromBrain();
        }

        appContext.setLocomotiveControl(new DummyLocomotiveController(DummyRailwayController.getInstance()));
        appContext.setTurnoutControl(new DummyTurnoutController(DummyRailwayController.getInstance()));
        appContext.setRouteControl(new DummyRouteController(appContext.getTurnoutControl(), appContext.getTurnoutManager()));
        appContext.setPowerController(new DummyPowerController(DummyRailwayController.getInstance()));

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
                throw new AdHocRailwayException("failed to autoconnect to railway device",
                        x);
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
        DummyRailwayController.getInstance().removeDummyListener(this);
        DummyRailwayController.getInstance().addDummyListener(this);
    }

    private void connectToBrain(final String stringValue) {
        final BrainController brainController = BrainController.getInstance();
        brainController.addBrainListener(this);
        brainController.connect(stringValue);

    }

    private void disconnectFromBrain() {
        BrainController.getInstance().disconnect();

    }

    private void connectToSRCPServer(final String host, final int port) {
        try {


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
            if (session != null) {
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

    private void loadControlLayer(RailwayDevice railwayDevice) {

        final TaskExecutor taskExecutor = new TaskExecutor();
        createPowerControllerOnContext(railwayDevice);

        createLocomotiveControllerOnContext(railwayDevice, taskExecutor);

        final TurnoutController turnoutControl = createTurnoutControllerOnContext(railwayDevice, taskExecutor);

        createRouteControllerOnContext(railwayDevice, turnoutControl);

        createLockControllerOnContext();
    }

    private RailwayDevice getRailwayDevice() {
        final Preferences preferences = appContext.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        return RailwayDevice
                .fromString(railwayDeviceString);
    }

    private void createLockControllerOnContext() {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Locks)"));
        SRCPLockControl.getInstance().removeAllLockChangeListener();
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
                PreferencesKeys.ROUTING_DELAY, 250));
        appContext.setRouteControl(routeControl);
    }

    private TurnoutController createTurnoutControllerOnContext(
            final RailwayDevice railwayDevive, TaskExecutor taskExecutor) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Turnouts)"));
        final TurnoutController turnoutControl = RailwayDeviceFactory
                .createTurnoutController(railwayDevive, taskExecutor);
        turnoutControl.setCutterSleepTime(Preferences.getInstance().getIntValue(
                PreferencesKeys.CUTTER_SLEEP_TIME, 500));
        appContext.setTurnoutControl(turnoutControl);
        return turnoutControl;
    }

    private void createLocomotiveControllerOnContext(
            final RailwayDevice railwayDevive, TaskExecutor taskExecutor) {
        appContext.getMainBus().post(
                new InitProceededEvent("Loading Control Layer (Locomotives)"));
        final LocomotiveController locomotiveControl = RailwayDeviceFactory
                .createLocomotiveController(railwayDevive, taskExecutor);

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
        if (!(appContext.getPowerControl() instanceof SRCPPowerControlAdapter)) {
            return;
        }
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
        logIfLoggingEnabled("Command sent: " + commandData);
    }

    @Override
    public void commandDataReceived(final String response) {
        logIfLoggingEnabled("Command received: " + response);
    }

    @Override
    public void infoDataSent(final String infoData) {
        logIfLoggingEnabled("Info sent: " + infoData);
    }

    @Override
    public void infoDataReceived(final String infoData) {
        logIfLoggingEnabled("Info received: " + infoData);
    }

    @Override
    public void sentMessage(String sentMessage) {
        logIfLoggingEnabled("Sent to brain: " + sentMessage);
    }

    @Override
    public void sentDummyMessage(String sentMessage) {
        logIfLoggingEnabled("Dummy: " + sentMessage);
    }


    @Override
    public void receivedMessage(String receivedString) {
        logIfLoggingEnabled("receive from brain: " + receivedString);
    }

    private void logIfLoggingEnabled(String response) {
        if (response.contains("POWER"))
            return;
        final Preferences preferences = appContext.getPreferences();
        if (preferences.getBooleanValue(LOGGING)) {
            appContext.getMainBus().post(
                    new CommandLogEvent(response));
        }
        //LOGGER.info(response);
    }

    public void sendCommand(String command) {

        final Preferences preferences = appContext.getPreferences();
        final String railwayDeviceString = preferences
                .getStringValue(RAILWAY_DEVICE);
        final RailwayDevice railwayDevive = RailwayDevice
                .fromString(railwayDeviceString);
        if (isConnected()) {
            throw new AdHocRailwayException("not connected");
        }
        if (RailwayDevice.SRCP == railwayDevive) {
            try {
                appContext.getSession().getCommandChannel().send(String.format("SET 0 GM 0 0 BRAINCMD %s", command));
            } catch (SRCPException e) {
                throw new AdHocRailwayException("failed to send command to brain", e);
            }
        } else if (RailwayDevice.ADHOC_BRAIN == railwayDevive) {
            BrainController.getInstance().write(command);
        } else {
            LOGGER.info(String.format("NULL device: received command %s", command));
        }

    }
}
