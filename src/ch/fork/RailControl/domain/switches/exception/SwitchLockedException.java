/*------------------------------------------------------------------------
 * 
 * <src/domain/switches/SwitchLockedException.java>  -  <Exception when
 * a switch is locked>
 * 
 * begin     : Sat Jan 28 16:56:22 CET 2006
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

package ch.fork.RailControl.domain.switches.exception;


public class SwitchLockedException extends SwitchException {
    public SwitchLockedException(String msg) {
        super(msg);
    }
}
