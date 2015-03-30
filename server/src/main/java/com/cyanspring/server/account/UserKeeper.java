package com.cyanspring.server.account;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.User;
import com.cyanspring.common.account.UserException;
import com.cyanspring.common.account.UserType;
import com.cyanspring.common.message.ErrorMessage;

public class UserKeeper {
	private Map<String, User> users = new ConcurrentHashMap<String, User>();

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
	
	public synchronized User tryCreateDefaultUser() {
		if(!userExists(Default.getUser())) {
			User user = new User(Default.getUser(), "guess?");
			user.setDefaultAccount(Default.getAccount());
			user.setUserType(UserType.SUPPORT);
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
