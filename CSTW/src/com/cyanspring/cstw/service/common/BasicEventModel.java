package com.cyanspring.cstw.service.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/14
 *
 */
public abstract class BasicEventModel {

	protected PropertyChangeSupport changes = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changes.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
		changes.addPropertyChangeListener(prop, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changes.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
		changes.removePropertyChangeListener(prop, listener);
	}
	
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		changes.firePropertyChange(propertyName, oldValue, newValue);
	}

}
