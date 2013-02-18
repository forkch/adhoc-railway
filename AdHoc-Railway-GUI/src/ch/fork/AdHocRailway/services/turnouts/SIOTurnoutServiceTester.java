package ch.fork.AdHocRailway.services.turnouts;

import java.io.IOException;
import java.util.List;

import org.jboss.logging.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public class SIOTurnoutServiceTester implements TurnoutServiceListener {

	private static Logger LOGGER = Logger.getLogger(SIOTurnoutService.class);
	boolean ready = false;

	public SIOTurnoutServiceTester() throws InterruptedException, IOException {
		LOGGER.info("start");
		TurnoutService service = SIOTurnoutService.getInstance();
		service.init(this);

		TurnoutGroup turnoutGroup = new TurnoutGroup();
		turnoutGroup.setName("test");
		service.addTurnoutGroup(turnoutGroup);
		Turnout turnout = new Turnout(1, TurnoutType.DEFAULT, 1, 1, false,
				TurnoutState.LEFT, TurnoutOrientation.EAST, "desdcsldkfjs",
				turnoutGroup);
		service.addTurnout(turnout);

		service.disconnect();
	}

	@Override
	public void turnoutUpdated(Turnout turnout) {
		System.out.println("turnoutUpdated()");
		System.out.println(turnout);

	}

	@Override
	public void turnoutRemoved(Turnout turnout) {
		System.out.println("turnoutRemoved()");
		System.out.println(turnout);

	}

	@Override
	public void turnoutAdded(Turnout turnout) {
		System.out.println("turnoutAdded()");
		System.out.println(turnout);

	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws Exception {
		new SIOTurnoutServiceTester();
	}

	@Override
	public void turnoutsUpdated(List<TurnoutGroup> turnoutGroups) {
		System.out.println("turnoutsUpdated()");
		System.out.println(turnoutGroups);
	}

	@Override
	public void turnoutGroupAdded(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void turnoutGroupRemoved(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void turnoutGroupUpdated(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void failure(TurnoutManagerException arg0) {
		LOGGER.error(arg0);

	}

	@Override
	public void ready() {
		// TODO Auto-generated method stub

	}
}
