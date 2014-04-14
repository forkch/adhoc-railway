package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.PowerChangeListener;
import ch.fork.AdHocRailway.model.power.Booster;
import ch.fork.AdHocRailway.model.power.BoosterState;
import ch.fork.AdHocRailway.model.power.PowerSupply;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

public class BrainPowerControlAdapterTest {

    @Mock
    private BrainController brainController;
    @Mock
    private PowerChangeListener listener;

    private BrainPowerControlAdapter testee;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void toggle_booster_0() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);

        givenTestee(powerSupply);

        whenToggleBooster(powerSupply.getBooster(0));

        assertBoosterPowerOnBrainCall(powerSupply.getBooster(0));

        whenInformTestee("XBS A O O O O O O O");

        whenToggleBooster(powerSupply.getBooster(0));

        assertBoosterPowerOffBrainCall(powerSupply.getBooster(0));

        assertListenerInformed(powerSupply);
    }

    private void assertListenerInformed(final PowerSupply powerSupply) {
        Mockito.verify(listener).powerChanged(powerSupply);
    }

    private void whenInformTestee(final String states) {
        testee.receivedMessage(states);
    }

    private void whenToggleBooster(final Booster booster) {
        testee.toggleBooster(booster);
    }

    @Test
    public void turn_booster_0_on() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenTurningBoosterOn(powerSupply.getBooster(0));

        assertBoosterPowerOnBrainCall(powerSupply.getBooster(0));

        whenInformTestee("XBS A O O O O O O O");

        Assert.assertEquals(BoosterState.ACTIVE, powerSupply.getBooster(0)
                .getState());
        assertListenerInformed(powerSupply);
    }

    @Test
    public void turn_booster_0_on_shortcuts() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenTurningBoosterOn(powerSupply.getBooster(0));

        assertBoosterPowerOnBrainCall(powerSupply.getBooster(0));

        whenInformTestee("XBS S O O O O O O O");

        Assert.assertEquals(BoosterState.SHORTCUT, powerSupply.getBooster(0)
                .getState());
        assertListenerInformed(powerSupply);
    }

    @Test(expected = ControllerException.class)
    public void turn_booster_0_on_exception() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenBoosterPowerThrowsException(powerSupply.getBooster(0), "XGO");

        whenTurningBoosterOn(powerSupply.getBooster(0));

    }

    @Test
    public void turn_booster_0_off() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenTurningBoosterOff(powerSupply.getBooster(0));

        assertBoosterPowerOffBrainCall(powerSupply.getBooster(0));

        whenInformTestee("XBS O O O O O O O O");

        Assert.assertEquals(BoosterState.INACTIVE, powerSupply.getBooster(0)
                .getState());
        assertListenerInformed(powerSupply);
    }

    @Test(expected = ControllerException.class)
    public void turn_booster_0_off_exception() {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenBoosterPowerThrowsException(powerSupply.getBooster(0), "XSTOP");
        whenTurningBoosterOff(powerSupply.getBooster(0));

    }

    @Test
    public void turn_all_boosters_on() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenTurningAllBoostersOn(powerSupply);

        assertAllBoostersPowerOnBrainCall(powerSupply);

        whenInformTestee("XBS A A A A A A A A");

        for (final Booster booster : powerSupply.getBoosters()) {

            Assert.assertEquals(BoosterState.ACTIVE, booster.getState());
        }

        assertListenerInformed(powerSupply);
    }

    @Test
    public void turn_all_boosters_on_shortcut() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenTurningAllBoostersOn(powerSupply);

        assertAllBoostersPowerOnBrainCall(powerSupply);

        whenInformTestee("XBS S S S S S S S S");

        for (final Booster booster : powerSupply.getBoosters()) {

            Assert.assertEquals(BoosterState.SHORTCUT, booster.getState());
        }
        assertListenerInformed(powerSupply);

    }

    @Test(expected = ControllerException.class)
    public void turn_all_boosters_on_exception() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);
        whenAllBoosterPowerThrowsException("X!");
        whenTurningAllBoostersOn(powerSupply);

    }

    @Test
    public void turn_all_boosters_off() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenTurningAllBoostersOff(powerSupply);

        assertAllBoostersPowerOffBrainCall(powerSupply);

        whenInformTestee("XBS O O O O O O O O");

        for (final Booster booster : powerSupply.getBoosters()) {

            Assert.assertEquals(BoosterState.INACTIVE, booster.getState());
        }
        assertListenerInformed(powerSupply);

    }

    @Test(expected = ControllerException.class)
    public void turn_all_boosters_off_exception() throws IOException {
        final PowerSupply powerSupply = new PowerSupply(1);
        givenTestee(powerSupply);

        whenAllBoosterPowerThrowsException("X.");
        whenTurningAllBoostersOff(powerSupply);

    }

    private void givenTestee(final PowerSupply powerSupply) {
        testee = new BrainPowerControlAdapter(brainController);
        testee.addOrUpdatePowerSupply(powerSupply);
        testee.addPowerChangeListener(listener);
    }

    private void whenBoosterPowerThrowsException(final Booster booster,
                                                 final String onOff) {
        Mockito.doThrow(new ControllerException()).when(brainController)
                .write(onOff + " " + booster.getBoosterNumber());
    }

    private void whenAllBoosterPowerThrowsException(final String onOff) {
        Mockito.doThrow(new ControllerException()).when(brainController)
                .write(onOff);
    }

    private void whenTurningBoosterOn(final Booster booster) {
        testee.boosterOn(booster);
    }

    private void whenTurningBoosterOff(final Booster booster) {
        testee.boosterOff(booster);
    }

    private void whenTurningAllBoostersOn(final PowerSupply supply) {
        testee.powerOn(supply);
    }

    private void whenTurningAllBoostersOff(final PowerSupply powerSupply) {
        testee.powerOff(powerSupply);
    }

    private void assertBoosterPowerOnBrainCall(final Booster booster)
            throws IOException {
        assertPowerBrainCall(booster, "XGO");
    }

    private void assertBoosterPowerOffBrainCall(final Booster booster)
            throws IOException {
        assertPowerBrainCall(booster, "XSTOP");
    }

    private void assertPowerBrainCall(final Booster booster, final String onOff)
            throws IOException {
        Mockito.verify(brainController).write(
                onOff + " " + booster.getBoosterNumber());
    }

    private void assertAllBoostersPowerOnBrainCall(final PowerSupply powerSupply)
            throws IOException {
        Mockito.verify(brainController).write("X!");
    }

    private void assertAllBoostersPowerOffBrainCall(
            final PowerSupply powerSupply) throws IOException {
        Mockito.verify(brainController).write("X.");
    }

}
