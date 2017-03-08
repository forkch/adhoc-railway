/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Locomotive.java 308 2013-05-01 15:43:50Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.model.locomotives;

import ch.fork.AdHocRailway.model.AbstractItem;
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;

import static ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection.FORWARD;
import static ch.fork.AdHocRailway.model.locomotives.LocomotiveDirection.REVERSE;

public class Locomotive extends AbstractItem implements Serializable,
        Comparable<Locomotive> {

    public static final String PROPERTYNAME_ID = "id";
    public static final String PROPERTYNAME_NAME = "name";
    public static final String PROPERTYNAME_DESCRIPTION = "desc";
    public static final String PROPERTYNAME_IMAGE = "image";
    public static final String PROPERTYNAME_LOCOMOTIVE_TYPE = "type";
    public static final String PROPERTYNAME_ADDRESS1 = "address1";
    public static final String PROPERTYNAME_ADDRESS2 = "address2";
    public static final String PROPERTYNAME_MFX_UUID = "mfxUUID";
    public static final String PROPERTYNAME_BUS = "bus";
    public static final String PROPERTYNAME_FUNCTIONS = "functions";
    public static final String PROPERTYNAME_LOCOMOTIVE_GROUP = "group";
    @XStreamAsAttribute
    @Expose
    private String id = UUID.randomUUID().toString();
    @XStreamAsAttribute
    @Expose
    private String name;
    @XStreamAsAttribute
    @Expose
    private String groupId;
    @XStreamAsAttribute
    @Expose
    private String desc;
    @XStreamAsAttribute
    @Expose
    private String image;
    @Expose
    private String imageBase64;
    @XStreamAsAttribute
    @Expose
    private LocomotiveType type;
    @XStreamAsAttribute
    @Expose
    private int bus;
    @XStreamAsAttribute
    @Expose
    private int address1;
    @XStreamAsAttribute
    @Expose
    private int address2;
    @Expose
    private int mfxUUID;
    @Expose
    private Set<LocomotiveFunction> functions = Sets.newHashSet();

    private transient LocomotiveGroup group;

    private transient int currentSpeed = 0;
    private transient int targetSpeed = -1;

    private transient LocomotiveDirection currentDirection = FORWARD;

    private transient boolean[] currentFunctions;

    public Locomotive() {
    }

    @Override
    public void init() {
        super.init();
        currentSpeed = 0;
        currentDirection = FORWARD;
        currentFunctions = new boolean[functions.size()];
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        final String old = this.id;
        this.id = id;
        changeSupport.firePropertyChange(PROPERTYNAME_ID, old, this.id);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        final String old = this.name;
        this.name = name;
        changeSupport.firePropertyChange(PROPERTYNAME_NAME, old, this.name);
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(final String description) {
        final String old = this.desc;
        this.desc = description;
        changeSupport.firePropertyChange(PROPERTYNAME_DESCRIPTION, old,
                this.desc);
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(final String image) {
        final String old = this.image;
        this.image = image;
        changeSupport.firePropertyChange(PROPERTYNAME_IMAGE, old, this.image);
    }

    public LocomotiveType getType() {
        return this.type;
    }

    public void setType(final LocomotiveType locomotiveType) {
        final LocomotiveType old = this.type;
        this.type = locomotiveType;
        changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVE_TYPE, old,
                this.type);
    }

    public int getAddress1() {
        return this.address1;
    }

    public void setAddress1(final int address1) {
        final int old = this.address1;
        this.address1 = address1;
        changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS1, old,
                this.address1);
    }

    public int getAddress2() {
        return this.address2;
    }

    public void setAddress2(final int address2) {
        final int old = this.address2;
        this.address2 = address2;
        changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS2, old,
                this.address2);
    }

    public int getBus() {
        return bus;
    }

    public void setBus(final int bus) {
        this.bus = bus;
    }

    public SortedSet<LocomotiveFunction> getFunctions() {
        return new TreeSet<LocomotiveFunction>(functions);
    }

    public void setFunctions(final SortedSet<LocomotiveFunction> functions) {
        final Set<LocomotiveFunction> old = this.functions;
        this.functions = new TreeSet<LocomotiveFunction>(functions);
        changeSupport
                .firePropertyChange(PROPERTYNAME_FUNCTIONS, old, this.functions);
    }

    public void addLocomotiveFunction(final LocomotiveFunction function) {
        this.functions.add(function);
    }

    public int getEmergencyStopFunctionNumber() {
        for (final LocomotiveFunction function : functions) {
            if (function.isEmergencyBrakeFunction()) {
                return function.getNumber();
            }
        }
        return -1;
    }

    public LocomotiveFunction getFunction(final int functionNumber) {
        for (final LocomotiveFunction function : functions) {
            if (function.getNumber() == functionNumber) {
                return function;
            }
        }
        return null;
    }

    public LocomotiveGroup getGroup() {
        return this.group;
    }

    public void setGroup(final LocomotiveGroup locomotiveGroup) {
        final LocomotiveGroup old = this.group;
        this.group = locomotiveGroup;
        setGroupId(locomotiveGroup.getId());
        changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVE_GROUP, old,
                this.group);
    }

    public void addPropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.addPropertyChangeListener(x);
    }

    public void removePropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.removePropertyChangeListener(x);
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(final int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getTargetSpeed() {
        return targetSpeed;
    }

    public void setTargetSpeed(int targetSpeed) {
        this.targetSpeed = targetSpeed;
    }

    public int getCurrentOrTargetSpeed() {
        if (targetSpeed == -1) {
            return currentSpeed;
        }
        return targetSpeed;
    }

    public LocomotiveDirection getCurrentDirection() {

        if (currentDirection == null) {
            currentDirection = LocomotiveDirection.FORWARD;
        }
        return currentDirection;
    }

    public void setCurrentDirection(final LocomotiveDirection currentDirection) {
        this.currentDirection = currentDirection;

    }

    public boolean[] getCurrentFunctions() {
        if (currentFunctions == null) {
            currentFunctions = new boolean[functions.size()];
        }
        return currentFunctions;
    }

    public void setCurrentFunctions(final boolean[] currentFunctions) {
        this.currentFunctions = currentFunctions;
    }

    public LocomotiveDirection getToggledDirection() {
        if (FORWARD.equals(currentDirection)) {
            return REVERSE;
        } else {
            return FORWARD;
        }
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int compareTo(final Locomotive o) {
        if (o == null) {
            return 1;
        }
        return name.compareTo(o.getName());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Locomotive)) {
            return false;
        }
        return id.equals(((Locomotive) obj).getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(
                "id" , id)
                .append("name",name)
                .append("groupId",groupId)
                .append("desc",desc)
                .append("type",type)
                .append("bus",bus)
                .append("address1",address1)
                .append("address2",address2)
                .append("functions",functions)
                .append("group",group)
                .append("currentSpeed",currentSpeed)
                .append("targetSpeed",targetSpeed)
                .append("currentDirection",currentDirection)
                .append("currentFunctions",Arrays.toString(currentFunctions)).build();
    }

    public int getMfxUUID() {
        return mfxUUID;
    }

    public void setMfxUUID(int mfxUUID) {
        this.mfxUUID = mfxUUID;
    }
}
