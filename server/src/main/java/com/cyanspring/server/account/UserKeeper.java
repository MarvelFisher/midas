package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.business.GroupManagement;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.server.persistence.PersistenceManager;

public class UserKeeper {
	private static final Logger log = LoggerFactory
			.getLogger(UserKeeper.class);
	
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	private Map<String, String> pairs = new ConcurrentHashMap<String, String>();
	private Map<String, Set<String>> groups = new ConcurrentHashMap<String, Set<String>>();
	private static final int maxGroupLevel = 20;

	public void createUser(User user) throws UserException {
		String lowCases = user.getId().toLowerCase();
		if(users.containsKey(lowCases))
			throw new UserException("User already exists: " + lowCases,ErrorMessage.USER_ALREADY_EXIST);

		user.setId(lowCases);
		users.put(user.getId(), user);
	}
	
	public User getUser(String id) {
		return users.get(id);
	}
	
	public boolean userExists(String userId) {
		return users.containsKey(userId);
	}
	
	public boolean login(String userId, String password) throws UserException {
		String lowCases = userId.toLowerCase();
		User user = getUser(lowCases);
		if(null == user)
			throw new UserException("Invalid user id or password",ErrorMessage.INVALID_USER_ACCOUNT_PWD);
		/*
		synchronized(user) {
			if(!user.getId().equals(lowCases) || !user.getPassword().equals(password))
				throw new UserException("Invalid user or password");
		}
		*/
		return true;
	}
	
	public User tryCreateDefaultUser() {
		if(!userExists(Default.getUser())) {
			User user = new User(Default.getUser(), "guess?");
			user.setDefaultAccount(Default.getAccount());
			user.setUserType(UserType.SUPPORT);
			this.users.putIfAbsent(user.getId(), user);
			return user;
		}
		return null;
	}
	
	public void injectUsers(List<User> users) {
		for(User user: users) {
			this.users.put(user.getId(), user);
		}
	}

	private void buildNextLevel(Map<String, GroupManagement> current, int level){
		if(current.size() <= 0)
			return;
		
		if(level > maxGroupLevel) {
			log.error("User group exceeding max level of " + maxGroupLevel);
			return;
		}
		
		Map<String, GroupManagement> remaining = new HashMap<String, GroupManagement>();
		for(GroupManagement gm: current.values()) {
			Set<String> list = groups.get(gm.getManaged());
			if(null != list) {
				Set<String> existing = groups.get(gm.getManager());
				if(null == existing) {
					existing = new HashSet<String>(list);
					groups.put(gm.getManager(), existing);
				} else {
					existing.addAll(list);
				}
				
			} else {
				GroupManagement next = current.get(gm.getManaged());
				if(null != next)
					remaining.put(gm.getManager(), gm);
			}
		}
		
		buildNextLevel(remaining, level++);
	}
	
	public void injectGroup(List<GroupManagement> list) {
		Map<String, GroupManagement> remaining = new HashMap<String, GroupManagement>();
		for(GroupManagement gm: list) {
			pairs.put(gm.getManager(), gm.getManaged());
			User user = users.get(gm.getManaged());
			if(user.getRole().equals(UserType.TRADER)) {
				Set<String> subList = groups.get(gm.getManager());
				if(null == subList) {
					subList = new HashSet<String>();
					groups.put(gm.getManager(), subList);
				}
				subList.add(gm.getManaged());
			} else {
				remaining.put(gm.getManager(), gm);
			}
		}
		
		buildNextLevel(remaining, 0);
	}

}
