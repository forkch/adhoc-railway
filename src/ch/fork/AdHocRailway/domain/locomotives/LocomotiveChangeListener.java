/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/LocomotiveChangeListener.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:55:11 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id$
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


package ch.fork.AdHocRailway.domain.locomotives;

public interface LocomotiveChangeListener {
    public void locomotiveChanged(Locomotive changedSwitch);
}
