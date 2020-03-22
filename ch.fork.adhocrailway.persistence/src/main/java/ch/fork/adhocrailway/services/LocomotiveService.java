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

package ch.fork.adhocrailway.services;

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveGroup;

import java.util.SortedSet;

public interface LocomotiveService {
    void addLocomotive(Locomotive locomotive);

    void removeLocomotive(Locomotive locomotive);

    void updateLocomotive(Locomotive locomotive);

    void getAllLocomotiveGroups();

    void addLocomotiveGroup(LocomotiveGroup group);

    void removeLocomotiveGroup(LocomotiveGroup group);

    void updateLocomotiveGroup(LocomotiveGroup group);

    void clear();

    void init(LocomotiveServiceListener listener);

    void disconnect();

    void addLocomotiveGroups(SortedSet<LocomotiveGroup> groups);

}
