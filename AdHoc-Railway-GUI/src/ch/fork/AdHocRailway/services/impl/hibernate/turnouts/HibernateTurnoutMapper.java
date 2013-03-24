package ch.fork.AdHocRailway.services.impl.hibernate.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public class HibernateTurnoutMapper {

	public static Turnout mapTurnout(final HibernateTurnout hTurnout) {
		final Turnout turnout = new Turnout();
		updateTurnout(turnout, hTurnout);
		return turnout;
	}

	public static TurnoutOrientation mapOrientation(final String orientation) {
		if (orientation.equalsIgnoreCase("NORTH")) {
			return TurnoutOrientation.NORTH;
		} else if (orientation.equalsIgnoreCase("EAST")) {
			return TurnoutOrientation.EAST;
		} else if (orientation.equalsIgnoreCase("SOUTH")) {
			return TurnoutOrientation.SOUTH;
		} else if (orientation.equalsIgnoreCase("WEST")) {
			return TurnoutOrientation.WEST;
		}
		return null;
	}

	public static String mapOrientation(final TurnoutOrientation orientation) {
		return orientation.toString();
	}

	public static TurnoutState mapTurnoutState(final String defaultState) {
		if (defaultState.equalsIgnoreCase("STRAIGHT")) {
			return TurnoutState.STRAIGHT;
		} else if (defaultState.equalsIgnoreCase("LEFT")) {
			return TurnoutState.LEFT;
		} else if (defaultState.equalsIgnoreCase("RIGHT")) {
			return TurnoutState.RIGHT;
		}
		return TurnoutState.UNDEF;
	}

	public static String mapTurnoutState(final TurnoutState defaultState) {
		switch (defaultState) {
		case STRAIGHT:
			return "STRAIGHT";
		case LEFT:
			return "LEFT";
		case RIGHT:
			return "RIGHT";
		default:
			return "UNDEF";
		}
	}

	public static TurnoutGroup map(final HibernateTurnoutGroup hGroup) {
		final TurnoutGroup group = new TurnoutGroup();
		group.setId(hGroup.getId());
		group.setName(hGroup.getName());
		group.setTurnoutNumberAmount(hGroup.getTurnoutNumberAmount());
		group.setTurnoutNumberOffset(hGroup.getTurnoutNumberOffset());
		for (final HibernateTurnout hTurnout : hGroup.getTurnouts()) {
			final Turnout turnout = mapTurnout(hTurnout);
			turnout.setTurnoutGroup(group);
			group.addTurnout(turnout);
		}
		return group;
	}

	public static HibernateTurnout map(final Turnout turnout) {
		final HibernateTurnout hTurnout = new HibernateTurnout();

		update(turnout, hTurnout);

		return hTurnout;
	}

	private static String mapType(final TurnoutType turnoutType) {
		switch (turnoutType) {
		case CUTTER:
			return "CUTTER";
		case DEFAULT:
			return "DEFAULT";
		case DOUBLECROSS:
			return "DOUBLECROSS";
		case THREEWAY:
			return "THREEWAY";
		default:
			return "UNKNOWN";

		}
	}

	private static TurnoutType mapType(final String turnoutType) {
		if (turnoutType.equalsIgnoreCase("DEFAULT")) {
			return TurnoutType.DEFAULT;
		} else if (turnoutType.equalsIgnoreCase("DOUBLECROSS")) {
			return TurnoutType.DOUBLECROSS;
		} else if (turnoutType.equalsIgnoreCase("THREEWAY")) {
			return TurnoutType.THREEWAY;
		} else if (turnoutType.equalsIgnoreCase("CUTTER")) {
			return TurnoutType.CUTTER;
		}
		return null;
	}

	public static HibernateTurnoutGroup map(final TurnoutGroup group) {
		final HibernateTurnoutGroup hTurnoutGroup = new HibernateTurnoutGroup();

		hTurnoutGroup.setName(group.getName());
		hTurnoutGroup.setTurnoutNumberAmount(group.getTurnoutNumberAmount());
		hTurnoutGroup.setTurnoutNumberOffset(group.getTurnoutNumberOffset());

		return hTurnoutGroup;
	}

	public static void update(final Turnout turnout,
			final HibernateTurnout hTurnout) {
		hTurnout.setId(turnout.getId());
		hTurnout.setNumber(turnout.getNumber());
		hTurnout.setBus1(turnout.getBus1());
		hTurnout.setBus2(turnout.getBus2());
		hTurnout.setAddress1(turnout.getAddress1());
		hTurnout.setAddress1Switched(turnout.isAddress1Switched());
		hTurnout.setAddress2(turnout.getAddress2());
		hTurnout.setAddress2Switched(turnout.isAddress2Switched());
		hTurnout.setDefaultState(mapTurnoutState(turnout.getDefaultState()));
		hTurnout.setDescription(turnout.getDescription());
		hTurnout.setOrientation(mapOrientation(turnout.getOrientation()));
		hTurnout.setTurnoutType(mapType(turnout.getTurnoutType()));
	}

	public static void updateTurnout(final Turnout turnout,
			final HibernateTurnout hTurnout) {
		turnout.setId(hTurnout.getId());
		turnout.setNumber(hTurnout.getNumber());
		turnout.setBus1(hTurnout.getBus1());
		turnout.setBus2(hTurnout.getBus2());
		turnout.setAddress1(hTurnout.getAddress1());
		turnout.setAddress1Switched(hTurnout.isAddress1Switched());
		turnout.setAddress2(hTurnout.getAddress2());
		turnout.setAddress2Switched(hTurnout.isAddress2Switched());
		turnout.setDefaultState(mapTurnoutState(hTurnout.getDefaultState()));
		turnout.setDescription(hTurnout.getDescription());
		turnout.setOrientation(mapOrientation(hTurnout.getOrientation()));
		turnout.setTurnoutType(mapType(hTurnout.getTurnoutType()));
	}

	public static void updateHibernate(final HibernateTurnout hTurnout,
			final Turnout turnout) {
		hTurnout.setId(turnout.getId());
		hTurnout.setNumber(turnout.getNumber());
		hTurnout.setBus1(turnout.getBus1());
		hTurnout.setBus2(turnout.getBus2());
		hTurnout.setAddress1(turnout.getAddress1());
		hTurnout.setAddress1Switched(turnout.isAddress1Switched());
		hTurnout.setAddress2(turnout.getAddress2());
		hTurnout.setAddress2Switched(turnout.isAddress2Switched());
		hTurnout.setDefaultState(mapTurnoutState(turnout.getDefaultState()));
		hTurnout.setDescription(turnout.getDescription());
		hTurnout.setOrientation(mapOrientation(turnout.getOrientation()));
		hTurnout.setTurnoutType(mapType(turnout.getTurnoutType()));
	}

}
