package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import de.dermoba.srcp.model.locomotives.DoubleMMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.MMDeltaLocomotive;
import de.dermoba.srcp.model.locomotives.MMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SRCPLocomotiveControlAdapterTest {

    private SRCPLocomotiveControlAdapter adapter;

    @Before
    public void setup() {
        adapter = new SRCPLocomotiveControlAdapter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createSRCPLocomotiveFromNull() {
        adapter.getOrCreateSrcpLocomotive(null);
    }

    @Test
    public void addDeltaSRCPLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.DELTA);
        final SRCPLocomotive srcpLocomotive = adapter
                .getOrCreateSrcpLocomotive(locomotive);

        assertTrue(srcpLocomotive instanceof MMDeltaLocomotive);
        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    @Test
    public void addDigitalSRCPLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.DIGITAL);
        final SRCPLocomotive srcpLocomotive = adapter
                .getOrCreateSrcpLocomotive(locomotive);

        assertTrue(srcpLocomotive instanceof MMDigitalLocomotive);
        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    @Test
    public void addSimulatedMFXSRCPLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.SIMULATED_MFX);
        final DoubleMMDigitalLocomotive srcpLocomotive = (DoubleMMDigitalLocomotive) adapter
                .getOrCreateSrcpLocomotive(locomotive);

        assertTrue(srcpLocomotive instanceof DoubleMMDigitalLocomotive);
        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
        assertEquals(locomotive.getAddress2(), srcpLocomotive.getAddress2());
    }

    @Test
    public void updateExistingDeltaLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.DELTA);
        SRCPLocomotive srcpLocomotive = adapter.getOrCreateSrcpLocomotive(locomotive);

        assertTrue(srcpLocomotive instanceof MMDeltaLocomotive);
        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());

        locomotive.setBus(2);
        locomotive.setAddress1(3);
        srcpLocomotive = adapter.getOrCreateSrcpLocomotive(locomotive);

        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    @Test
    public void updateExistingSimulatedMFXLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.SIMULATED_MFX);
        DoubleMMDigitalLocomotive srcpLocomotive = (DoubleMMDigitalLocomotive) adapter
                .getOrCreateSrcpLocomotive(locomotive);

        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
        assertEquals(locomotive.getAddress2(), srcpLocomotive.getAddress2());

        locomotive.setBus(2);
        locomotive.setAddress1(3);
        locomotive.setAddress2(4);
        srcpLocomotive = (DoubleMMDigitalLocomotive) adapter
                .getOrCreateSrcpLocomotive(locomotive);

        assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    private Locomotive createLocomotive(final LocomotiveType type) {
        final Locomotive locomotive = new Locomotive();
        locomotive.setId("1");
        locomotive.setName("testname");
        locomotive.setDesc("description");
        locomotive.setBus(1);
        locomotive.setAddress1(1);
        locomotive.setAddress2(2);
        locomotive.setImage("image.png");
        locomotive.setType(type);
        return locomotive;
    }

}
