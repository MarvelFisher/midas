package com.cyanspring.common.cstw.auth;

import com.cyanspring.common.account.UserRole;

public interface IAuthChecker {
	public boolean hasAuth(UserRole role, String view,String action);
	public boolean hasViewAuth(UserRole role, String view);

}
