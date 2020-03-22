package ch.fork.adhocrailway.railway.srcp;

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveType;
import de.dermoba.srcp.model.locomotives.DoubleMMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.MMDeltaLocomotive;
import de.dermoba.srcp.model.locomotives.MMDigitalLocomotive;
import de.dermoba.srcp.model.locomotives.SRCPLocomotive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SRCPLocomotiveControlAdapterTest {

    private SRCPLocomotiveControlAdapter testee;

    @Before
    public void setup() {
        testee = new SRCPLocomotiveControlAdapter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createSRCPLocomotiveFromNull() {
        testee.getOrCreateSrcpLocomotive(null);
    }

    @Test
    public void addDeltaSRCPLocomotive() {
        // given
        final Locomotive locomotive = createLocomotive(LocomotiveType.DELTA);

        // when
        final SRCPLocomotive srcpLocomotive = testee
                .getOrCreateSrcpLocomotive(locomotive);

        // then
        assertTrue(srcpLocomotive instanceof MMDeltaLocomotive);
        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    @Test
    public void addDigitalSRCPLocomotive() {
        // given
        final Locomotive locomotive = createLocomotive(LocomotiveType.DIGITAL);

        // when
        final SRCPLocomotive srcpLocomotive = testee
                .getOrCreateSrcpLocomotive(locomotive);

        // then
        assertTrue(srcpLocomotive instanceof MMDigitalLocomotive);
        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    @Test
    public void addSimulatedMFXSRCPLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.SIMULATED_MFX);
        final DoubleMMDigitalLocomotive srcpLocomotive = (DoubleMMDigitalLocomotive) testee
                .getOrCreateSrcpLocomotive(locomotive);

        assertTrue(srcpLocomotive instanceof DoubleMMDigitalLocomotive);
        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
        Assert.assertEquals(locomotive.getAddress2(), srcpLocomotive.getAddress2());
    }

    @Test
    public void updateExistingDeltaLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.DELTA);
        SRCPLocomotive srcpLocomotive = testee.getOrCreateSrcpLocomotive(locomotive);

        assertTrue(srcpLocomotive instanceof MMDeltaLocomotive);
        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());

        locomotive.setBus(2);
        locomotive.setAddress1(3);
        srcpLocomotive = testee.getOrCreateSrcpLocomotive(locomotive);

        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    @Test
    public void updateExistingSimulatedMFXLocomotive() {
        final Locomotive locomotive = createLocomotive(LocomotiveType.SIMULATED_MFX);
        DoubleMMDigitalLocomotive srcpLocomotive = (DoubleMMDigitalLocomotive) testee
                .getOrCreateSrcpLocomotive(locomotive);

        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
        Assert.assertEquals(locomotive.getAddress2(), srcpLocomotive.getAddress2());

        locomotive.setBus(2);
        locomotive.setAddress1(3);
        locomotive.setAddress2(4);
        srcpLocomotive = (DoubleMMDigitalLocomotive) testee
                .getOrCreateSrcpLocomotive(locomotive);

        Assert.assertEquals(locomotive.getBus(), srcpLocomotive.getBus());
        Assert.assertEquals(locomotive.getAddress1(), srcpLocomotive.getAddress());
    }

    private Locomotive createLocomotive(final LocomotiveType type) {
        final Locomotive locomotive = new Locomotive();
        locomotive.setId("1");
        locomotive.setName("testname");
        locomotive.setDesc("description");
        locomotive.setBus(2);
        locomotive.setAddress1(1);
        locomotive.setAddress2(2);
        locomotive.setImage("image.png");
        locomotive.setType(type);
        return locomotive;
    }

}
