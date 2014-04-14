package ch.fork.AdHocRailway.model.power;

public class Booster {

    private final int boosterNumber;
    private final PowerSupply supply;
    private BoosterState state;

    public Booster(final PowerSupply supply, final int boosterNumber) {
        this.supply = supply;
        this.boosterNumber = boosterNumber;
        state = BoosterState.INACTIVE;

    }

    public int getBoosterNumber() {
        return boosterNumber;
    }

    public BoosterState getState() {
        return state;
    }

    public void setState(final BoosterState state) {
        this.state = state;
    }

    public PowerSupply getSupply() {
        return supply;
    }

}
