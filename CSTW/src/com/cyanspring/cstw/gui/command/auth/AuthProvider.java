package com.cyanspring.cstw.gui.command.auth;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.gui.ApplicationWorkbenchWindowAdvisor;

public class AuthProvider extends AbstractSourceProvider{
	
	private static final Logger log = LoggerFactory
			.getLogger(AuthProvider.class);
	
	  public final static String ADMIN = UserRole.Admin.name();
	  public final static String RISK_MANAGER = UserRole.RiskManager.name();
	  public final static String TRADER = UserRole.Trader.name();
	  public final static String ENABLED = "ENABLED";
	  public final static String DISENABLED = "DISENABLED";
	  private boolean enabled = true;
	  public Map stateMap = new HashMap();
	@Override
	public void dispose() {
		
	}

	@Override
	public Map getCurrentState() {
		UserRole role = Business.getInstance().getUserGroup().getRole();
	    stateMap.put(role.name(), DISENABLED);
	    return stateMap;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { ADMIN,RISK_MANAGER,TRADER };
	}
	
	public void fireAccountChanged(){
		stateMap.clear();
		UserRole role = Business.getInstance().getUserGroup().getRole();
		log.info("get role again:{}",role.name());
	    stateMap.put(role.name(), ENABLED);
		fireSourceChanged(0, stateMap);
	}

}
