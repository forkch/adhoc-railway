package ch.fork.AdHocRailway.model.power;

import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("boosterNumber").append("state").build();
    }

    public BoosterState getToggeledState() {
        switch (this.state) {

            case ACTIVE:
                return BoosterState.INACTIVE;
            case SHORTCUT:
                return BoosterState.ACTIVE;
            case INACTIVE:
            default:
                return BoosterState.ACTIVE;
        }
    }
}
