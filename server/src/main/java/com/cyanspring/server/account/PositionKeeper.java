package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.apievent.obj.OrderType;
import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountException;
import com.cyanspring.common.account.AccountSetting;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.ICommissionManager;
import com.cyanspring.common.account.ILeverageManager;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PositionException;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.fx.FxUtils;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteUtils;
import com.cyanspring.common.message.ErrorMessage;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

public class PositionKeeper {
	private static final Logger log = LoggerFactory
			.getLogger(PositionKeeper.class);

	private ConcurrentHashMap<String, String> accounts = new ConcurrentHashMap<String, String>(); // for synchronization
	private ConcurrentHashMap<String, Map<String, List<OpenPosition>>> accountPositions = 
				new ConcurrentHashMap<String, Map<String, List<OpenPosition>>>();
	private ConcurrentHashMap<String, List<ClosedPosition>> closedPositions = 
				new ConcurrentHashMap<String, List<ClosedPosition>>();
	private ConcurrentHashMap<String, List<Execution>> executions = new ConcurrentHashMap<String, List<Execution>>();
	private ConcurrentHashMap<String, Map<String, Map<String, ParentOrder>>> parentOrders = // keys: account/symbol/orderId
				new ConcurrentHashMap<String, Map<String, Map<String, ParentOrder>>>();

	private IPositionListener listener;
	private IQuoteFeeder quoteFeeder;
	private boolean useMid;

	@Autowired
	IRefDataManager refDataManager;
	
	@Autowired
	IFxConverter fxConverter;
	
	@Autowired
	AccountKeeper accountKeeper;
	
	@Autowired
	ILeverageManager leverageManager;
	
	@Autowired
	ICommissionManager commissionManager;

	private ClosePositionLock closePositionLock = new ClosePositionLock();
	
	public IPositionListener setListener(IPositionListener listener) {
		IPositionListener result = this.listener;
		this.listener = listener;
		return result;
	}

	private String getSyncAccount(String account) {
		String synAccount = accounts.putIfAbsent(account, account);
		return synAccount == null? account : synAccount;
	}
	
	public void processParentOrder(ParentOrder order, Account account) {
		closePositionLock.processParentOrder(order);
		
		Map<String, Map<String, ParentOrder>> accountMap = parentOrders.get(order.getAccount());
		if(null == accountMap) {
			accountMap = new ConcurrentHashMap<String, Map<String, ParentOrder>>();
			Map<String, Map<String, ParentOrder>> existing = parentOrders.putIfAbsent(order.getAccount(), accountMap);
			accountMap = existing == null?accountMap:existing;
		}
		
		synchronized(getSyncAccount(order.getAccount())) {
			Map<String, ParentOrder> symbolMap = accountMap.get(order.getSymbol());
			if(null == symbolMap) {
				symbolMap = new ConcurrentHashMap<String, ParentOrder>();
				accountMap.put(order.getSymbol(), symbolMap);
			}
			
			if(order.getOrdStatus().isCompleted() || order.getState() == StrategyState.Terminated) {
				symbolMap.remove(order.getId());
				log.debug("Remove completed parentOrder: " + order.getId());
			} else {
				symbolMap.put(order.getId(), order);
			}
		}
		
		this.updateAccountDynamicData(account);
		notifyAccountDynamicUpdate(account);

		OpenPosition update = getOverallPosition(account, order.getSymbol());
		notifyOpenPositionUpdate(update);
	}
	
	private OpenPosition createOpenPosition(Execution execution, Account account) {
		AccountSetting accountSetting = null;
		try {
			accountSetting = accountKeeper.getAccountSetting(account.getId());
		} catch (AccountException e) {
			log.error(e.getMessage(), e);
		}

		double margin = 0.0;
		if(null != refDataManager) {
			margin = FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), 
					execution.getSymbol(), execution.getQuantity(), execution.getPrice());
			RefData refData = refDataManager.getRefData(execution.getSymbol());
			double leverage = leverageManager.getLeverage(refData, accountSetting);
			margin /= leverage;
			log.debug("Open position margin: " + margin + ", " + leverage);
			account.setMarginHeld(account.getMarginHeld() + margin);
		}
		return new OpenPosition(execution, margin);
	}
	
	public void processExecution(Execution execution, Account account) throws PositionException {
		if(null == execution.getAccount())
			throw new PositionException("Execution has no account: " + execution,ErrorMessage.ACCOUNT_NOT_EXIST);
		
		addExecution(execution);
		
		//update execution
		boolean parentOrderUdpated = false;
		ParentOrder parentOrder = null;
		Map<String, Map<String, ParentOrder>> accountOrders =  parentOrders.get(execution.getAccount());
		if(null != accountOrders) {
			Map<String, ParentOrder> symbolOrders = accountOrders.get(execution.getSymbol());
			if(null != symbolOrders) {		
				parentOrder = symbolOrders.get(execution.getParentOrderId());
				if(null != parentOrder) {
					log.debug("Before processing execution: " + parentOrder);
					parentOrder.processExecution(execution);
					parentOrderUdpated = true;
					if(PriceUtils.EqualGreaterThan(parentOrder.getCumQty(), parentOrder.getQuantity())) { // fully filled
						symbolOrders.remove(execution.getParentOrderId());
						log.debug("Remove fully filled parentOrder: " + execution.getParentOrderId());
					}
				}
			}
		} 
		
		if(!parentOrderUdpated){
			log.warn("Cant location parent order: " + execution.getParentOrderId());
		}
		
		Map<String, List<OpenPosition>> symbolPositions = accountPositions.get(execution.getAccount());
		if(null == symbolPositions) {
			symbolPositions = new HashMap<String, List<OpenPosition>>();
			Map<String, List<OpenPosition>> existing = this.accountPositions.putIfAbsent(execution.getAccount(), symbolPositions);
			symbolPositions = existing == null?symbolPositions:existing;
		}
		
		synchronized(getSyncAccount(execution.getAccount())) {
			List<OpenPosition> list = symbolPositions.get(execution.getSymbol());
			if(null == list) {
				list = new LinkedList<OpenPosition>();
				symbolPositions.put(execution.getSymbol(), list);
			}
			
			if(list.size() <= 0) { // no existing position, just add it
				OpenPosition position = createOpenPosition(execution, account);
				list.add(position);
				this.notifyUpdateDetailOpenPosition(position);
			} else {
			
				OpenPosition overallPosition = getOverallPosition(list, account, execution.getSymbol());
		
				double execPos = execution.toPostion();
				if(overallPosition.oppositePosition(execPos)) {
					
					Iterator<OpenPosition> it = list.iterator();
					while (it.hasNext() && !PriceUtils.isZero(execPos)){
						OpenPosition pos = it.next();
						if(PriceUtils.EqualGreaterThan(Math.abs(execPos), Math.abs(pos.getQty()))) {
							transferToClosedPositions(pos, execution, account);
							it.remove();
							this.notifyRemoveDetailOpenPosition(pos);
							execPos += pos.getQty();
						} else {
							OpenPosition transfer = pos.split(-execPos);
							transferToClosedPositions(transfer, execution, account);
							execPos = 0;
							this.notifyUpdateDetailOpenPosition(pos);
						}
					}
					
					//if there is still execution quantity left
					if(PriceUtils.GreaterThan(Math.abs(execPos), 0)) {
						OpenPosition position = createOpenPosition(execution, account);
						position.setQty(execPos);
						list.add(position);
						this.notifyUpdateDetailOpenPosition(position);
					}
					
				} else { // same side just add it
					OpenPosition position = createOpenPosition(execution, account);
					list.add(position);
					this.notifyUpdateDetailOpenPosition(position);
				}
			}
			
			AccountSetting accountSetting = null;
			try {
				accountSetting = accountKeeper.getAccountSetting(account.getId());
			} catch (AccountException e) {
				log.error(e.getMessage(), e);
			}
			RefData refData = refDataManager.getRefData(execution.getSymbol());
			
			if(!PriceUtils.isZero(commissionManager.getCommission(refData, accountSetting, execution))) {
				double value = FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), 
						execution.getSymbol(), execution.getQuantity(), execution.getPrice());
				double commision = commissionManager.getCommission(refData, accountSetting, value, execution);
				account.updatePnL(-commision);
			} 

			updateAccountDynamicData(account);

			OpenPosition update = getOverallPosition(account, execution.getSymbol());
			notifyOpenPositionUpdate(update);

			notifyAccountUpdate(account);
			
		}

		if(parentOrder != null && 
			checkAccountPositionLock(parentOrder.getAccount(), parentOrder.getSymbol()) &&
			parentOrder.getOrdStatus().equals(OrdStatus.FILLED) &&
			PriceUtils.Equal(parentOrder.getCumQty(), parentOrder.getQuantity())) {
				unlockAccountPosition(parentOrder.getId());
				log.debug("Close position action completed ex: " + parentOrder.getAccount() + ", " + parentOrder.getSymbol() + ", " + parentOrder.getId());
		}
		
		closePositionLock.processExecution(execution);

	}
	
	public List<ParentOrder> getParentOrders(String account) {
		List<ParentOrder> orders = new ArrayList<ParentOrder>();
		
		Map<String, Map<String, ParentOrder>> symbolMap = parentOrders.get(account);
		if(null != symbolMap) {
			for(Map<String, ParentOrder> orderMap: symbolMap.values()) {
				if(null != orderMap) {
					orders.addAll(orderMap.values());
				}
			}
		}
		return orders;
	}
	
	public List<ParentOrder> getParentOrders(String account, String symbol) {
		List<ParentOrder> orders = new ArrayList<ParentOrder>();
		
		Map<String, Map<String, ParentOrder>> symbolMap = parentOrders.get(account);
		if(null != symbolMap) {
			Map<String, ParentOrder> orderMap = symbolMap.get(symbol);
			if(null != orderMap) {
				orders.addAll(orderMap.values());
			}
		}
		return orders;
	}
	
	protected void notifyRemoveDetailOpenPosition(OpenPosition position) {
		if(null != listener)
			listener.onRemoveDetailOpenPosition(position);
	}
	
	protected void notifyUpdateDetailOpenPosition(OpenPosition position) {
		if(null != listener)
			listener.onUpdateDetailOpenPosition(position);
	}
	
	protected void notifyOpenPositionUpdate(OpenPosition position) {
		if(null != listener)
			listener.onOpenPositionUpdate(position);
	}
	
	protected void notifyOpenPositionUrPnLUpdate(OpenPosition position) {
		if(null != listener)
			listener.onOpenPositionDynamiceUpdate(position);
	}
	
	protected void notifyClosedPositionUpdate(ClosedPosition position) {
		if(null != listener)
			listener.onClosedPositionUpdate(position);
	}
	
	protected void notifyAccountDynamicUpdate(Account account) {
		if(null != listener)
			listener.onAccountDynamicUpdate(account);
	}
	
	protected void notifyAccountUpdate(Account account) {
		if(null != listener)
			listener.onAccountUpdate(account);
	}
	
	protected void transferToClosedPositions(OpenPosition position, Execution execution, Account account) {
		List<ClosedPosition> list = closedPositions.get(position.getAccount());
		if(null == list) {
			list = new LinkedList<ClosedPosition>();
			List<ClosedPosition> existing = closedPositions.putIfAbsent(position.getAccount(), list);
			list = existing == null?list:existing;
		}
		
		ClosedPosition pos = ClosedPosition.create(refDataManager, fxConverter, position, execution, account);
		list.add(pos); 

		account.updatePnL(pos.getAcPnL());
		account.setMarginHeld(account.getMarginHeld() - Math.abs(position.getMargin()));
				
		notifyClosedPositionUpdate(pos);
	}
	
	public List<ClosedPosition> getClosedPositions(String account) {
		synchronized(getSyncAccount(account)) {
			List<ClosedPosition> result = new ArrayList<ClosedPosition>();
			List<ClosedPosition> positions = closedPositions.get(account);
			if(null != positions)
				result.addAll(positions);
			return result;
		}
	}
	
	private void addExecution(Execution execution) {
		List<Execution> list = executions.get(execution.getAccount());
		if(null == list) {
			list = new LinkedList<Execution>();
			List<Execution> prev = executions.putIfAbsent(execution.getAccount(), list);
			list = prev == null? list : prev;
		}
		
		synchronized(getSyncAccount(execution.getAccount())) {
			list.add(execution);
		}
	}
	
	// this one gives the raw positions
	protected List<OpenPosition> getOpenPositions(String account) {
			Map<String, List<OpenPosition>> symbolPositions = accountPositions.get(account);
			if(null == symbolPositions)
				return null;

			List<OpenPosition> result = new LinkedList<OpenPosition>();
			synchronized(getSyncAccount(account)) {
				for(List<OpenPosition> list: symbolPositions.values()) {
					if(null != list)
						result.addAll(list);
				}
			}
			return result;
	}
	
	// this one gives all the overall positions for an account on each symbol
	public List<OpenPosition> getOverallPosition(Account account) {
		List<OpenPosition> result = new ArrayList<OpenPosition>();
		Map<String, List<OpenPosition>> ap = accountPositions.get(account.getId());
		if(null == ap)
			return result;

		synchronized(getSyncAccount(account.getId())) {
			for(String symbol: ap.keySet()) {
				OpenPosition pos = getOverallPosition(account, symbol);
				if(!PriceUtils.isZero(pos.getQty()))
					result.add(pos);
			}
		}
		return result;
	}

	// this one gives the overall positions for a specific account and symbol
	public OpenPosition getOverallPosition(Account account, String symbol) {
		OpenPosition result = new OpenPosition(account.getUserId(), account.getId(), symbol, 0, 0, 0);
		Map<String, List<OpenPosition>> ap = accountPositions.get(account.getId());
		if(null == ap)
			return result;
		
		List<OpenPosition> list = ap.get(symbol);
		if(null == list)
			return result;
		
		OpenPosition pos = null;
		synchronized(getSyncAccount(account.getId())) {
			try {
				pos = getOverallPosition(list, account, symbol);
				if(null != quoteFeeder) {
					Quote quote = quoteFeeder.getQuote(symbol);
					if(null != quote && null != pos) {
						double price = QuoteUtils.getPnlPrice(quote, pos.getQty(), useMid);
						double lastPrice = QuoteUtils.getLastPrice(quote);
						if(!PriceUtils.validPrice(price))
							price = QuoteUtils.getValidPrice(quote);
						
						if(PriceUtils.validPrice(price)) {
							double pnl = FxUtils.calculatePnL(refDataManager, pos.getSymbol(), pos.getQty(), 
									(price-pos.getPrice()));
							pos.setPnL(pnl);
							double lastPnL = FxUtils.calculatePnL(refDataManager, pos.getSymbol(), pos.getQty(), lastPrice-pos.getPrice());
							pos.setLastPnL(lastPnL);
							double urPnL = FxUtils.convertPnLToCurrency(refDataManager, fxConverter, account.getCurrency(), 
									quote.getSymbol(), pos.getPnL());
							pos.setAcPnL(urPnL);
							double acLastPnL = FxUtils.convertPnLToCurrency(refDataManager, fxConverter, account.getCurrency(), 
									quote.getSymbol(), pos.getLastPnL());
							pos.setAcLastPnL(acLastPnL);
						}
					}
				}
			} catch (PositionException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return null == pos? result : pos;
	}

	private OpenPosition getOverallPosition(List<OpenPosition> list, Account account, String symbol) throws PositionException {
		if (null == list || list.size() <= 0)
			return null;
		
		checkOverallPosition(list);
		RefData refData = refDataManager.getRefData(symbol);
		double qty = 0;
		double amount = 0;
		double PnL = 0;
		double lastPnL = 0;
		double margin = 0;
		double availableQty = 0;
		for (OpenPosition pos : list) {
			qty += pos.getQty();
			amount += pos.getQty() * pos.getPrice();
			PnL += pos.getPnL();
			lastPnL += pos.getLastPnL();
			margin += pos.getMargin();
			availableQty += pos.getDetailAvailableQty(refData);
		}
		double price = amount / qty;

        if (availableQty > 0) {
            availableQty -= getPendingSellQty(account, symbol);

			if (availableQty < 0) {
				availableQty = 0;
			}

        } else {
            availableQty += getPendingBuyQty(account, symbol);

			if (availableQty > 0) {
				availableQty = 0;
			}
        }

		OpenPosition result = new OpenPosition(list.get(0).getUser(), list.get(0).getAccount(),
				list.get(0).getSymbol(), qty, price, margin);
		result.setPnL(PnL);
		result.setLastPnL(lastPnL);
		result.setAvailableQty(availableQty);
		return result;
	}

	private double getPendingSellQty(Account account, String symbol) {
		double pendingSellQty = 0;

		List<ParentOrder> orders = getParentOrders(account.getId(), symbol);

		for (ParentOrder o : orders) {

			if (o.getOrdStatus().isCompleted() || !o.getSide().isSell()) {
				continue;
			}

			pendingSellQty += o.getRemainingQty();
		}

		return pendingSellQty;
	}

	private double getPendingBuyQty(Account account, String symbol) {
		double pendingBuyQty = 0;

		List<ParentOrder> orders = getParentOrders(account.getId(), symbol);

		for (ParentOrder o : orders) {

			if (o.getOrdStatus().isCompleted() || !o.getSide().isBuy()) {
				continue;
			}

			pendingBuyQty += o.getRemainingQty();
		}

		return pendingBuyQty;
	}
	
	private void checkOverallPosition(List<OpenPosition> list) throws PositionException {
		if(null == list || list.size() < 2)
			return;
		
		OpenPosition first = list.get(0);
		for(OpenPosition pos: list) {
			if(!pos.getAccount().equals(first.getAccount()))
				throw new PositionException("Position list contains different account: " + 
							first + " vs " + pos,ErrorMessage.POSITION_CONTAINS_DIFF_ACCOUNT);
			
			if(!pos.getSymbol().equals(first.getSymbol()))
				throw new PositionException("Position list contains different symbol: " + 
							first + " vs " + pos,ErrorMessage.POSITION_CONTAINS_DIFF_SYMBOL);
			
			if(first.oppositePosition(pos))
				throw new PositionException("Position list contains different side: " + 
							first + " vs " + pos,ErrorMessage.POSITION_CONTAINS_DIFF_SIDE);
			
			if(PriceUtils.isZero(pos.getQty()))
				throw new PositionException("Position list contains 0: " + pos,ErrorMessage.POSITION_CONTAINS_ZERO); 
		}
	}
	
	private List<String> getCombinedSymbolList(String account) {
		List<String> symbols = new LinkedList<String>();
		Map<String, List<OpenPosition>> symbolPositions = accountPositions.get(account);
		Map<String, Map<String, ParentOrder>> symbolOpenOrders = parentOrders.get(account);
		if(null != symbolPositions && null != symbolOpenOrders) {
			symbols.addAll(symbolPositions.keySet());
			for(String symbol: symbolOpenOrders.keySet()) {
				if(!symbolPositions.containsKey(symbol))
					symbols.add(symbol);
			}
		} else if(null != symbolPositions) {
			symbols.addAll(symbolPositions.keySet());
		} else if(null != symbolOpenOrders) {
			symbols.addAll(symbolOpenOrders.keySet());
		}
		return symbols;
	}

	public void updateAccountDynamicData(Account account) {
		double accountUrPnL = 0;
		double accountUrLastPnl = 0;
		double leverageUrPnL = 0;
		double marginValue = 0;
		double marginHeld = 0;
		if(null == quoteFeeder)
			return;

		AccountSetting accountSetting;
		try {
			accountSetting = accountKeeper.getAccountSetting(account.getId());
		} catch (AccountException e) {
			log.error(e.getMessage(), e);
			return;
		}
		
		synchronized(getSyncAccount(account.getId())) {
			// since margin is contributed by both open position and open orders, we need
			// to work out a combined list of symbols with either one of them
			// e.g. a symbol may contains only open order and no open position
			// or open positions and no open orders
			List<String> symbolList = getCombinedSymbolList(account.getId());
			for(String symbol: symbolList) {
				Quote quote = quoteFeeder.getQuote(symbol);
				if(null == quote) {
					continue;
				}

				RefData refData = refDataManager.getRefData(symbol);
				double lev = leverageManager.getLeverage(refData, accountSetting);

				Map<String, List<OpenPosition>> symbolPositions = accountPositions.get(account.getId());
				if(null != symbolPositions) {
					List<OpenPosition> list = symbolPositions.get(symbol);
					if(null != list) {
						boolean validMarketablePrice = true;
						for(OpenPosition position: list) {
							double price = QuoteUtils.getPnlPrice(quote, position.getQty(), useMid);
							double lastPrice = QuoteUtils.getLastPrice(quote);
							
							validMarketablePrice = PriceUtils.validPrice(price);
							if(!validMarketablePrice)
								break;
							double pnl = FxUtils.calculatePnL(refDataManager, position.getSymbol(), position.getQty(), 
									(price-position.getPrice()));
							position.setPnL(pnl);
							double lastPnL = FxUtils.calculatePnL(refDataManager, position.getSymbol(), position.getQty(), 
									(lastPrice-position.getPrice()));
							position.setLastPnL(lastPnL);
						}
						
						if(!validMarketablePrice)
							continue;
						
						OpenPosition overallPosition = getOverallPosition(account, symbol);
						accountUrPnL += overallPosition.getAcPnL();
						accountUrLastPnl += overallPosition.getAcLastPnL();
						leverageUrPnL += overallPosition.getAcPnL() * (1-1/lev);
					}
				}
				
				double value = getMarginValueByAccountAndSymbol(account, symbol, quote, lev);
				marginValue += value * lev;
				marginHeld += value;
			}
			account.setUrPnL(accountUrPnL);
			account.setUrLastPnL(accountUrLastPnl);
			
			double accountMargin = (account.getCash() +  account.getUrPnL()) * leverageManager.getLeverage(null, accountSetting);
			if(PriceUtils.GreaterThan(accountSetting.getMargin(), 0))
				accountMargin = (account.getCash() +  account.getUrPnL()) * accountSetting.getMargin();
			account.setMargin(accountMargin - marginValue);
			account.setCashDeduct(account.getCash() - account.getMarginHeld() + leverageUrPnL);
			account.setCashAvailable(account.getCash() - marginHeld + leverageUrPnL);
		}
	}
	
	private double getMarginQtyByAccountAndSymbol(Account account, String symbol, double extraQty) {
		double buyQty = 0;
		double sellQty = 0;

		synchronized(getSyncAccount(account.getId())) {
			Map<String, Map<String, ParentOrder>> accountMap = parentOrders.get(account.getId());
			if (null != accountMap) {
				Map<String, ParentOrder> symbolMap = accountMap.get(symbol);
				if(null != symbolMap) {
					for(ParentOrder order: symbolMap.values()) {
						if(order.getOrdStatus().isReady()) {
							if(order.getSide().isBuy()) {
								buyQty += order.getQuantity() - order.getCumQty();
							} else {
								sellQty += order.getQuantity() - order.getCumQty();
							}
						}
					}
				}
			}
			
			OpenPosition overallPosition = getOverallPosition(account, symbol);
			double positionQty = overallPosition.getQty();
			if(positionQty > 0)
				buyQty += positionQty;
			else
				sellQty -= positionQty;
		}
		
		if(extraQty > 0)
			buyQty += extraQty;
		else
			sellQty -= extraQty;

		// take the maximum one
		return buyQty > sellQty? buyQty:-sellQty;
	}
	
	private double getMarginValueByAccountAndSymbol(Account account, String symbol, Quote quote, double lev) {
		double buyValue = 0;
		double sellValue = 0;

		synchronized(getSyncAccount(account.getId())) {
			Map<String, Map<String, ParentOrder>> accountMap = parentOrders.get(account.getId());
			if (null != accountMap) {
				Map<String, ParentOrder> symbolMap = accountMap.get(symbol);
				if(null != symbolMap) {
					for(ParentOrder order: symbolMap.values()) {
						double price;
						double orderPrice = order.getPrice();
						if(order.getOrderType().equals(OrderType.Limit) && PriceUtils.validPrice(orderPrice)) {
							price = orderPrice;
						} else {
							price = QuoteUtils.getMarketablePrice(quote, order.getSide().isBuy()?1:-1);
							if(!PriceUtils.validPrice(price))
								price =	QuoteUtils.getValidPrice(quote);
						}
						if(!order.getOrdStatus().isCompleted()) {
							double value = Math.abs(FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), quote.getSymbol(), 
									(order.getQuantity() - order.getCumQty()), price));
							if(order.getSide().isBuy()) {
								buyValue += value/lev;
							} else {
								sellValue += value/lev;
							}
						}
					}
				}
			}
			
			OpenPosition overallPosition = getOverallPosition(account, symbol);
			double positionQty = overallPosition.getQty();
			
			if(positionQty > 0)
				buyValue += overallPosition.getMargin();
			else
				sellValue += overallPosition.getMargin();
		}
		
		// take the maximum one
		return buyValue > sellValue? buyValue:sellValue;
	}
	
//	private double getMarginValueByAccountAndSymbol(Account account, String symbol, Quote quote) {
//		double marginQty = getMarginQtyByAccountAndSymbol(account, symbol, 0);
//		double price = QuoteUtils.getMarketablePrice(quote, marginQty);
//		if(!PriceUtils.validPrice(price))
//			price =	QuoteUtils.getValidPrice(quote);
//		return Math.abs(FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), quote.getSymbol(), 
//				marginQty, price));
//	}
	
	public boolean checkMarginDeltaByAccountAndSymbol(Account account, String symbol, Quote quote, double extraQty) throws AccountException {
		double currentMarginQty = getMarginQtyByAccountAndSymbol(account, symbol, 0);
		double futureMarginQty = getMarginQtyByAccountAndSymbol(account, symbol, extraQty);
		if(Math.abs(currentMarginQty) >= Math.abs(futureMarginQty))
			return true; // reducing qty, ok
		
		double deltaQty = Math.abs(futureMarginQty) - Math.abs(currentMarginQty);
		if(futureMarginQty < 0) {
			deltaQty = -deltaQty;
		}

		double price = QuoteUtils.getMarketablePrice(quote, deltaQty);
		if(!PriceUtils.validPrice(price))
			price =	QuoteUtils.getValidPrice(quote);
		
		if(!PriceUtils.validPrice(price)) {
			log.error("Quote invalid: " + quote);
			return false;
		}
		
		double deltaValue = Math.abs(FxUtils.convertPositionToCurrency(refDataManager, fxConverter,
				account.getCurrency(), quote.getSymbol(), 
				deltaQty, price));
				
		AccountSetting accountSetting = accountKeeper.getAccountSetting(account.getId());

		RefData refData = refDataManager.getRefData(symbol);
		double leverage = leverageManager.getLeverage(refData, accountSetting);
//		double commission = commissionManager.getCommission(refData, accountSetting, deltaValue);
//		deltaValue += commission * leverage ;
		
		if(account.getCashAvailable() * Default.getMarginCall() - deltaValue/leverage >= 0) {
			return true;
		} else {
//			log.debug("Credit check fail: " + account.getCashAvailable() + ", " + deltaValue + ", " + leverage + ", " + commission);
			log.debug("Credit check fail: " + account.getCashAvailable() + ", " + deltaValue + ", " + leverage);
			return false;
		}
	}
	
	public List<Execution> getExecutions(String account) {
		List<Execution> list = new ArrayList<Execution>();
		List<Execution> exes = executions.get(account);
		if(null != exes)
			list.addAll(exes);
		return list;
	}
	
	protected IQuoteFeeder getQuoteFeeder() {
		return quoteFeeder;
	}

	protected void setQuoteFeeder(IQuoteFeeder quoteFeeder) {
		this.quoteFeeder = quoteFeeder;
	}
	
	public Quote getQuote(String symbol) {
		if(null == this.quoteFeeder)
			return null;
		return this.quoteFeeder.getQuote(symbol);
	}

	public void injectExecutions(List<Execution> executions) {
		Date prev = TimeUtil.getPreviousDay();
		for(Execution execution: executions) {
			if(execution.getCreated().after(prev))
				addExecution(execution);
		}
	}

	public void injectOpenPositions(List<OpenPosition> positions) {
		for(OpenPosition position: positions) {
			Map<String, List<OpenPosition>> symbolPositions = accountPositions.get(position.getAccount());
			if(null == symbolPositions) {
				symbolPositions = new ConcurrentHashMap<String, List<OpenPosition>>();
				this.accountPositions.put(position.getAccount(), symbolPositions);
			}
			
			List<OpenPosition> list = symbolPositions.get(position.getSymbol());
			if(null == list) {
				list = new LinkedList<OpenPosition>();
				symbolPositions.put(position.getSymbol(), list);
			}
			
			list.add(position);
		}
	}
	
	public void injectClosedPositions(List<ClosedPosition> positions) {
		for(ClosedPosition position: positions) {
			List<ClosedPosition> list = closedPositions.get(position.getAccount());
			if(null == list) {
				list = new LinkedList<ClosedPosition>();
				closedPositions.put(position.getAccount(), list);
			}
			list.add(position);
		}
	}
	
	public Account rollAccount(Account account) {
		synchronized(getSyncAccount(account.getId())) {
			account.updateEndOfDay();
			try {
				return account.clone();
			} catch (CloneNotSupportedException e) {
				log.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	public void resetMarginHeld() {
		log.info("Resetting margin held...");
		for(Entry<String, Map<String, List<OpenPosition>>> entry: accountPositions.entrySet()) {
			if(null == entry)
				continue;
			
			Account account = accountKeeper.getAccount(entry.getKey());
			AccountSetting accountSetting = null;
			try {
				accountSetting = accountKeeper.getAccountSetting(entry.getKey());
			} catch (AccountException e) {
				log.error(e.getMessage(), e);
			}

			account.setMarginHeld(0.0);
			for(List<OpenPosition> list: entry.getValue().values()) {
				if(null == list)
					continue;
				
				double total = 0.0;
				for(OpenPosition position: list) {
					double margin = FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), 
							position.getSymbol(), Math.abs(position.getQty()), position.getPrice());
					RefData refData = refDataManager.getRefData(position.getSymbol());
					double leverage = leverageManager.getLeverage(refData, accountSetting);
					margin /= leverage;
					total += margin;
					position.setMargin(margin);
					this.notifyUpdateDetailOpenPosition(position);
				}
				account.setMarginHeld(account.getMarginHeld()+total);
				
			}
			this.notifyAccountUpdate(account);
		}
		log.info("Resetting margin held done");
	}
	
	public void updateAccountOpenPosition(String account, String symbol, double price) throws AccountException{
		List<OpenPosition> list = getOpenPositions(account);
		Account acc = accountKeeper.getAccount(account);
		AccountSetting settings = accountKeeper.getAccountSetting(account);
		for (OpenPosition position : list) {
			if (!position.getSymbol().equals(symbol))
				continue;
			if(refDataManager == null)
				continue;

			Quote quote = quoteFeeder.getQuote(symbol);
			if(quote == null)
				continue;
			
			double qPrice = QuoteUtils.getPnlPrice(quote, position.getQty(), useMid);
			if(!PriceUtils.validPrice(qPrice))
				qPrice = QuoteUtils.getValidPrice(quote);
			if(!PriceUtils.validPrice(qPrice))
				continue;
			
			RefData refData = refDataManager.getRefData(symbol);
			double margin = FxUtils.convertPositionToCurrency(refDataManager, fxConverter, acc.getCurrency(), 
					position.getSymbol(), Math.abs(position.getQty()), price);
			double leverage = leverageManager.getLeverage(refData, settings);
			margin /= leverage;
			acc.setMarginHeld(acc.getMarginHeld() - position.getMargin() + margin);
			position.setMargin(margin);
			position.setPrice(price);
			
			double pnl = FxUtils.calculatePnL(refDataManager, position.getSymbol(), position.getQty(), 
					(qPrice-price));
			position.setPnL(pnl);
			double urPnL = FxUtils.convertPnLToCurrency(refDataManager, fxConverter, acc.getCurrency(), 
					quote.getSymbol(), position.getPnL());
			position.setAcPnL(urPnL);
							
			this.notifyUpdateDetailOpenPosition(position);
		}
		
		OpenPosition update = getOverallPosition(acc, symbol);
		this.notifyOpenPositionUpdate(update);
	}
	
	public void lockAccountPosition(ParentOrder order) throws AccountException {
		closePositionLock.lockAccountPosition(order);
	}
	
	public String unlockAccountPosition(String orderId) {
		return closePositionLock.unlockAccountPosition(orderId);
	}
	
	public boolean checkAccountPositionLock(String account, String symbol) {
		return closePositionLock.checkAccountPositionLock(account, symbol);
	}
	
	public List<ParentOrder> getTimeoutLocks() {
		return closePositionLock.getTimeoutOrders();
	}	
	
	public void resetAccount(String accountId) {
		Account account = accountKeeper.getAccount(accountId);
		synchronized(getSyncAccount(accountId)) {
			account.reset();
			parentOrders.remove(accountId);
			accountPositions.remove(accountId);
			closedPositions.remove(accountId);
			executions.remove(accountId);
		}	
		this.notifyAccountUpdate(account);
	}

	public boolean isUseMid() {
		return useMid;
	}

	public void setUseMid(boolean useMid) {
		this.useMid = useMid;
	}
		
}
