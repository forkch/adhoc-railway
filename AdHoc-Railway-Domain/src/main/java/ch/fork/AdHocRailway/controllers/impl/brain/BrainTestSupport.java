package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

import java.io.IOException;

/**
 * Created by fork on 3/22/14.
 */
public class BrainTestSupport {


    protected String createBrainLocomotiveCommand(final Locomotive locomotive,
                                               final int speed, final String direction, final String light,
                                               final String functions) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XL ");
        stringBuilder.append(locomotive.getAddress1());
        stringBuilder.append(" ");
        stringBuilder.append(speed);
        stringBuilder.append(" " + light);
        stringBuilder.append(" " + direction);
        stringBuilder.append(" " + functions);
        return stringBuilder.toString();

    }

    protected Locomotive createDeltaLocomotive() {
        final Locomotive locomotive = new Locomotive();
        locomotive.setType(LocomotiveType.DELTA);
        locomotive.setAddress1(1);
        locomotive.setCurrentFunctions(new boolean[1]);
        return locomotive;
    }

    protected Locomotive createDigitalLocomotive() {
        final Locomotive locomotive = new Locomotive();
        locomotive.setType(LocomotiveType.DIGITAL);
        locomotive.setAddress1(1);
        locomotive.setCurrentFunctions(new boolean[5]);
        return locomotive;
    }

    protected Locomotive createSimulatedMfxLocomotive() {
        final Locomotive locomotive = new Locomotive();
        locomotive.setType(LocomotiveType.SIMULATED_MFX);
        locomotive.setAddress1(1);
        locomotive.setAddress2(2);
        locomotive.setCurrentFunctions(new boolean[9]);
        return locomotive;
    }
}
