/*------------------------------------------------------------------------
 * 
 * <Switch.java>  -  <Represents a switch>
 * 
 * begin     : Tue Jan  3 21:24:40 CET 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : bm@fork.ch
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

package ch.fork.RailControl.domain;

public abstract class Switch {

    private String name;
    private String description;
    private int address;

    protected abstract void toggle();

}
