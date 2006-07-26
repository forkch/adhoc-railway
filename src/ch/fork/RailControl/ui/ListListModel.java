package ch.fork.RailControl.ui;

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
        fireContentsChanged(this, 0, list.size()-1);
    }
}
