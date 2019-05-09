package de.dermoba.srcp.model.turnouts;

public class MMTurnout extends SRCPTurnout {

	public final static int MAX_MM_TURNOUT_ADDRESS = 968;

	public final static String PROTOCOL = "M";

	public MMTurnout() {
		this(0, false, 0, SRCPTurnoutState.UNDEF, SRCPTurnoutTypes.DEFAULT);
	}

	public MMTurnout(final int address1, final boolean address1Switched,
			final int address2, final boolean address2Switched, final int bus1,
			final int bus2, final SRCPTurnoutState defaultState,
			final SRCPTurnoutTypes turnoutType) {
		super(address1, address1Switched, address2, address2Switched, bus1,
				bus2, defaultState, turnoutType);
		protocol = PROTOCOL;
	}

	public MMTurnout(final int address1, final boolean address1Switched,
			final int bus1, final SRCPTurnoutState defaultState,
			final SRCPTurnoutTypes turnoutType) {
		this(address1, address1Switched, 0, false, 0, 0, defaultState,
				turnoutType);

	}

	@Override
	public boolean checkAddress() {
		switch (turnoutType) {
		case DEFAULT:
		case DOUBLECROSS:
		case CUTTER:
			return !(address1 <= 0 || address1 > MMTurnout.MAX_MM_TURNOUT_ADDRESS);
		case THREEWAY:
			return !(address1 <= 0
					|| address1 > MMTurnout.MAX_MM_TURNOUT_ADDRESS
					|| address2 <= 0 || address2 > MMTurnout.MAX_MM_TURNOUT_ADDRESS);
		}
		return false;
	}

	@Override
	public Object clone() {
		final MMTurnout newTurnout = new MMTurnout(address1, address1Switched,
				address2, address2Switched, bus1, bus2, defaultState,
				turnoutType);
		return newTurnout;
	}

}
