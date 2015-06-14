package com.cyanspring.common.business;

import java.io.Serializable;

public class GroupManagement implements Serializable {
	private static final long serialVersionUID = 1L;
	private String manager;
	private String managed;
	
	
	public GroupManagement(String manager, String managed) {
		super();
		this.manager = manager;
		this.managed = managed;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public String getManaged() {
		return managed;
	}
	public void setManaged(String managed) {
		this.managed = managed;
	}
}
