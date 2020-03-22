package ch.fork.adhocrailway.model;

import java.beans.PropertyChangeSupport;

public abstract class AbstractItem {

    protected transient PropertyChangeSupport changeSupport;

    public AbstractItem() {
        changeSupport = new PropertyChangeSupport(this);
    }

    public void init() {
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }

    }

    private Object readResolve() {

        changeSupport = new PropertyChangeSupport(this);
        return this;
    }
}
