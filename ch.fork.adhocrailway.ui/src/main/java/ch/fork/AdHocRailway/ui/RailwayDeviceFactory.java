package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.controllers.*;
import ch.fork.AdHocRailway.controllers.impl.dummy.*;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.railway.brain.brain.*;
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;

public class RailwayDeviceFactory {

    public static LocomotiveController createLocomotiveController(
            final RailwayDevice railwayDevice, TaskExecutor taskExecutor) {
        if (railwayDevice == null) {
            return new DummyLocomotiveController(DummyRailwayController.getInstance());
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainLocomotiveControlAdapter(taskExecutor,
                        BrainController.getInstance());
            case SRCP:
                return new SRCPLocomotiveControlAdapter(taskExecutor);
            default:
                return new DummyLocomotiveController(DummyRailwayController.getInstance());

        }
    }

    public static TurnoutController createTurnoutController(
            final RailwayDevice railwayDevice, TaskExecutor taskExecutor) {

        if (railwayDevice == null) {
            return new DummyTurnoutController(DummyRailwayController.getInstance());
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainTurnoutControlAdapter(taskExecutor, BrainController.getInstance());
            case SRCP:
                return new SRCPTurnoutControlAdapter(taskExecutor);
            default:
                return new DummyTurnoutController(DummyRailwayController.getInstance());
        }

    }

    public static RouteController createRouteController(
            final RailwayDevice railwayDevice,
            final TurnoutController turnoutController, TurnoutManager turnoutManager) {
        if (railwayDevice == null) {
            return new DummyRouteController(turnoutController, turnoutManager);
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainRouteControlAdapter(turnoutController, turnoutManager);
            case SRCP:
                return new SRCPRouteControlAdapter(
                        (SRCPTurnoutControlAdapter) turnoutController, turnoutManager);
            default:
                return new DummyRouteController(turnoutController, turnoutManager);

        }
    }

    public static PowerController createPowerController(
            final RailwayDevice railwayDevice) {
        if (railwayDevice == null) {
            return new DummyPowerController(DummyRailwayController.getInstance());
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainPowerControlAdapter(BrainController.getInstance());
            case SRCP:
                return new SRCPPowerControlAdapter();
            default:
                return new DummyPowerController(DummyRailwayController.getInstance());

        }

    }
}
