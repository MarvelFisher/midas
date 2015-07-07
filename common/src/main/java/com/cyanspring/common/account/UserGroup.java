package com.cyanspring.common.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserType;


public class UserGroup implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(UserGroup.class);
	private String user;
	private UserRole role;
	private List <UserGroup>manageeList = new ArrayList<UserGroup>();
		
	public UserGroup(String user,UserRole role) {
		this.user = user;
		this.role = role;
	}
		
	public boolean isManageeExist(String managee){
		for(UserGroup group : manageeList){
			if(group.getUser().equals(managee))
				return true;
		}
		return false;
	}
		
	public boolean isManageRecursive(String manager){
		if(manageeList.isEmpty()){
			return false;
		}
		for(UserGroup managee : manageeList){
			if( null == managee.getUser()){
				continue;
			}
				
			log.debug("check manager :"+manager+", managee.getUser():"+managee.getUser());
			
			if(managee.isManageRecursive(manager)){
					return true;
			}
				
			if(managee.getUser().equals(manager)){
					return true;
			}
		}
		return false;
	}
			
	public String getUser() {
			return user;
	}
		
	public void setUser(String user) {
			this.user = user;
	}
		
	public Set<UserGroup> getManageeSet() {

		Set <UserGroup>userSet = new HashSet<UserGroup>();
		if(manageeList.isEmpty()){
				return userSet;
		}
		for(UserGroup managee:manageeList){
			userSet.add(managee);
			Set <UserGroup>tempSet = managee.getManageeSet();
			if(null != tempSet){
				userSet.addAll(tempSet);
			}
		}
				
		return userSet;
	}
		
	public UserRole getRole() {
			return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public void putManagee(UserGroup userGroup){
			
		if(manageeList.contains(userGroup)){
			log.info("user group already exist:"+userGroup.getUser());
			return;
		}
			
		manageeList.add(userGroup);	
	}
	
	public boolean isAdmin(){
		return UserRole.Admin.equals(role)?true:false;
	}
}
