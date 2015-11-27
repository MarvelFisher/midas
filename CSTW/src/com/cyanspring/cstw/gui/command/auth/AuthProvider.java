package com.cyanspring.cstw.gui.command.auth;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.account.UserRole;
import com.cyanspring.cstw.session.CSTWSession;

public class AuthProvider extends AbstractSourceProvider {

	private static final Logger log = LoggerFactory
			.getLogger(AuthProvider.class);

	public final static String ADMIN = UserRole.Admin.name();
	public final static String RISK_MANAGER = UserRole.RiskManager.name();
	public final static String TRADER = UserRole.Trader.name();
	public final static String BACKEND_RISK_MANAGER = UserRole.BackEndRiskManager
			.name();
	public final static String ENABLED = "ENABLED";
	public final static String DISABLED = "DISABLED";
	private boolean enabled = true;
	public Map stateMap = new HashMap();

	@Override
	public void dispose() {

	}

	@Override
	public Map getCurrentState() {

		String names[] = getProvidedSourceNames();
		for (String name : names) {
			stateMap.put(name, DISABLED);
		}

		return stateMap;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { ADMIN, RISK_MANAGER, BACKEND_RISK_MANAGER, TRADER };
	}

	public void fireAccountChanged() {
		stateMap.clear();
		UserRole role = CSTWSession.getInstance().getUserGroup().getRole();
		log.info("get role again:{}", role.name());

		String names[] = getProvidedSourceNames();
		for (String name : names) {
			if (role.name().equals(name)) {
				stateMap.put(role.name(), ENABLED);
			} else {
				stateMap.put(name, DISABLED);
			}
		}

		fireSourceChanged(0, stateMap);
	}

}
