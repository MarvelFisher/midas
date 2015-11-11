package com.cyanspring.server.downstream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.downstream.DownStreamManager;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.downstream.IOrderRouter;
import com.cyanspring.server.BusinessManager;

public class AccountOrderRouter implements IOrderRouter {
	private static final Logger log = LoggerFactory
			.getLogger(AccountOrderRouter.class);
	
	@Autowired
	private AccountKeeper accountKeeper;
	
	@Override
	public IDownStreamSender setRoute(DownStreamManager downStreamManager,
			DataObject data) throws Exception {
		String account = data.get(String.class, OrderField.ACCOUNT.value());
		AccountSetting settings = accountKeeper.getAccountSetting(account);
		if(null != settings) {
			String route = settings.getRoute();
			if(null != route && !route.isEmpty()) {
				data.put(OrderField.ROUTE.value(), route);
				IDownStreamSender sender = downStreamManager.getSender(route);
				log.info("Route set to: " + route);
				return sender;
			}
		}
		return downStreamManager.getSender();
	}

}
