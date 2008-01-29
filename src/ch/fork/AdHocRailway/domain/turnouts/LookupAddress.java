package ch.fork.AdHocRailway.domain.turnouts;

public class LookupAddress {
	private int bus1;
	private int address1;
	private int bus2;
	private int address2;
	public LookupAddress(int bus1, int address1, int bus2, int address2) {
		super();
		this.bus1 = bus1;
		this.address1 = address1;
		this.bus2 = bus2;
		this.address2 = address2;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address1;
		result = prime * result + address2;
		result = prime * result + bus1;
		result = prime * result + bus2;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final LookupAddress other = (LookupAddress) obj;
		if (address1 != other.address1)
			return false;
		if (address2 != other.address2)
			return false;
		if (bus1 != other.bus1)
			return false;
		if (bus2 != other.bus2)
			return false;
		return true;
	}
	
	public String toString() {
		return bus1 + "," + address1 + "," + bus2 + "," + address2; 
	}
}
