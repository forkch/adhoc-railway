/*------------------------------------------------------------------------
 * 
 * <./ui/ListListModel.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 17:00:18 BST 2006
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


package ch.fork.AdHocRailway.ui;

import java.util.List;

import javax.swing.AbstractListModel;

public class ListListModel<E> extends AbstractListModel {
    public List<E> list;

    public ListListModel(List<E> list) {
        this.list = list;
    }

    public int getSize() {
        return list.size();
    }

    public E getElementAt(int arg0) {
        return list.get(arg0);
    }

    public void updated() {
        fireContentsChanged(this, 0, list.size() - 1);
    }
}
