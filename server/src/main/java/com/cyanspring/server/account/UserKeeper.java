package com.cyanspring.server.account;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;

public class UserKeeper {
	private Map<String, User> users = new ConcurrentHashMap<String, User>();

	public void createUser(User user) throws UserException {
		if(users.containsKey(user.getId()))
			throw new UserException("User already exists: " + user.getId());

		users.put(user.getId(), user);
	}
	
	public User getUser(String id) {
		return users.get(id);
	}
	
	public boolean userExists(String userId) {
		return users.containsKey(userId);
	}
	
	public boolean login(String userId, String password) throws UserException {
		User user = getUser(userId);
		if(null == user)
			throw new UserException("Invalid user id or password");
		synchronized(user) {
			if(!user.getId().equals(userId) || !user.getPassword().equals(password))
				throw new UserException("Invalid user id or password");
		}
		return true;
	}
	
	public synchronized User tryCreateDefaultUser() {
		if(!userExists(Default.getUser())) {
			User user = new User(Default.getUser(), "");
			user.setDefaultAccount(Default.getAccount());
			this.users.put(user.getId(), user);
			return user;
		}
		return null;
	}
	
	public void injectUsers(List<User> users) {
		for(User user: users) {
			this.users.put(user.getId(), user);
		}
	}
}
