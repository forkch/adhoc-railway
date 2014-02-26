package ch.fork.AdHocRailway.domain.power;

public class Booster {

    private final int boosterNumber;
    private BoosterState state;
    private final PowerSupply supply;

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
