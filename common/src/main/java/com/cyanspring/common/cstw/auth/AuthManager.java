package com.cyanspring.common.cstw.auth;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.cyanspring.common.account.UserRole;

public class AuthManager implements IAuthChecker{
	
	private static final Logger log = LoggerFactory
			.getLogger(AuthManager.class);
	
	@Autowired
	List<ViewActionBean>viewActionList;
	
	public boolean hasAuth(UserRole role,String view,String action){
		
		boolean findAuth = false;
		
		if( !StringUtils.hasText(view) || !StringUtils.hasText(action) ){
			log.info("Can't match auth :{},{}",new Object[]{view,action});
			return false;
		}
		
		if(UserRole.Admin == role)
			return true ; 
		
		if(null == viewActionList || viewActionList.isEmpty())
			return false;
		
		for(ViewActionBean bean : viewActionList){
			String tempAction = bean.getAction().toUpperCase();
			String tempView = bean.getView().toUpperCase();
			if(tempAction.equals(action.toUpperCase()) 
					&& tempView.equals(view.toUpperCase()) ){
				findAuth = true;
				UserRole roles[] = bean.getOwner();
				if(null == roles){
					return false;
				}else{
					for(UserRole authRole : roles){
						if(authRole.equals(role)){
							return true;
						}
					}
				}	
			}
		}
		
		if(!findAuth){
			log.info("Can't match auth :{},{}",new Object[]{view,action});
		}
		
		
		return false;
	}

	
	
}
