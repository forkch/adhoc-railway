package ch.fork.adhocrailway.railway.brain.brain.brain;

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveFunction;
import ch.fork.adhocrailway.model.locomotives.LocomotiveType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;

/**
 * Created by fork on 3/22/14.
 */
public class BrainTestSupport {


    protected String createBrainLocomotiveCommand(final Locomotive locomotive,
                                                  final int speed, final String direction,
                                                  final String functions) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XL ");
        stringBuilder.append(locomotive.getAddress1());
        stringBuilder.append(" ");
        stringBuilder.append(speed);
        stringBuilder.append(" " + direction);
        if (!functions.isEmpty())
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
        locomotive.setFunctions(Sets.newTreeSet(Lists.newArrayList(new LocomotiveFunction(0, "0", false, 0),
                new LocomotiveFunction(1, "1", false, 0),
                new LocomotiveFunction(2, "2", false, 0),
                new LocomotiveFunction(3, "3", false, 0),
                new LocomotiveFunction(4, "4", false, 0))));
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
