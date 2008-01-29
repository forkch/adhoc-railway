package ch.fork.AdHocRailway.domain.turnouts;

import ch.fork.AdHocRailway.domain.ControlObject;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GA;

public class SRCPTurnout extends ControlObject {

	private Turnout turnout;

	public enum TurnoutState {
		LEFT, STRAIGHT, RIGHT, UNDEF
	}

	protected TurnoutState turnoutState = TurnoutState.UNDEF;

	public static final String PROTOCOL = "M";
	private Turnout[] subTurnouts;
	private GA ga;
	private SRCPSession session;

	public SRCPTurnout(Turnout turnout) {
		this.turnout = turnout;
	}

	public GA getGA() {
		return this.ga;
	}

	protected void setGA(GA ga) {
		this.ga = ga;
	}

	public SRCPSession getSession() {
		return this.session;
	}

	protected void setSession(SRCPSession session) {
		this.session = session;
	}

	@Override
	public String getDeviceGroup() {
		return "GA";
	}

	public TurnoutState getTurnoutState() {
		return turnoutState;
	}

	protected void setTurnoutState(TurnoutState switchState) {
		this.turnoutState = switchState;
	}

	@Override
	public int[] getAddresses() {
		int[] addrs = new int[] { turnout.getAddress1(), turnout.getAddress2() };
		return addrs;
	}

	public TurnoutAddress[] getTurnoutAddresses() {
		return new TurnoutAddress[] {
				new TurnoutAddress(turnout.getAddress1(), turnout.getBus1(),
						turnout.isAddress1Switched()),
				new TurnoutAddress(turnout.getAddress2(), turnout.getBus2(),
						turnout.isAddress2Switched()) };
	}

	public TurnoutAddress getTurnoutAddress(int index) {
		return getTurnoutAddresses()[index];
	}

	protected Turnout[] getSubTurnouts() {
		return subTurnouts;
	}

	protected void setSubTurnouts(Turnout[] subTurnouts) {
		this.subTurnouts = subTurnouts;
	}

	public String toString() {

		String buf = "\"" + turnout.getNumber() + ": "
				+ turnout.getTurnoutType().getTypeName() + " @";

		buf += " " + getTurnoutAddress(0);
		if (turnout.getTurnoutType().getTypeName().equals("ThreeWay"))
			buf += " " + getTurnoutAddress(1);
		buf += " Group:" + turnout.getTurnoutGroup().toString();
		return buf;
	}
}
