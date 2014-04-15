package ch.fork.AdHocRailway.railway.srcp;

import org.junit.Before;
import org.junit.Test;

public class SRCPTurnoutControlAdapterTest {

    private SRCPTurnoutControlAdapter adapter;

    @Before
    public void setup() {
        adapter = new SRCPTurnoutControlAdapter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createSRCPLocomotiveFromNull() {
        adapter.getOrCreateSRCPTurnout(null);
    }


}
