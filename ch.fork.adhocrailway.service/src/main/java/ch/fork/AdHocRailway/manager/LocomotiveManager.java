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

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.LocomotiveService;

import java.util.SortedSet;

public interface LocomotiveManager {

    void initialize();

    void addLocomotiveManagerListener(final LocomotiveManagerListener listener);

    void removeLocomotiveManagerListenerInNextEvent(
            final LocomotiveManagerListener listener);

    SortedSet<Locomotive> getAllLocomotives();

    SortedSet<LocomotiveGroup> getAllLocomotiveGroups();

    void addLocomotiveToGroup(final Locomotive locomotive,
                              final LocomotiveGroup group);

    void removeLocomotiveFromGroup(final Locomotive locomotive,
                                   final LocomotiveGroup group);

    void updateLocomotive(final Locomotive locomotive);

    void addLocomotiveGroup(final LocomotiveGroup group);

    void removeLocomotiveGroup(final LocomotiveGroup group);

    void updateLocomotiveGroup(final LocomotiveGroup group);

    void setLocomotiveService(final LocomotiveService instance);

    void clear();

    void clearToService();

    void disconnect();

    void setActiveLocomotive(final int locomotiveNumber,
                             final Locomotive locomotive);

    Locomotive getActiveLocomotive(final int locomotiveNumber);

    LocomotiveService getService();

}