package com.cyanspring.server.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventBridge;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.order.StrategySnapshotEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.event.system.SystemErrorEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.transport.IServerUserSocketService;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncPriorityEventThread;
import com.cyanspring.common.error.ErrorLookup;

public class ApiBridgeManager implements IPlugin, IAsyncEventBridge, IAsyncEventListener {
	private static Logger log = LoggerFactory.getLogger(ApiBridgeManager.class);
	
	@Autowired
	private IAsyncEventManager eventManager;
	
	private IServerUserSocketService socketService;
	
	private String bridgeId = "ApiBridge-163168";
	private Map<String, PendingRecord> pendingRecords = new ConcurrentHashMap<String, PendingRecord>();
	private Map<String, Map<String, String>> quoteSubscription = new ConcurrentHashMap<String,  Map<String, String>>();
	private Map<String, String> accountUserMap = new ConcurrentHashMap<String, String>();
	
	private IServerSocketListener listener = new IServerSocketListener() {
		@Override
		public void onConnected(boolean connected, IUserSocketContext ctx) {			
			ctx.send(new ServerReadyEvent(connected));
			
			if(!connected) {
				for(Map<String, String> map: quoteSubscription.values()) {
					String symbol = map.remove(ctx.getUser());
					log.info("Remove symbol subscription: " + ctx.getUser() + ", " + symbol);
				}
			}
		}

		@Override
		public void onMessage(Object obj, IUserSocketContext ctx) {
			if(obj instanceof UserLoginEvent) {
				processUserLoginEvent((UserLoginEvent)obj, ctx);
			} else if (ctx.getUser() == null) {
				ctx.send(new SystemErrorEvent(null, null, 301, ErrorLookup.lookup(301)));
			} else if(obj instanceof QuoteSubEvent) {
				processQuoteSubEvent((QuoteSubEvent)obj, ctx);
			} else if(obj instanceof StrategySnapshotRequestEvent) {
				processStrategySnapshotRequestEvent((StrategySnapshotRequestEvent)obj, ctx);
			} else {
				ctx.send(new SystemErrorEvent(null, null, 302, ErrorLookup.lookup(302) + " : " + obj.getClass()));
			}
			
		}

	};
	
	private AsyncPriorityEventThread thread = new AsyncPriorityEventThread() {

		@Override
		public void onEvent(AsyncEvent event) {
			if(event instanceof UserLoginReplyEvent) {
				processUserLoginReplyEvent((UserLoginReplyEvent)event);
			} else if (event instanceof QuoteEvent) {
				processQuoteEvent((QuoteEvent)event);
			} else if (event instanceof EnterParentOrderReplyEvent) {
				processEnterParentOrderReplyEvent((EnterParentOrderReplyEvent)event);
			} else if (event instanceof AmendParentOrderReplyEvent) {
				processAmendParentOrderReplyEvent((AmendParentOrderReplyEvent)event);
			} else if (event instanceof CancelParentOrderReplyEvent) {
				processCancelParentOrderReplyEvent((CancelParentOrderReplyEvent)event);
			} else if (event instanceof ParentOrderUpdateEvent) {
				processParentOrderUpdateEvent((ParentOrderUpdateEvent)event);
			} else if (event instanceof ChildOrderUpdateEvent) {
				processChildOrderUpdateEvent((ChildOrderUpdateEvent)event);
			} else if (event instanceof StrategySnapshotEvent) {
				processStrategySnapshotEvent((StrategySnapshotEvent)event);
			} else if (event instanceof AccountSnapshotReplyEvent) {
				processAccountSnapshotReplyEvent((AccountSnapshotReplyEvent)event);
			} else if (event instanceof AccountUpdateEvent) {
				processAccountUpdateEvent((AccountUpdateEvent)event);
			} else if (event instanceof AccountDynamicUpdateEvent) {
				processAccountDynamicUpdateEvent((AccountDynamicUpdateEvent)event);
			} else if (event instanceof OpenPositionUpdateEvent) {
				processOpenPositionUpdateEvent((OpenPositionUpdateEvent)event);
			} else if (event instanceof OpenPositionDynamicUpdateEvent) {
				processOpenPositionDynamicUpdateEvent((OpenPositionDynamicUpdateEvent)event);
			} else if (event instanceof ClosedPositionUpdateEvent) {
				processClosedPositionUpdateEvent((ClosedPositionUpdateEvent)event);
			} else if (event instanceof QuoteEvent) {
				processQuoteEvent((QuoteEvent)event);
			} else if (event instanceof QuoteEvent) {
				processQuoteEvent((QuoteEvent)event);
			}
			
		}
		
	};
	
	@Override
	public void onEvent(AsyncEvent event) {
		thread.addEvent(event);
	}

	private void subscribeToEvents() {
//		eventManager.subscribe(UserLoginReplyEvent.class, null, this);
//		eventManager.subscribe(QuoteEvent.class, null, this);
//		eventManager.subscribe(EnterParentOrderReplyEvent.class, getBridgeId(), this);
//		eventManager.subscribe(AmendParentOrderReplyEvent.class, getBridgeId(), this);
//		eventManager.subscribe(CancelParentOrderReplyEvent.class, getBridgeId(), this);
//		eventManager.subscribe(ParentOrderUpdateEvent.class, null, this);
//		eventManager.subscribe(ChildOrderUpdateEvent.class, null, this);
//		eventManager.subscribe(StrategySnapshotEvent.class, null, this);
//		eventManager.subscribe(AccountSnapshotReplyEvent.class, null, this);
//		eventManager.subscribe(AccountUpdateEvent.class, null, this);
//		eventManager.subscribe(AccountDynamicUpdateEvent.class, null, this);
//		eventManager.subscribe(OpenPositionUpdateEvent.class, null, this);
//		eventManager.subscribe(OpenPositionDynamicUpdateEvent.class, null, this);
//		eventManager.subscribe(ClosedPositionUpdateEvent.class, null, this);

	}
	
	private class PendingRecord {
		String txId;
		String origTxId;
		String origSender;
		IUserSocketContext ctx;
		
		public PendingRecord(String txId, String origTxId, String origSender, IUserSocketContext ctx) {
			this.txId = txId;
			this.origTxId = origTxId;
			this.origSender = origSender;
			this.ctx = ctx;
		}
	}

	public void processUserLoginEvent(UserLoginEvent event, IUserSocketContext ctx) {
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), event.getSender(), ctx);
		pendingRecords.put(record.txId, record);
		UserLoginEvent request = new UserLoginEvent(event.getKey(), null, event.getUserId(), event.getPassword(), txId);
		request.setSender(getBridgeId());
		sendEventToManager(request);
	}

	public void processUserLoginReplyEvent(UserLoginReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(event.isOk()) {
			socketService.setUserContext(event.getUser().getId(), record.ctx);
		}
		
		accountUserMap.put(event.getDefaultAccount().getId(), event.getUser().getId());
		for(Account account: event.getAccounts()) {
			accountUserMap.put(account.getId(), event.getUser().getId());
		}
		
		UserLoginReplyEvent reply = new UserLoginReplyEvent(
				event.getKey(), 
				record.origSender, 
				event.getUser(),
				event.getDefaultAccount(),
				event.getAccounts(),
				event.isOk(), event.getMessage(), record.origTxId);
		
		if(record.ctx.isOpen())
			record.ctx.send(reply);
	}
	
	protected void processQuoteSubEvent(QuoteSubEvent event, IUserSocketContext ctx) {
		log.info("User: " + ctx.getUser() + ", Symbol: " + event.getSymbol());
		if(null == ctx.getUser())
			return;
		
		Map<String, String> userSymbol = quoteSubscription.get(event.getSymbol());
		if(null == userSymbol) {
			userSymbol = new ConcurrentHashMap<String, String>();
			quoteSubscription.put(event.getSymbol(), userSymbol);
		}
		
		userSymbol.put(ctx.getUser(), event.getSymbol());
		
		sendEventToManager(new QuoteSubEvent(ctx.getUser(), getBridgeId(), event.getSymbol()));
	}

	public void processQuoteEvent(QuoteEvent event) {
		if(event.getReceiver() != null) {
			sendEventToUser(event.getKey(), event); //in this case key is user
		} else {
			Map<String, String> userSymbol = quoteSubscription.get(event.getQuote().getSymbol());
			if(null != userSymbol) {
				for(String user: userSymbol.keySet())
					sendEventToUser(user, event);
			}
		}
	}

	protected void processStrategySnapshotRequestEvent(
			StrategySnapshotRequestEvent event, IUserSocketContext ctx) {
		if(!checkAccount(event.getKey(), ctx.getUser()))
			ctx.send(new SystemErrorEvent(null, null, 303, 
					ErrorLookup.lookup(303) + ": " + event.getKey() + ", " + ctx.getUser()));
			
		sendEventToManager(event);
	}

	protected void processStrategySnapshotEvent(StrategySnapshotEvent event) {
		String user = accountUserMap.get(event.getKey());
		if(null == user) {
			log.error("StrategySnapshotEvent can't find user with this account: " + event.getKey());
			return;
		}
			
		sendEventToUser(user, event);
	}

	protected void processAccountSnapshotReplyEvent(
			AccountSnapshotReplyEvent event) {
		// TODO Auto-generated method stub
		
	}

	protected void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		// TODO Auto-generated method stub
		
	}

	protected void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		// TODO Auto-generated method stub
		
	}

	protected void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void processAccountUpdateEvent(AccountUpdateEvent event) {
		
	}
	
	public void processAccountDynamicUpdateEvent(AccountDynamicUpdateEvent event) {
		
	}
	
	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		
	}
	
	public void processOpenPositionDynamicUpdateEvent(OpenPositionDynamicUpdateEvent event) {
		
	}
	
	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		
	}

	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		
	}

	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
		
	}
	
	private boolean checkAccount(String account, String user) {
		return null != user && user.equals(accountUserMap.get(account));
	}

	private void sendEventToManager(RemoteAsyncEvent event) {
		event.setSender(this.getBridgeId());
		eventManager.sendEvent(event);
	}
	
	private void sendEventToUser(String user, RemoteAsyncEvent event) {
		List<IUserSocketContext> list = socketService.getContextByUser(user);
		for(IUserSocketContext ctx: list) {
			if(ctx.isOpen())
				ctx.send(event);
		}
	}
	
	@Override
	public String getBridgeId() {
		return bridgeId;
	}

	public void setBridgeId(String bridgeId) {
		this.bridgeId = bridgeId;
	}

	@Override
	public void onBridgeEvent(RemoteAsyncEvent event) {
		thread.addEvent(event);
	}

	@Override
	public void init() throws Exception {
		thread.setName("ApiBridgeManager");
		thread.start();
		
		socketService.addListener(this.listener);
		socketService.init();
		
		subscribeToEvents();
	}

	@Override
	public void uninit() {
		thread.exit();
	}

	public IServerUserSocketService getSocketService() {
		return socketService;
	}

	public void setSocketService(IServerUserSocketService socketService) {
		this.socketService = socketService;
	}

	

}
