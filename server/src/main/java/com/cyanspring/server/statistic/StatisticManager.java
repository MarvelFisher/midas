package com.cyanspring.server.statistic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.AsyncEventProcessor;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.statistic.AccountStatistic;
import com.cyanspring.common.event.statistic.AccountStatisticReplyEvent;
import com.cyanspring.common.event.statistic.AccountStatisticRequestEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.account.AccountKeeper;

public class StatisticManager implements IPlugin{
	
	private static final Logger log = LoggerFactory
			.getLogger(StatisticManager.class);

	private static final String ID = "StatisticMgr-"+IdGenerator.getInstance().getNextID();
	private static final String SENDER = StatisticManager.class.getSimpleName();
	
	@Autowired
	protected IRemoteEventManager eventManager;
	
	@Autowired
    AccountKeeper accountKeeper;

	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {		
			subscribeToEvent(AccountStatisticRequestEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	

	
	@Override
	public void init() throws Exception {
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if (eventProcessor.getThread() != null){
			eventProcessor.getThread().setName("StatisticManager");
		}	
	}

	@Override
	public void uninit() {
		eventProcessor.uninit();	
	}
	
	
	public void processAccountStatisticRequestEvent(AccountStatisticRequestEvent event){		
		
		AccountStatisticCollector cal = new AccountStatisticCollector();
		List <Account>accounts = accountKeeper.getAllAccounts();
		try {
			
			for(Account account:accounts){		
				cal.calculate(account);
			}
			Map <String,Object>map = new LinkedHashMap<>();
			map.put(AccountStatistic.CASH_DEPOSITED.value(), cal.getTotalCashDeposited());
			map.put(AccountStatistic.ACCOUNT_VALUE.value(), cal.getTotalAccountValue());
			map.put(AccountStatistic.VALUE.value(), cal.getTotalValue());		
			map.put(AccountStatistic.DAILY_PNL.value(), cal.getTotalDailyPnL());
			map.put(AccountStatistic.ALL_TIME_PNL.value(), cal.getTotalAllTimePnl());	
			map.put(AccountStatistic.UR_PNL.value(), cal.getTotalUrPnL());
			AccountStatisticReplyEvent reply = new AccountStatisticReplyEvent(event.getKey(),event.getSender(),map);
			eventManager.sendRemoteEvent(reply);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
}
