package com.cyanspring.common.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.GroupManagement;

public class UserGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(UserGroup.class);
	private String user;
	private UserRole role;
	private Set <UserGroup>managerSet = new HashSet<UserGroup>();
	private List <UserGroup>manageeList = Collections.synchronizedList(new ArrayList<UserGroup>());
	public  static final int MAX_LEVEL = 10;
	private int currentLevel = 1;

	public UserGroup(String user,UserRole role) {
		this.user = user;
		this.role = role;
	}
		
	public boolean isManageeExist(String managee){
		if(manageeList.isEmpty()){
			return false;
		}
		for(UserGroup group : manageeList){			
			if(group.isManageeExist(managee)){
					return true;
			}			
			if(group.getUser().equals(managee)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isGroupPairExist(String managee){
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
	
	/**
	 * recursive every level managee, get all managee.
	 * @return
	 */
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
	
	public int countLevel(UserGroup userGroup){
		int level = 2;
		if(manageeList.isEmpty()){
			return 1;
		}
		for(UserGroup managee:manageeList){
			if(!managee.manageeList.isEmpty()){
			   int temp = managee.countLevel(managee)+1;
			   if(temp > level)
				   level = temp;
			}
		}
		return level;
	}

	public boolean hasManager(){
		if(managerSet.size()>0){
			return true;
		}
		return false;
	}
	
	public List<UserGroup> getManagerList(){
		return new ArrayList<UserGroup>(managerSet);
	}
	
	public void putManagee(UserGroup userGroup){
			
		if(manageeList.contains(userGroup)){
			log.info("user group already exist:"+userGroup.getUser());
			return;
		}
			
		manageeList.add(userGroup);	
	}
	
	public void deleteManagee(String user) throws Exception{
		UserGroup managee = null;
		for(UserGroup group : manageeList){
			if(group.getUser().equals(user)){
				managee = group;
				break;
			}
		}
		
		int manageeLevel = managee.currentLevel;
		if(null != managee)
			manageeList.remove(managee);	
		
		if((manageeLevel+1) == currentLevel){
			int tempLevel = countLevel(this);
			if(tempLevel == currentLevel)
				return;
			
			currentLevel = tempLevel;
			assignManagerLevel(currentLevel,false);
		}
	}
	
	public void deleteManager(String user) throws Exception{
		UserGroup manager = null;
		for(UserGroup group : managerSet){
			if(group.getUser().equals(user)){
				manager = group;
				break;
			}
		}
		if( null != manager)
			managerSet.remove(manager);
	}
	
	public boolean isAdmin(){
		return UserRole.Admin.equals(role)?true:false;
	}
	
	public int findManagerMaxLevel(UserGroup userGroup){
		return  findManagerMaxLevel(userGroup,0);
	}
	
	public int findManagerMaxLevel(UserGroup userGroup,int count){

		for(UserGroup user:userGroup.managerSet){
			if(count < user.currentLevel){
				count = user.currentLevel;
				return user.findManagerMaxLevel(user,count);
			}
		}	
		return count;
	}

	public boolean isOverMaxLevel(UserGroup userGroup){
		
		int maxLevel = findManagerMaxLevel(userGroup);
//		log.info("isOverMaxLevel manangerLevel:{} maxLevel:{}",maxLevel,this.MAX_LEVEL);
		if((maxLevel+currentLevel) > MAX_LEVEL){
			return true;
		}
		return false;
	}
	
	public void putManager(UserGroup userGroup){
		
		managerSet.add(userGroup);	
		assignManagerLevel(currentLevel,true);
	}
	
	private void assignManagerLevel(int currentLevel,boolean isAdd) {
		
		if(managerSet.isEmpty())
			return;
		
		for(UserGroup user: managerSet){	
			if(isAdd){
				if(currentLevel>= user.currentLevel){
					user.currentLevel = currentLevel+1;
//					log.info("user:{},level:{}",user.getUser(),user.currentLevel);
					user.assignManagerLevel(user.currentLevel,isAdd);
					
				}
			}else{
				user.currentLevel = currentLevel+1;
//				log.info("de user:{},level:{}",user.getUser(),user.currentLevel);
				user.assignManagerLevel(user.currentLevel,isAdd);
			}	
		}
	}
	
	/**
	 * only first level managee , like group management pair
	 * @return
	 */
	public List<UserGroup> getNoneRecursiveManageeList() {
		return manageeList;
	}
	
	public List<GroupManagement> exportGroupManagementList(){
		List<GroupManagement> gmList = new ArrayList<GroupManagement>();
		for(UserGroup userGroup : manageeList){
			GroupManagement gm = new GroupManagement(user,userGroup.getUser());
			gmList.add(gm);
		}
		return gmList;
	}
	
	public void reset(){
		manageeList.clear();
		managerSet.clear();
		currentLevel=1;
	}
	
	public void clearManageeList(){
		manageeList.clear();
	}
}
