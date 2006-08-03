
package ch.fork.AdHocRailway.domain;


public class Address {
    private int     bus;
    private int     address;
    private boolean addressSwitched = false;

    public Address(int bus, int address) {
        this.bus = bus;
        this.address = address;
    }

    public Address(String anAddress) {
        address = Integer.parseInt(anAddress);
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int hashCode() {
        return Integer.valueOf(bus).hashCode()*1000 + Integer.valueOf(address).hashCode();
    }

    public boolean equals(Object anAddress) {
        if (anAddress instanceof Address) {
            Address ad = (Address) anAddress;
            if (ad.bus == bus && ad.address == address
                && ad.addressSwitched == addressSwitched) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return " bus: " + bus + " address: " + address + " : " + addressSwitched;
    }

    public boolean isAddressSwitched() {
        return addressSwitched;
    }

    public void setAddressSwitched(boolean addressSwitched) {
        this.addressSwitched = addressSwitched;
    }

    public int getBus() {
        return bus;
    }

    public void setBus(int bus) {
        this.bus = bus;
    }

}
