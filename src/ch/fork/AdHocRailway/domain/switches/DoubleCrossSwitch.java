
package ch.fork.AdHocRailway.domain.switches;

import ch.fork.AdHocRailway.domain.Address;

public class DoubleCrossSwitch extends DefaultSwitch {
    public DoubleCrossSwitch(int pNumber, String pDesc) {
        super(pNumber, pDesc);
    }

    public DoubleCrossSwitch(int pNumber, String pDesc, Address address) {
        super(pNumber, pDesc, address);
    }

    @Override
    public Switch clone() {
        DoubleCrossSwitch newSwitch = new DoubleCrossSwitch(number, desc,
            addresses[0]);
        newSwitch.setSession(session);
        newSwitch.setSwitchOrientation(switchOrientation);
        newSwitch.setDefaultState(defaultState);
        return newSwitch;
    }
}
