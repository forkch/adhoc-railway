package ch.fork.AdHocRailway.services.turnouts;

import java.util.List;

import org.jboss.logging.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public class SocketIOTurnoutServiceTester implements TurnoutServiceListener {

	private static Logger LOGGER = Logger
			.getLogger(SocketIOTurnoutService.class);
	boolean ready = false;

	public SocketIOTurnoutServiceTester() throws InterruptedException {
		LOGGER.info("start");
		TurnoutService service = SocketIOTurnoutService.getInstance();
		service.init(this);

		TurnoutGroup turnoutGroup = new TurnoutGroup();
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
	public static void main(String[] args) throws InterruptedException {
		new SocketIOTurnoutServiceTester();
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
	public void turnoutGroupDeleted(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void turnoutGroupUpdated(TurnoutGroup group) {
		// TODO Auto-generated method stub

	}
}
