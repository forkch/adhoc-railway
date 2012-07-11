package ch.fork.AdHocRailway.services.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public class HibernateTurnoutMapper {

	public static Turnout map(HibernateTurnout hTurnout) {
		Turnout t = new Turnout();
		t.setId(hTurnout.getId());
		t.setNumber(hTurnout.getNumber());
		t.setBus1(hTurnout.getBus1());
		t.setBus2(hTurnout.getBus2());
		t.setAddress1(hTurnout.getAddress1());
		t.setAddress1Switched(hTurnout.isAddress1Switched());
		t.setAddress2(hTurnout.getAddress2());
		t.setAddress2Switched(hTurnout.isAddress2Switched());
		t.setDefaultState(hTurnout.getDefaultState());
		t.setDefaultStateEnum(hTurnout.getDefaultStateEnum());
		t.setDescription(hTurnout.getDescription());
		t.setOrientation(hTurnout.getOrientation());

		t.setTurnoutGroupId(hTurnout.getTurnoutGroup().getId());
		t.setTurnoutTypeId(hTurnout.getTurnoutType().getId());
		// FIXME
		// t.setOrientationEnum(hTurnout.getOrientationEnum());
		return t;
	}

	public static TurnoutGroup map(HibernateTurnoutGroup hGroup) {
		TurnoutGroup group = new TurnoutGroup();
		group.setId(hGroup.getId());
		group.setName(hGroup.getName());
		group.setTurnoutNumberAmount(hGroup.getTurnoutNumberAmount());
		group.setTurnoutNumberOffset(hGroup.getTurnoutNumberOffset());
		return group;
	}

	public static TurnoutType map(HibernateTurnoutType hType) {
		TurnoutType type = new TurnoutType();
		type.setId(hType.getId());
		type.setTypeName(hType.getTypeName());
		return type;
	}

}
