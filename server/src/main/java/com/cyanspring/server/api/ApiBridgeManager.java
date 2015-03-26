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
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventBridge;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.UserConnectionType;
import com.cyanspring.common.event.account.AccountDynamicUpdateEvent;
import com.cyanspring.common.event.account.AccountSnapshotReplyEvent;
import com.cyanspring.common.event.account.AccountSnapshotRequestEvent;
import com.cyanspring.common.event.account.AccountUpdateEvent;
import com.cyanspring.common.event.account.ClosedPositionUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionDynamicUpdateEvent;
import com.cyanspring.common.event.account.OpenPositionUpdateEvent;
import com.cyanspring.common.event.account.UserLoginEvent;
import com.cyanspring.common.event.account.UserLoginReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.event.order.StrategySnapshotEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.AmendSingleInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.AmendSingleInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.CancelSingleInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.CancelSingleInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.system.SystemErrorEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.transport.IServerSocketListener;
import com.cyanspring.common.transport.IServerUserSocketService;
import com.cyanspring.common.transport.IUserSocketContext;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.event.AsyncPriorityEventThread;
import com.cyanspring.common.error.ErrorLookup;

public class ApiBridgeManager implements IPlugin, IAsyncEventBridge, IAsyncEventListener {
	private static Logger log = LoggerFactory.getLogger(ApiBridgeManager.class);
	
	@Autowired
	private IAsyncEventManager eventManager;
	
	private IServerUserSocketService socketService;
	private UserConnectionType userConnectionType = UserConnectionType.PREEMPTIVE;
	
	private String bridgeId = "ApiBridge-163168";
	private Map<String, PendingRecord> pendingRecords = new ConcurrentHashMap<String, PendingRecord>();
	private Map<String, Map<String, String>> quoteSubscription = new ConcurrentHashMap<String,  Map<String, String>>();
	private Map<String, String> accountUserMap = new ConcurrentHashMap<String, String>();
	private Map<String, ParentOrder> orders = new ConcurrentHashMap<String, ParentOrder>();
	
	private IServerSocketListener listener = new IServerSocketListener() {
		@Override
		public void onConnected(boolean connected, IUserSocketContext ctx) {
			if(connected) {
				ctx.send(new ServerReadyEvent(connected));
			} else {
				for(Map<String, String> map: quoteSubscription.values()) {
					String symbol = map.remove(ctx.getId());
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
			} else if(obj instanceof AccountSnapshotRequestEvent) {
				processAccountSnapshotRequestEvent((AccountSnapshotRequestEvent)obj, ctx);
			} else if(obj instanceof EnterParentOrderEvent) {
				processEnterParentOrderEvent((EnterParentOrderEvent)obj, ctx);
			} else if(obj instanceof AmendParentOrderEvent) {
				processAmendParentOrderEvent((AmendParentOrderEvent)obj, ctx);
			} else if(obj instanceof CancelParentOrderEvent) {
				processCancelParentOrderEvent((CancelParentOrderEvent)obj, ctx);
			} else if(obj instanceof NewSingleInstrumentStrategyEvent) {
				processNewSingleInstrumentStrategyEvent((NewSingleInstrumentStrategyEvent)obj, ctx);
			} else if(obj instanceof AmendSingleInstrumentStrategyEvent) {
				processAmendSingleInstrumentStrategyEvent((AmendSingleInstrumentStrategyEvent)obj, ctx);
			} else if(obj instanceof CancelSingleInstrumentStrategyEvent) {
				processCancelSingleInstrumentStrategyEvent((CancelSingleInstrumentStrategyEvent)obj, ctx);
			} else if(obj instanceof NewMultiInstrumentStrategyEvent) {
				processNewMultiInstrumentStrategyEvent((NewMultiInstrumentStrategyEvent)obj, ctx);
			} else if(obj instanceof AmendMultiInstrumentStrategyEvent) {
				processAmendMultiInstrumentStrategyEvent((AmendMultiInstrumentStrategyEvent)obj, ctx);
			} else if(obj instanceof CancelMultiInstrumentStrategyEvent) {
				processCancelMultiInstrumentStrategyEvent((CancelMultiInstrumentStrategyEvent)obj, ctx);
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
			} else if (event instanceof NewSingleInstrumentStrategyReplyEvent) {
				processNewSingleInstrumentStrategyReplyEvent((NewSingleInstrumentStrategyReplyEvent)event);
			} else if (event instanceof AmendSingleInstrumentStrategyReplyEvent) {
				processAmendSingleInstrumentStrategyReplyEvent((AmendSingleInstrumentStrategyReplyEvent)event);
			} else if (event instanceof CancelSingleInstrumentStrategyReplyEvent) {
				processCancelSingleInstrumentStrategyReplyEvent((CancelSingleInstrumentStrategyReplyEvent)event);
			} else if (event instanceof NewMultiInstrumentStrategyReplyEvent) {
				processNewMultiInstrumentStrategyReplyEvent((NewMultiInstrumentStrategyReplyEvent)event);
			} else if (event instanceof AmendMultiInstrumentStrategyReplyEvent) {
				processAmendMultiInstrumentStrategyReplyEvent((AmendMultiInstrumentStrategyReplyEvent)event);
			} else if (event instanceof CancelMultiInstrumentStrategyReplyEvent) {
				processCancelMultiInstrumentStrategyReplyEvent((CancelMultiInstrumentStrategyReplyEvent)event);
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
		IUserSocketContext ctx;
		
		public PendingRecord(String txId, String origTxId, IUserSocketContext ctx) {
			this.txId = txId;
			this.origTxId = origTxId;
			this.ctx = ctx;
		}
	}

	public void processUserLoginEvent(UserLoginEvent event, IUserSocketContext ctx) {
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
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
			String user = event.getUser().getId();
			if(userConnectionType == UserConnectionType.PREEMPTIVE) {
				List<IUserSocketContext> list = socketService.getContextByUser(user);
				for(IUserSocketContext ctx: list) {
					log.info("Terminate existing connection: " + ctx.getUser() + ", " + ctx.getId());
					ctx.close();
				}
			} else if (userConnectionType == UserConnectionType.BLOCKING) {
				List<IUserSocketContext> list = socketService.getContextByUser(user);
				for(IUserSocketContext ctx: list) {
					if(ctx.isOpen()) {
						log.info("Terminate current connection since there is existing connection: " 
								+ ctx.getUser() + ", " + ctx.getId());

						record.ctx.send(new SystemErrorEvent(null, null, 301, ErrorLookup.lookup(301)));
						record.ctx.close();
					}
				}
			}
			
			socketService.setUserContext(event.getUser().getId(), record.ctx);

			if(event.getDefaultAccount() != null)
				accountUserMap.put(event.getDefaultAccount().getId(), event.getUser().getId());
			else
				accountUserMap.put(event.getAccounts().get(0).getId(), event.getUser().getId());
			for(Account account: event.getAccounts()) {
				accountUserMap.put(account.getId(), event.getUser().getId());
			}
			
		}
		
		UserLoginReplyEvent reply = new UserLoginReplyEvent(
				event.getKey(), 
				null, 
				event.getUser(),
				event.getDefaultAccount(),
				event.getAccounts(),
				event.isOk(), event.getMessage(), record.origTxId);
		
		if(record.ctx.isOpen())
			record.ctx.send(reply);
		
		log.info("API user login: " + event.getUser().getId() + ", " + event.isOk());
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
			
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);

		sendEventToManager(new StrategySnapshotRequestEvent(event.getKey(), event.getReceiver(), txId));
	}

	protected void processStrategySnapshotEvent(StrategySnapshotEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new StrategySnapshotEvent(event.getKey(), event.getReceiver(), 
				event.getOrders(), event.getInstruments(), event.getStrategyData(), record.origTxId));
	}

	protected void processAccountSnapshotRequestEvent(
			AccountSnapshotRequestEvent event, IUserSocketContext ctx) {
		if(!checkAccount(event.getAccountId(), ctx.getUser()))
			ctx.send(new SystemErrorEvent(null, null, 303, 
					ErrorLookup.lookup(303) + ": " + event.getKey() + ", " + ctx.getUser()));
			
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);

		sendEventToManager(new AccountSnapshotRequestEvent(event.getKey(), 
				event.getReceiver(), event.getAccountId(), txId));
	}

	protected void processAccountSnapshotReplyEvent(
			AccountSnapshotReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
			
		if(record.ctx.isOpen())
			record.ctx.send(new AccountSnapshotReplyEvent(event.getKey(), 
				event.getReceiver(), event.getAccount(), 
				event.getAccountSetting(), event.getOpenPositions(),
				event.getClosedPositions(),
				event.getExecutions(), record.origTxId 
				));
	}

	protected void processEnterParentOrderEvent(EnterParentOrderEvent event,
			IUserSocketContext ctx) {
		String account = (String) event.getFields().get(OrderField.ACCOUNT.value());
		if(!checkAccount(account, ctx.getUser()))
			ctx.send(new SystemErrorEvent(null, null, 303, 
					ErrorLookup.lookup(303) + ": " + event.getKey() + ", " + ctx.getUser()));
			
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		EnterParentOrderEvent request = new EnterParentOrderEvent(event.getKey(), event.getReceiver(), event.getFields(), 
				txId, false);
		request.getFields().put(OrderField.USER.value(), ctx.getUser());

		sendEventToManager(request);
	}

	protected void processEnterParentOrderReplyEvent(
			EnterParentOrderReplyEvent event) {
		orders.put(event.getOrder().getId(), event.getOrder());
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new EnterParentOrderReplyEvent(event.getKey(), 
				null, event.isOk(), event.getMessage(), record.origTxId, event.getOrder(), 
				event.getUser(), event.getAccount()));
	}

	protected void processAmendParentOrderEvent(AmendParentOrderEvent event,
			IUserSocketContext ctx) {
		
		ParentOrder prev = orders.get(event.getId());
		
		if(null == prev) {
			ctx.send(new AmendParentOrderReplyEvent(event.getKey(), null, false, 
					"Can't find order to amend", event.getTxId(), null));
		}
		
		if(!checkAccount(prev.getAccount(), ctx.getUser()))
			ctx.send(new SystemErrorEvent(null, null, 303, 
					ErrorLookup.lookup(303) + ": " + event.getKey() + ", " + ctx.getUser()));
		
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);

		AmendParentOrderEvent request = new AmendParentOrderEvent(event.getKey(), 
				event.getReceiver(), event.getId(), event.getFields(), txId);
		sendEventToManager(request);
	}

	protected void processAmendParentOrderReplyEvent(
			AmendParentOrderReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;

		if(record.ctx.isOpen())
			record.ctx.send(new AmendParentOrderReplyEvent(
				event.getKey(), null, event.isOk(), event.getMessage(), record.origTxId, event.getOrder()));
	}

	protected void processCancelParentOrderEvent(CancelParentOrderEvent event,
			IUserSocketContext ctx) {
		ParentOrder prev = orders.get(event.getOrderId());
		
		if(null == prev) {
			ctx.send(new CancelParentOrderReplyEvent(event.getKey(), null, false, 
					"Can't find order to cancel", event.getTxId(), null));
		}
		
		if(!checkAccount(prev.getAccount(), ctx.getUser()))
			ctx.send(new SystemErrorEvent(null, null, 303, 
					ErrorLookup.lookup(303) + ": " + event.getKey() + ", " + ctx.getUser()));
		
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);

		CancelParentOrderEvent request = new CancelParentOrderEvent(event.getKey(), 
				event.getReceiver(), event.getOrderId(), false, txId);
		sendEventToManager(request);
	}

	protected void processCancelParentOrderReplyEvent(
			CancelParentOrderReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;

		if(record.ctx.isOpen())
			record.ctx.send(new CancelParentOrderReplyEvent(
				event.getKey(), null, event.isOk(), event.getMessage(), record.origTxId, event.getOrder()));
	}

	public void processAccountUpdateEvent(AccountUpdateEvent event) {
		sendEventToUser(event.getAccount().getUserId(), event);
	}
	
	public void processClosedPositionUpdateEvent(ClosedPositionUpdateEvent event) {
		sendEventToUser(event.getPosition().getUser(), event);
	}
	
	public void processOpenPositionUpdateEvent(OpenPositionUpdateEvent event) {
		sendEventToUser(event.getPosition().getUser(), event);
	}
	
	protected void processNewSingleInstrumentStrategyEvent(
			NewSingleInstrumentStrategyEvent event, IUserSocketContext ctx) {
		
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		NewSingleInstrumentStrategyEvent request = new NewSingleInstrumentStrategyEvent(
				event.getKey(), event.getReceiver(), txId, event.getInstrument());

		sendEventToManager(request);
	}

	protected void processNewSingleInstrumentStrategyReplyEvent(
			NewSingleInstrumentStrategyReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new NewSingleInstrumentStrategyReplyEvent(
					event.getKey(), event.getReceiver(), record.origTxId, 
					event.isSuccess(), event.getMessage()));
	}

	protected void processAmendSingleInstrumentStrategyEvent(
			AmendSingleInstrumentStrategyEvent event, IUserSocketContext ctx) {
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		AmendSingleInstrumentStrategyEvent request = new AmendSingleInstrumentStrategyEvent(
				event.getKey(), event.getReceiver(), event.getFields(), txId);

		sendEventToManager(request);
	}

	protected void processAmendSingleInstrumentStrategyReplyEvent(
			AmendSingleInstrumentStrategyReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new AmendSingleInstrumentStrategyReplyEvent(
					event.getKey(), event.getReceiver(), record.origTxId, 
					event.isSuccess(), event.getMessage()));
	}

	protected void processCancelSingleInstrumentStrategyEvent(
			CancelSingleInstrumentStrategyEvent event, IUserSocketContext ctx) {
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		CancelSingleInstrumentStrategyEvent request = new CancelSingleInstrumentStrategyEvent(
				event.getKey(), event.getReceiver(), txId);

		sendEventToManager(request);
	}
	
	protected void processCancelSingleInstrumentStrategyReplyEvent(
			CancelSingleInstrumentStrategyReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new CancelSingleInstrumentStrategyReplyEvent(
					event.getKey(), event.getReceiver(), record.origTxId, 
					event.isSuccess(), event.getMessage()));
	}

	protected void processNewMultiInstrumentStrategyEvent(
			NewMultiInstrumentStrategyEvent event, IUserSocketContext ctx) {
		
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		NewSingleInstrumentStrategyEvent request = new NewSingleInstrumentStrategyEvent(
				event.getKey(), event.getReceiver(), txId, event.getStrategy());

		sendEventToManager(request);
	}
	
	protected void processNewMultiInstrumentStrategyReplyEvent(
			NewMultiInstrumentStrategyReplyEvent event) {
		
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new NewMultiInstrumentStrategyReplyEvent(
					event.getKey(), event.getReceiver(), record.origTxId, 
					event.isSuccess(), event.getMessage()));
	}

	protected void processAmendMultiInstrumentStrategyEvent(
			AmendMultiInstrumentStrategyEvent event, IUserSocketContext ctx) {
		
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		AmendMultiInstrumentStrategyEvent request = new AmendMultiInstrumentStrategyEvent(
				event.getKey(), event.getReceiver(), event.getFields(), event.getInstruments(), txId);

		sendEventToManager(request);
	}

	protected void processAmendMultiInstrumentStrategyReplyEvent(
			AmendMultiInstrumentStrategyReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new AmendMultiInstrumentStrategyReplyEvent(
					event.getKey(), event.getReceiver(), record.origTxId, 
					event.isSuccess(), event.getMessage()));
	}

	protected void processCancelMultiInstrumentStrategyEvent(
			CancelMultiInstrumentStrategyEvent event, IUserSocketContext ctx) {
		String txId = IdGenerator.getInstance().getNextID();
		PendingRecord record = new PendingRecord(txId, event.getTxId(), ctx);
		pendingRecords.put(record.txId, record);
		
		CancelMultiInstrumentStrategyEvent request = new CancelMultiInstrumentStrategyEvent(
				event.getKey(), event.getReceiver(), txId);

		sendEventToManager(request);
	}

	protected void processCancelMultiInstrumentStrategyReplyEvent(
			CancelMultiInstrumentStrategyReplyEvent event) {
		PendingRecord record = pendingRecords.remove(event.getTxId());
		if(null == record)
			return;
		
		if(record.ctx.isOpen())
			record.ctx.send(new CancelMultiInstrumentStrategyReplyEvent(
					event.getKey(), event.getReceiver(), record.origTxId, 
					event.isSuccess(), event.getMessage()));
	}

	public void processAccountDynamicUpdateEvent(AccountDynamicUpdateEvent event) {
		
	}
	
	public void processOpenPositionDynamicUpdateEvent(OpenPositionDynamicUpdateEvent event) {
		
	}
	
	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		ParentOrder order = event.getOrder();
		if(order.getState() == StrategyState.Terminated || order.getOrdStatus().isCompleted())
			orders.remove(order.getId());
		else if(accountUserMap.containsKey(order.getAccount())) {
			orders.put(order.getId(), order);
		}
		sendEventToUser(order.getUser(), event);
	}

	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		
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
