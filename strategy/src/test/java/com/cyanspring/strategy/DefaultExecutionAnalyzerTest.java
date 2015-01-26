/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.strategy;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.cyanspring.common.Default;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.ExecutionInstruction;
import com.cyanspring.common.strategy.IExecutionManager;
import com.cyanspring.common.strategy.IStrategyContainer;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderAction;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.strategy.DefaultExecutionAnalyzer;

public class DefaultExecutionAnalyzerTest {

	class TestStrategy implements IStrategy {
		private String id = IdGenerator.getInstance().getNextID() + "S";
		private Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();

		@Override
		public ChildOrder createChildOrder(String parentId, String symbol,
				OrderSide side, double quantity, double price,
				ExchangeOrderType type) {
			return new ChildOrder(symbol, side, quantity, price, type, parentId, getId(), Default.getUser(), Default.getAccount(), null);
		}

		@Override
		public ChildOrder addChildOrder(ChildOrder order) {
			return childOrders.put(order.getId(), order);
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getAccount() {
			return Default.getAccount();
		}
		
		@Override
		public Collection<ChildOrder> getChildOrders() {
			return new ArrayList<ChildOrder>(childOrders.values());
		}

		@Override
		public List<FieldDef> getCommonFieldDefs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, FieldDef> getCombinedFieldDefs()
				throws StrategyException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void logDebug(String message) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void logInfo(String message) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void logWarn(String message) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void logError(String message) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<ChildOrder> getOpenChildOrdersByParent(String parent) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<ChildOrder> getSortedOpenChildOrdersByParent(String parent) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<ChildOrder> getSortedOpenChildOrdersBySymbol(String symbol) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChildOrder getChildOrder(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChildOrder removeChildOrder(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int childOrderCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void start() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void terminate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void uninit() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void execute(ExecuteTiming timing) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Date getStartTime() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Date getEndTime() {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public void create(Object... objects) throws StrategyException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IStrategyContainer getContainer() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IDownStreamSender getSender() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IAsyncEventInbox getInbox() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IExecutionManager getExecutionManager() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCheckAdjQuote() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setCheckAdjQuote(boolean checkAdjQuote) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getStrategyName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<FieldDef> getStrategyFieldDefs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RemoteAsyncEvent createConfigUpdateEvent()
				throws StrategyException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setSender(IDownStreamSender sender) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContainer(IStrategyContainer container) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isValidateQuote() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setValidateQuote(boolean validateQuote) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public DataObject getDataObject() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isQuoteRequired() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setQuoteRequired(boolean checkQuote) {
			// TODO Auto-generated method stub
			
		}


	}
	
	@Test
	public void testNoInstruction() {
		TestStrategy strategy = new TestStrategy();
		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 0);
	}

	@Test
	public void testRemoveAllChildOrders() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				2000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child);
		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 1);
		ExecutionInstruction ei = eis.get(0);
		assertTrue(ei.getAction().equals(OrderAction.CANCEL));
		assertTrue(ei.getOrder().equals(child));
	}
	
	@Test
	public void testNewOrder() {
		TestStrategy strategy = new TestStrategy();
		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Sell, 68.3, 4000, ExchangeOrderType.IOC, strategy.getId());
		pi.add(pa);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 1);
		ExecutionInstruction ei = eis.get(0);
		assertTrue(ei.getAction().equals(OrderAction.NEW));
		assertTrue(ei.getOrder().getParentOrderId().equals(pa.getParentId()));
		assertTrue(ei.getOrder().getPrice() == pa.getPrice());
		assertTrue(ei.getOrder().getQuantity() == pa.getQty());
		assertTrue(ei.getOrder().getType() == pa.getOrderType());
	}
	
	@Test
	public void testQuantityUp() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				2000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child);

		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Buy, 68.3, 6000, ExchangeOrderType.LIMIT, "001P");
		pi.add(pa);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 1);
		ExecutionInstruction ei = eis.get(0);
		assertTrue(ei.getAction().equals(OrderAction.NEW));
		assertTrue(ei.getOrder().getParentOrderId().equals(pa.getParentId()));
		assertTrue(ei.getOrder().getPrice() == pa.getPrice());
		assertTrue(ei.getOrder().getQuantity() == 4000);
		assertTrue(ei.getOrder().getType() == pa.getOrderType());
	}

	@Test
	public void testQuantityDown() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				8000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child);

		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Buy, 68.3, 2000, ExchangeOrderType.LIMIT, "001P");
		pi.add(pa);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 1);
		ExecutionInstruction ei = eis.get(0);
		assertTrue(ei.getAction().equals(OrderAction.AMEND));
		assertTrue(ei.getOrder().getParentOrderId().equals(pa.getParentId()));
		assertTrue(ei.getOrder().getPrice() == pa.getPrice());
		assertTrue(PriceUtils.Equal((Double)(ei.getChangeFields().get(OrderField.QUANTITY.value())), 2000));
		assertTrue(ei.getOrder().getType() == pa.getOrderType());
	}

	@Test
	public void testMultipleOrderQuantityDown() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child1 = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				2000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child1);
		ChildOrder child4 = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				4000, 68.4, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child4);
		ChildOrder child2 = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				6000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child2);
		ChildOrder child3 = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				8000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child3);

		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Buy, 68.3, 6000, ExchangeOrderType.LIMIT, "001P");
		pi.add(pa);
		PriceAllocation pa2 = new PriceAllocation("0005.HK", OrderSide.Buy, 68.4, 4000, ExchangeOrderType.LIMIT, "001P");
		pi.add(pa2);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 2);
		ExecutionInstruction ei = eis.get(0);
		assertTrue(ei.getAction().equals(OrderAction.AMEND));
		assertTrue(ei.getOrder().equals(child2));
		assertTrue(PriceUtils.Equal((Double)(ei.getChangeFields().get(OrderField.QUANTITY.value())), 4000));
		
		ei = eis.get(1);
		assertTrue(ei.getAction().equals(OrderAction.CANCEL));
		assertTrue(ei.getOrder().equals(child3));
	}

	@Test
	public void testPriceAmend() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				8000, 68.3, ExchangeOrderType.LIMIT);
		strategy.addChildOrder(child);

		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Buy, 68.4, 2000, ExchangeOrderType.LIMIT, "001P");
		pi.add(pa);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 2);
		ExecutionInstruction ei = eis.get(0);
		assertTrue(ei.getAction().equals(OrderAction.CANCEL));
		assertTrue(ei.getOrder().equals(child));
		
		ei = eis.get(1);
		assertTrue(ei.getAction().equals(OrderAction.NEW));
		assertTrue(ei.getOrder().getParentOrderId().equals(pa.getParentId()));
		assertTrue(ei.getOrder().getPrice() == pa.getPrice());
		assertTrue(ei.getOrder().getQuantity() == pa.getQty());
		assertTrue(ei.getOrder().getType() == pa.getOrderType());
	}
	
	@Test
	public void testMarketTypeOrder() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				8000, 68.3, ExchangeOrderType.MARKET);
		strategy.addChildOrder(child);

		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Buy, 68.4, 8000, ExchangeOrderType.MARKET, "001P");
		pi.add(pa);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 0);
	}
	
	@Test
	public void testMarketTypeOrderPartialFilled() {
		TestStrategy strategy = new TestStrategy();
		ChildOrder child = strategy.createChildOrder("001P", "0005.HK", OrderSide.Buy, 
				8000, 68.3, ExchangeOrderType.MARKET);
		child.setCumQty(2000);
		strategy.addChildOrder(child);

		DefaultExecutionAnalyzer analyzer = new DefaultExecutionAnalyzer();
		PriceInstruction pi = new PriceInstruction();
		PriceAllocation pa = new PriceAllocation("0005.HK", OrderSide.Buy, 68.4, 6000, ExchangeOrderType.MARKET, "001P");
		pi.add(pa);
		List<ExecutionInstruction> eis = analyzer.analyze(pi, strategy);
		assertTrue(eis.size() == 0);
	}

}
