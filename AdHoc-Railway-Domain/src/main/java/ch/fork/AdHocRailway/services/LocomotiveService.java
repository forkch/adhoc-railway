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

package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;

import java.util.SortedSet;

public interface LocomotiveService {
    public abstract void addLocomotive(Locomotive locomotive)
            ;

    public abstract void removeLocomotive(Locomotive locomotive)
            ;

    public abstract void updateLocomotive(Locomotive locomotive)
            ;

    public abstract SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
            ;

    public abstract void addLocomotiveGroup(LocomotiveGroup group)
            ;

    public abstract void removeLocomotiveGroup(LocomotiveGroup group)
            ;

    public abstract void updateLocomotiveGroup(LocomotiveGroup group)
            ;

    public abstract void clear();

    void init(LocomotiveServiceListener listener);

    void disconnect();

}