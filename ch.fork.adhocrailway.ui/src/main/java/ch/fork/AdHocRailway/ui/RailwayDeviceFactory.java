package ch.fork.AdHocRailway.ui;

import ch.fork.AdHocRailway.controllers.*;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyLocomotiveController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyPowerController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyRouteController;
import ch.fork.AdHocRailway.controllers.impl.dummy.DummyTurnoutController;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.railway.brain.brain.*;
import ch.fork.AdHocRailway.railway.srcp.SRCPLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPPowerControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPRouteControlAdapter;
import ch.fork.AdHocRailway.railway.srcp.SRCPTurnoutControlAdapter;

public class RailwayDeviceFactory {

    public static LocomotiveController createLocomotiveController(
            final RailwayDevice railwayDevice) {
        if (railwayDevice == null) {
            return new DummyLocomotiveController();
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainLocomotiveControlAdapter(
                        BrainController.getInstance());
            case SRCP:
                return new SRCPLocomotiveControlAdapter();
            default:
                return new DummyLocomotiveController();

        }
    }

    public static TurnoutController createTurnoutController(
            final RailwayDevice railwayDevice) {

        if (railwayDevice == null) {
            return new DummyTurnoutController();
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainTurnoutControlAdapter(BrainController.getInstance());
            case SRCP:
                return new SRCPTurnoutControlAdapter();
            default:
                return new DummyTurnoutController();
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
            return new DummyPowerController();
        }
        switch (railwayDevice) {
            case ADHOC_BRAIN:
                return new BrainPowerControlAdapter(BrainController.getInstance());
            case SRCP:
                return new SRCPPowerControlAdapter();
            default:
                return new DummyPowerController();

        }

    }
}
