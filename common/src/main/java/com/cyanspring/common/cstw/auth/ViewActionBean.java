package com.cyanspring.common.cstw.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;

import com.cyanspring.common.account.UserRole;

public class ViewActionBean {
	private String id;
	private String view;
	private String action;
	private UserRole owner[];
	public ViewActionBean(String view,String action,UserRole owner[]) {
		this.view = view;
		this.action = action;
		this.owner = owner;
		this.id = view+"-"+action;
	}
	public String getView() {
		return view;
	}
	public String getAction() {
		return action;
	}
	public String getId() {
		return id;
	}
	public UserRole[] getOwner() {
		return owner;
	}
}
