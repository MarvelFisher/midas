package com.cyanspring.server.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PositionException;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.fx.FxException;
import com.cyanspring.common.fx.FxUtils;
import com.cyanspring.common.fx.IFxConverter;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

public class PositionKeeper {
	private static final Logger log = LoggerFactory
			.getLogger(PositionKeeper.class);

	private ConcurrentHashMap<String, String> accounts = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, Map<String, List<OpenPosition>>> accountPositions = 
				new ConcurrentHashMap<String, Map<String, List<OpenPosition>>>();
	private ConcurrentHashMap<String, List<ClosedPosition>> closedPositions = 
				new ConcurrentHashMap<String, List<ClosedPosition>>();
	private ConcurrentHashMap<String, List<Execution>> executions = new ConcurrentHashMap<String, List<Execution>>();
	private ConcurrentHashMap<String, Map<String, Map<String, ParentOrder>>> parentOrders = // keys: account/symbol/orderId
				new ConcurrentHashMap<String, Map<String, Map<String, ParentOrder>>>();
	
	private IPositionListener listener;
	private IQuoteFeeder quoteFeeder;

	@Autowired
	RefDataManager refDataManager;
	
	@Autowired
	IFxConverter fxConverter;
	
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
		Map<String, Map<String, ParentOrder>> accountMap = parentOrders.get(order.getAccount());
		if(null == accountMap) {
			accountMap = new HashMap<String, Map<String, ParentOrder>>();
			Map<String, Map<String, ParentOrder>> existing = parentOrders.putIfAbsent(order.getAccount(), accountMap);
			accountMap = existing == null?accountMap:existing;
		}

		synchronized(getSyncAccount(order.getAccount())) {
			Map<String, ParentOrder> symbolMap = accountMap.get(order.getSymbol());
			if(null == symbolMap) {
				symbolMap = new HashMap<String, ParentOrder>();
				accountMap.put(order.getSymbol(), symbolMap);
			}
			
			if(order.getOrdStatus().isCompleted() || order.getState() == StrategyState.Terminated) {
				symbolMap.remove(order.getId());
			} else {
				symbolMap.put(order.getId(), order);
			}
		}
		
		this.updateAccountDynamicData(account);
	}
	
	public void processExecution(Execution execution, Account account) throws PositionException {
		if(null == execution.getAccount())
			throw new PositionException("Execution has no account: " + execution);
		
		addExecution(execution);
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
			
			boolean needAccountUpdate = false;
			if(list.size() <= 0) { // no existing position, just add it
				OpenPosition position = new OpenPosition(execution);
				list.add(position);
				this.notifyUpdateDetailOpenPosition(position);
			} else {
			
				OpenPosition overallPosition = getOverallPosition(list);
		
				double execPos = execution.toPostion();
				if(overallPosition.oppositePosition(execPos)) {
					
					Iterator<OpenPosition> it = list.iterator();
					while (it.hasNext() && !PriceUtils.isZero(execPos)){
						OpenPosition pos = it.next();
						if(PriceUtils.EqualGreaterThan(Math.abs(execPos), Math.abs(pos.getQty()))) {
							transferToClosedPositions(pos, execution, account);
							needAccountUpdate = true;
							it.remove();
							this.notifyRemoveDetailOpenPosition(pos);
							execPos += pos.getQty();
						} else {
							OpenPosition transfer = pos.split(-execPos);
							transferToClosedPositions(transfer, execution, account);
							needAccountUpdate = true;
							execPos = 0;
							this.notifyUpdateDetailOpenPosition(pos);
						}
					}
					
					//if there is still execution quantity left
					if(PriceUtils.GreaterThan(Math.abs(execPos), 0)) {
						OpenPosition position = new OpenPosition(execution);
						position.setQty(execPos);
						list.add(position);
						this.notifyUpdateDetailOpenPosition(position);
					}
					
				} else { // same side just add it
					OpenPosition position = new OpenPosition(execution);
					list.add(position);
					this.notifyUpdateDetailOpenPosition(position);
				}
			}
			
			OpenPosition update = getOverallPosition(account, execution.getSymbol());
			notifyOpenPositionUpdate(update);
			if(!PriceUtils.isZero(Default.getCommision())) {
				double value = FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), 
						execution.getSymbol(), execution.getQuantity(), execution.getPrice());
				double commision = Default.getCommision() * value;
				account.updatePnL(-commision);
				needAccountUpdate = true;
			} 
			
			if(needAccountUpdate)
				notifyAccountUpdate(account);
			
			updateAccountDynamicData(account);
		}
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
	protected List<OpenPosition> getOverallPosition(Account account) {
		List<OpenPosition> result = new ArrayList<OpenPosition>();
		Map<String, List<OpenPosition>> ap = accountPositions.get(account.getId());
		if(null == ap)
			return result;

		for(String symbol: ap.keySet()) {
			OpenPosition pos = getOverallPosition(account, symbol);
			if(!PriceUtils.isZero(pos.getQty()))
				result.add(pos);
		}
		return result;
	}
	
	// this one gives the overall positions for a specific account and symbol
	public OpenPosition getOverallPosition(Account account, String symbol) {
		OpenPosition result = new OpenPosition(account.getUserId(), account.getId(), symbol, 0, 0);
		Map<String, List<OpenPosition>> ap = accountPositions.get(account.getId());
		if(null == ap)
			return result;
		
		List<OpenPosition> list = ap.get(symbol);
		if(null == list)
			return result;
		
		OpenPosition pos = null;
		try {
			pos = getOverallPosition(list);
		} catch (PositionException e) {
			log.error(e.getMessage(), e);
		}
		
		return null == pos? result : pos;
	}
	
	protected OpenPosition getOverallPosition(List<OpenPosition> list) throws PositionException {
		if(null == list || list.size() <= 0)
			return null;
		
		checkOverallPosition(list);
		double qty = 0;
		double amount = 0;
		double PnL = 0;
		for(OpenPosition pos: list) {
			qty += pos.getQty();
			amount += pos.getQty() * pos.getPrice();
			PnL += pos.getPnL();
		}
		double price = amount / qty;
		OpenPosition result = new OpenPosition(list.get(0).getUser(), list.get(0).getAccount(), 
				list.get(0).getSymbol(), qty, price);
		result.setPnL(PnL);
		return result;
	}
	
	private void checkOverallPosition(List<OpenPosition> list) throws PositionException {
		if(null == list || list.size() < 2)
			return;
		
		OpenPosition first = list.get(0);
		for(OpenPosition pos: list) {
			if(!pos.getAccount().equals(first.getAccount()))
				throw new PositionException("Position list contains different account: " + 
							first + " vs " + pos);
			
			if(!pos.getSymbol().equals(first.getSymbol()))
				throw new PositionException("Position list contains different symbol: " + 
							first + " vs " + pos);
			
			if(first.oppositePosition(pos))
				throw new PositionException("Position list contains different side: " + 
							first + " vs " + pos);
			
			if(PriceUtils.isZero(pos.getQty()))
				throw new PositionException("Position list contains 0: " + pos); 
		}
	}
	
	public void updateDynamicData(List<Account> accounts) {
		for(Account account: accounts) {
			updateAccountDynamicData(account);
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

	private void updateAccountDynamicData(Account account) {
		double accountUrPnL = 0;
		double accountMargin = account.getCash() * Default.getMarginTimes();
		if(null == quoteFeeder)
			return;

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

				Map<String, List<OpenPosition>> symbolPositions = accountPositions.get(account.getId());
				if(null != symbolPositions) {
					List<OpenPosition> list = symbolPositions.get(symbol);
					if(null != list) {
						for(OpenPosition position: list) {
							double price = getMarketablePrice(quote, position.getQty());
							position.setPnL((price-position.getPrice())*position.getQty());
						}
						
						OpenPosition overallPosition = getOverallPosition(account, symbol);
						double price = getMarketablePrice(quote, overallPosition.getQty());
						overallPosition.setPnL((price-overallPosition.getPrice())*overallPosition.getQty());
						double urPnL = FxUtils.convertPnLToCurrency(refDataManager, fxConverter, account.getCurrency(), 
								quote.getSymbol(), overallPosition.getPnL());
						accountUrPnL += urPnL;
						accountMargin += urPnL;
						notifyOpenPositionUrPnLUpdate(overallPosition);
					}
				}
				
				double marginValue = getMarginValueByAccountAndSymbol(account, symbol, quote);
				accountMargin -= marginValue;
			}

			account.setUrPnL(accountUrPnL);
			account.setMargin(accountMargin);
		}
		notifyAccountDynamicUpdate(account);
	}
	
	private double getMarginQtyByAccountAndSymbol(Account account, String symbol, double extraQty) {
		double buyQty = 0;
		double sellQty = 0;

		synchronized(getSyncAccount(account.getId())) {
			OpenPosition overallPosition = getOverallPosition(account, symbol);
			double positionQty = overallPosition.getQty();
			
			Map<String, Map<String, ParentOrder>> accountMap = parentOrders.get(account.getId());
			if (null != accountMap) {
				Map<String, ParentOrder> symbolMap = accountMap.get(symbol);
				if(null != symbolMap) {
					for(ParentOrder order: symbolMap.values()) {
						if(order.getSide().isBuy()) {
							buyQty += order.getQuantity() - order.getCumQty();
						} else {
							sellQty += order.getQuantity() - order.getCumQty();
						}
					}
				}
			}
			buyQty += positionQty;
			sellQty -= positionQty;
		}
		
		if(extraQty > 0)
			buyQty += extraQty;
		else
			sellQty -= extraQty;

		// take the maximum one
		return buyQty > sellQty? buyQty:-sellQty;
	}
	
	private double getMarginValueByAccountAndSymbol(Account account, String symbol, Quote quote) {
		double marginQty = getMarginQtyByAccountAndSymbol(account, symbol, 0);
		double price = getMarketablePrice(quote, marginQty);
		return Math.abs(FxUtils.convertPositionToCurrency(refDataManager, fxConverter, account.getCurrency(), quote.getSymbol(), 
				marginQty, price));
	}
	
	public boolean checkMarginDeltaByAccountAndSymbol(Account account, String symbol, Quote quote, double extraQty) {
		double currentMarginQty = getMarginQtyByAccountAndSymbol(account, symbol, 0);
		double futureMarginQty = getMarginQtyByAccountAndSymbol(account, symbol, extraQty);
		if(Math.abs(currentMarginQty) >= Math.abs(futureMarginQty))
			return true; // reducing qty, ok
		
		double deltaQty = Math.abs(futureMarginQty) - Math.abs(currentMarginQty);
		if(futureMarginQty < 0) {
			deltaQty = -deltaQty;
		}

		double price = getMarketablePrice(quote, deltaQty);
		double deltaValue = Math.abs(FxUtils.convertPositionToCurrency(refDataManager, fxConverter,
				account.getCurrency(), quote.getSymbol(), 
				deltaQty, price));
		return account.getMargin() - deltaValue >= 0;
	}
	
	public List<Execution> getExecutions(String account) {
		List<Execution> list = new ArrayList<Execution>();
		List<Execution> exes = executions.get(account);
		if(null != exes)
			list.addAll(exes);
		return list;
	}
	
	private double getMarketablePrice(Quote quote, double qty) {
		return PriceUtils.GreaterThan(qty, 0)?quote.getBid():quote.getAsk();
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
				symbolPositions = new HashMap<String, List<OpenPosition>>();
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
	
	public void rollAccount(Account account) {
		synchronized(getSyncAccount(account.getId())) {
			account.updateEndOfDay();
		}
	}
	
}
