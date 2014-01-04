package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.TurnoutChangeListener;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

public class BrainTurnoutControlAdapterTest {

    @Mock
    private BrainController brainController;
    @Mock
    private TurnoutChangeListener listener;

    private BrainTurnoutControlAdapter testee;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void set_straight() throws IOException {
        final Turnout t = createTurnout(1, false);

        givenTestee(t);

        whenSetTurnoutStraight(t);

        assertBrainCalled(t, "g");
        assertListenerInformed(t, TurnoutState.STRAIGHT);
    }

    @Test
    public void set_straight_inverted() throws IOException {
        final Turnout t = createTurnout(1, true);

        givenTestee(t);

        whenSetTurnoutStraight(t);

        assertBrainCalled(t, "r");
        assertListenerInformed(t, TurnoutState.STRAIGHT);
    }

    @Test
    public void set_curved_left() throws IOException {
        final Turnout t = createTurnout(1, false);

        givenTestee(t);

        whenSetTurnoutCurvedLeft(t);

        assertBrainCalled(t, "r");
        assertListenerInformed(t, TurnoutState.LEFT);
    }

    @Test
    public void set_curved_left_inverted() throws IOException {
        final Turnout t = createTurnout(1, true);

        givenTestee(t);

        whenSetTurnoutCurvedLeft(t);

        assertBrainCalled(t, "g");
        assertListenerInformed(t, TurnoutState.LEFT);
    }

    @Test
    public void set_threeway_straight() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, false, false);

        givenTestee(t);

        whenSetTurnoutStraight(t);

        assertBrainCalledForThreeway(t, "g", "g");
        assertListenerInformed(t, TurnoutState.STRAIGHT);
    }

    @Test
    public void set_threeway_left() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, false, false);

        givenTestee(t);

        whenSetTurnoutCurvedLeft(t);

        assertBrainCalledForThreeway(t, "r", "g");
        assertListenerInformed(t, TurnoutState.LEFT);
    }

    @Test
    public void set_threeway_right() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, false, false);

        givenTestee(t);

        whenSetTurnoutCurvedRight(t);

        assertBrainCalledForThreeway(t, "g", "r");
        assertListenerInformed(t, TurnoutState.RIGHT);
    }

    @Test
    public void set_threeway_straight_inverted() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, true, true);

        givenTestee(t);

        whenSetTurnoutStraight(t);

        assertBrainCalledForThreeway(t, "r", "r");
        assertListenerInformed(t, TurnoutState.STRAIGHT);
    }

    @Test
    public void set_threeway_left_inverted() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, true, true);

        givenTestee(t);

        whenSetTurnoutCurvedLeft(t);

        assertBrainCalledForThreeway(t, "g", "r");
        assertListenerInformed(t, TurnoutState.LEFT);
    }

    @Test
    public void set_threeway_right_inverted() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, true, true);

        givenTestee(t);

        whenSetTurnoutCurvedRight(t);

        assertBrainCalledForThreeway(t, "r", "g");
        assertListenerInformed(t, TurnoutState.RIGHT);
    }

    @Test
    @Ignore
    public void toggle_turnout_from_undef_state() throws IOException {
        final Turnout t = createTurnout(1, false);

        givenTestee(t);

        whenToggleTurnout(t);

        assertBrainCalled(t, "g");
        assertListenerInformed(t, TurnoutState.STRAIGHT);

        whenToggleTurnout(t);

        assertBrainCalled(t, "r");
        assertListenerInformed(t, TurnoutState.LEFT);
    }

    @Test
    @Ignore
    public void toggle_threeway_turnout_from_undef_state() throws IOException {
        final Turnout t = createThreewayTurnout(1, 2, false, false);

        givenTestee(t);

        whenToggleTurnout(t);

        assertListenerInformed(t, TurnoutState.STRAIGHT);

        whenToggleTurnout(t);

        assertListenerInformed(t, TurnoutState.LEFT);

        whenToggleTurnout(t);

        assertListenerInformed(t, TurnoutState.RIGHT);
        assertBrainCalledForThreeway(t, "g", "g", "r", "g", "g", "r");
    }

    private void whenToggleTurnout(final Turnout t) {
        testee.toggle(t);
    }

    private Turnout createTurnout(final int address, final boolean inverted) {
        final Turnout t = new Turnout();
        t.setAddress1(address);
        t.setAddress1Switched(inverted);
        t.setTurnoutType(TurnoutType.DEFAULT_LEFT);
        return t;
    }

    private Turnout createThreewayTurnout(final int address1,
                                          final int address2, final boolean inverted1, final boolean inverted2) {
        final Turnout t = new Turnout();
        t.setAddress1(address1);
        t.setAddress1Switched(inverted1);
        t.setAddress2(address2);
        t.setAddress2Switched(inverted2);
        t.setTurnoutType(TurnoutType.THREEWAY);
        return t;
    }

    private void givenTestee(final Turnout t) {
        testee = new BrainTurnoutControlAdapter(brainController);
        testee.addTurnoutChangeListener(t, listener);
    }

    private void whenSetTurnoutStraight(final Turnout t) {
        testee.setStraight(t);
    }

    private void whenSetTurnoutCurvedLeft(final Turnout t) {
        testee.setCurvedLeft(t);
    }

    private void whenSetTurnoutCurvedRight(final Turnout t) {
        testee.setCurvedRight(t);
    }

    private void assertBrainCalled(final Turnout t, final String port) {
        Mockito.verify(brainController).write(
                "XT " + t.getAddress1() + " " + port);
    }

    private void assertBrainCalledForThreeway(final Turnout t,
                                              final String... ports) {
        final ArgumentCaptor<String> brainCommandCaptor = ArgumentCaptor
                .forClass(String.class);
        verify(brainController, Mockito.times(ports.length)).write(
                brainCommandCaptor.capture());

        final List<String> capturedCalls = brainCommandCaptor.getAllValues();
        if (capturedCalls.size() != ports.length) {
            fail();
        }
        for (int i = 0; i < ports.length; i = i + 2) {
            assertEquals("XT " + t.getAddress1() + " " + ports[i],
                    capturedCalls.get(i));
            assertEquals("XT " + t.getAddress2() + " " + ports[i + 1],
                    capturedCalls.get(i + 1));
        }
    }

    private void assertListenerInformed(final Turnout t,
                                        final TurnoutState state) {
        Mockito.verify(listener).turnoutChanged(t);

    }
}
