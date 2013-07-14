package ch.fork.AdHocRailway.domain;

import java.beans.PropertyChangeSupport;

public abstract class AbstractItem {

	protected PropertyChangeSupport changeSupport;

	public AbstractItem() {
		changeSupport = new PropertyChangeSupport(this);
	}

	public void init() {
		if (changeSupport == null) {
			changeSupport = new PropertyChangeSupport(this);
		}

	}
}
