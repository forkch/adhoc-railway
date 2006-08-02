
package ch.fork.AdHocRailway.domain.switches;

import java.util.StringTokenizer;

public class Address {
    private int address1;
    private int address2;
    private int bus;
    private boolean address1Turned;
    private boolean address2Turned;

    public Address(int address1) {
        this(address1, 0);
    }

    public Address(int address1, int address2) {
        this.address1 = address1;
        this.address2 = address2;
    }

    public Address(String address) {
        StringTokenizer token = new StringTokenizer(address, ",");
        address1 = Integer.parseInt(token.nextToken().trim());
        address2 = Integer.parseInt(token.nextToken().trim());
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<Address address1=\"" + address1 + "\" address2=\""
            + address2 + "\" />");
        return sb.toString();
    }

    public int getAddress1() {
        return address1;
    }

    public void setAddress1(int address1) {
        this.address1 = address1;
    }

    public int getAddress2() {
        return address2;
    }

    public void setAddress2(int address2) {
        this.address2 = address2;
    }

    public int hashCode() {
        return Integer.valueOf(address1).hashCode()
            + Integer.valueOf(address2).hashCode();
    }

    public boolean equals(Object address){
        if(address instanceof Address){
            Address ad = (Address)address;
            if (ad.address1 == address1 && ad.address2 == address2) {
                return true;
            }
        }   
        return false;
    }

    public String toString() {
        return address1 + ", " + address2;
    }
}
