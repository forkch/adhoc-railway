/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotivePersistenceIface.java 199 2012-01-14 23:46:24Z fork_ch $
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

package ch.fork.AdHocRailway.manager;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.LocomotiveService;
import com.google.common.eventbus.EventBus;

import java.util.SortedSet;

public interface LocomotiveManager {

    public abstract void initialize(final EventBus bus);

    void addLocomotiveManagerListener(final LocomotiveManagerListener listener);

    void removeLocomotiveManagerListenerInNextEvent(
            final LocomotiveManagerListener listener);

    public abstract SortedSet<Locomotive> getAllLocomotives()
           ;

    public abstract SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
           ;

    public abstract void addLocomotiveToGroup(final Locomotive locomotive,
                                              final LocomotiveGroup group);

    public abstract void removeLocomotiveFromGroup(final Locomotive locomotive,
                                                   final LocomotiveGroup group);

    public abstract void updateLocomotive(final Locomotive locomotive)
           ;

    public abstract void addLocomotiveGroup(final LocomotiveGroup group)
           ;

    public abstract void removeLocomotiveGroup(final LocomotiveGroup group)
           ;

    public abstract void updateLocomotiveGroup(final LocomotiveGroup group)
           ;

    public abstract void setLocomotiveService(final LocomotiveService instance);

    public abstract void clear();

    public abstract void clearToService();

    public abstract void disconnect();

    public abstract void setActiveLocomotive(final int locomotiveNumber,
                                             final Locomotive locomotive);

    public abstract Locomotive getActiveLocomotive(final int locomotiveNumber);

    public abstract LocomotiveService getService();

}