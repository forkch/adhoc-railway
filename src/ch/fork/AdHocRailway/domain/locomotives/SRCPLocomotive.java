package ch.fork.AdHocRailway.domain.locomotives;

import javax.persistence.Transient;

import ch.fork.AdHocRailway.domain.ControlObject;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GL;

public class SRCPLocomotive extends ControlObject {

	public enum Direction {
		FORWARD, REVERSE, UNDEF
	};

	
	protected Direction direction = Direction.UNDEF;


	protected static final String FORWARD_DIRECTION = "1";

	protected static final String REVERSE_DIRECTION = "0";
	
	
	protected int currentSpeed = 0;

	
	private GL gl;

	
	private SRCPSession session;

	protected boolean[] functions = new boolean[] { false, false, false, false, false };

	protected String[] params;

	private Locomotive locomotive;
	
	public SRCPLocomotive(Locomotive locomotive) {
		this.locomotive = locomotive;
	}
	
	public int getCurrentSpeed() {
		return currentSpeed;
	}

	
	protected void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}
	
	
	public Direction getDirection() {
		return direction;
	}
	
	protected void setDirection(Direction direction) {
		this.direction = direction;
	}

	
	public boolean[] getFunctions() {
		return functions;
	}
	
	
	public void setFunctions(boolean[] functions) {
		this.functions = functions;
	}

	
	public GL getGL() {
		return this.gl;
	}

	
	protected void setGL(GL gl) {
		this.gl = gl;
	}

	
	public SRCPSession getSession() {
		return this.session;
	}

	
	protected void setSession(SRCPSession session) {
		this.session = session;
	}
	
	@Override
	public int[] getAddresses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDeviceGroup() {
		// TODO Auto-generated method stub
		return null;
	}

}
