package ch.fork.RailControl.domain.switches;

public class DoubleCrossSwitch extends DefaultSwitch {

    public DoubleCrossSwitch(int pNumber, String pDesc) {
        super(pNumber, pDesc);
    }

    public DoubleCrossSwitch(int pNumber, String pDesc, int pBus,
        Address pAddress) {
        super(pNumber, pDesc, pBus, pAddress);
    }

    @Override
    public Switch clone() {
        DoubleCrossSwitch newSwitch = new DoubleCrossSwitch(number, desc,
            bus, address);
        newSwitch.setSession(session);
        newSwitch.setSwitchOrientation(switchOrientation);
        newSwitch.setDefaultState(defaultState);
        return newSwitch;
    }
}
