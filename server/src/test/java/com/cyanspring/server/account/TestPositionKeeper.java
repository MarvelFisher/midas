package com.cyanspring.server.account;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.Default;
import com.cyanspring.common.account.Account;
import com.cyanspring.common.account.AccountKeeper;
import com.cyanspring.common.account.ClosedPosition;
import com.cyanspring.common.account.OpenPosition;
import com.cyanspring.common.account.PositionException;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderException;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.position.PositionKeeper;
import com.cyanspring.common.staticdata.IRefDataManager;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.TimeUtil;

public class TestPositionKeeper {

	private static final Logger log = LoggerFactory
			.getLogger(TestPositionKeeper.class);

	private static final double epsilon = 0.00000001;
	int count = 1;
	PositionKeeper keeper;
	Account account = new Account(Default.getAccount(), Default.getUser());

	private Execution createExecution(OrderSide side, double qty, double price) {
		Execution exe = null;
		Date tradeDate = Calendar.getInstance().getTime();

		try {
			exe = new Execution("AUDUSD", side, qty, price, "orderId",
					"parentOrderId", "strategyId", "EXEC-" + count++,
					Default.getUser(), Default.getAccount(), null, tradeDate);
		} catch (OrderException e) {
			log.error(e.getMessage(), e);
			;
		}

		return exe;
	}

	@Before
	public void before() {
		keeper = new PositionKeeper();
		keeper.accountKeeper = new AccountKeeper();
		keeper.refDataManager = new testRefDataManager();
		keeper.leverageManager = new LeverageManager();
		keeper.commissionManager = new CommissionManager();
	}

	@Test
	public void testGetOverallPosition() throws Exception {
		List<OpenPosition> openPositions = keeper.getOpenPositions(Default
				.getAccount());
		assertEquals(null, openPositions);
		Execution execution = createExecution(OrderSide.Buy, 2000, 0.8);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOverallPosition(account);
		assertEquals(2000.0, openPositions.get(0).getAvailableQty(), epsilon);

		Field field = Default.class.getDeclaredField("settlementDays");
		field.setAccessible(true);
		field.set(null, 1);

		openPositions = keeper.getOverallPosition(account);
		assertEquals(0.0, openPositions.get(0).getAvailableQty(), epsilon);

		Clock.getInstance().setMode(Clock.Mode.MANUAL);
		Clock.getInstance().setManualClock(TimeUtil.getNextDay(new Date()));
		log.debug(Clock.getInstance().now().toString());

		openPositions = keeper.getOverallPosition(account);
		log.debug(openPositions.get(0).toString());
		assertEquals(2000.0, openPositions.get(0).getAvailableQty(), epsilon);
	}

	@Test
	public void testBuyPosition() throws PositionException {
		List<OpenPosition> openPositions = keeper.getOpenPositions(Default
				.getAccount());
		assertEquals(null, openPositions);
		Execution execution = createExecution(OrderSide.Buy, 2000, 0.8);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(2000.0, openPositions.get(0).getQty(), epsilon);
		assertEquals(0.8, openPositions.get(0).getPrice(), epsilon);
		log.debug(openPositions.get(0).toString());

		execution = createExecution(OrderSide.Buy, 5000, 1.6);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(5000.0, openPositions.get(1).getQty(), epsilon);
		assertEquals(1.6, openPositions.get(1).getPrice(), epsilon);
		log.debug(openPositions.get(1).toString());

		execution = createExecution(OrderSide.Buy, 4000, 1.2);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(4000.0, openPositions.get(2).getQty(), epsilon);
		assertEquals(1.2, openPositions.get(2).getPrice(), epsilon);
		log.debug(openPositions.get(2).toString());

		OpenPosition overallPosition = keeper.getOverallPosition(account,
				"AUDUSD");
		assertEquals(2000.0 + 5000.0 + 4000.0, overallPosition.getQty(),
				epsilon);
		double price = (2000.0 * 0.8 + 5000.0 * 1.6 + 4000.0 * 1.2)
				/ (2000.0 + 5000.0 + 4000.0);
		assertEquals(price, overallPosition.getPrice(), epsilon);
		log.debug("Overall position: " + overallPosition);

		// check nothing in closed positions
		List<ClosedPosition> closedPositions = keeper
				.getClosedPositions(Default.getAccount());
		assertEquals(0, closedPositions.size());

		// close some positions
		execution = createExecution(OrderSide.Sell, 4000, 1.4);
		keeper.processExecution(execution, account);

		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(3000.0, openPositions.get(0).getQty(), epsilon);
		assertEquals(1.6, openPositions.get(0).getPrice(), epsilon);
		log.debug(openPositions.get(0).toString());

		assertEquals(4000.0, openPositions.get(1).getQty(), epsilon);
		assertEquals(1.2, openPositions.get(1).getPrice(), epsilon);
		log.debug(openPositions.get(1).toString());

		// check closed positions now
		closedPositions = keeper.getClosedPositions(Default.getAccount());
		assertEquals(2000.0, closedPositions.get(0).getQty(), epsilon);
		assertEquals(0.8, closedPositions.get(0).getBuyPrice(), epsilon);
		assertEquals(1.4, closedPositions.get(0).getSellPrice(), epsilon);
		assertEquals((1.4 - 0.8) * 2000, closedPositions.get(0).getPnL(),
				epsilon);
		log.debug(closedPositions.get(0).toString());

		assertEquals(2000.0, closedPositions.get(1).getQty(), epsilon);
		assertEquals(1.6, closedPositions.get(1).getBuyPrice(), epsilon);
		assertEquals(1.4, closedPositions.get(1).getSellPrice(), epsilon);
		assertEquals((1.4 - 1.6) * 2000, closedPositions.get(1).getPnL(),
				epsilon);
		log.debug(closedPositions.get(1).toString());

		// close more positions
		execution = createExecution(OrderSide.Sell, 10000, 1.0);
		keeper.processExecution(execution, account);

		// check open positions
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(-3000.0, openPositions.get(0).getQty(), epsilon);
		assertEquals(1, openPositions.get(0).getPrice(), epsilon);
		log.debug(openPositions.get(0).toString());

		// check closed positions
		closedPositions = keeper.getClosedPositions(Default.getAccount());
		assertEquals(3000.0, closedPositions.get(2).getQty(), epsilon);
		assertEquals(1.6, closedPositions.get(2).getBuyPrice(), epsilon);
		assertEquals(1.0, closedPositions.get(2).getSellPrice(), epsilon);
		assertEquals((1.0 - 1.6) * 3000, closedPositions.get(2).getPnL(),
				epsilon);
		log.debug(closedPositions.get(2).toString());

		assertEquals(4000.0, closedPositions.get(3).getQty(), epsilon);
		assertEquals(1.2, closedPositions.get(3).getBuyPrice(), epsilon);
		assertEquals(1.0, closedPositions.get(3).getSellPrice(), epsilon);
		assertEquals((1.0 - 1.2) * 4000, closedPositions.get(3).getPnL(),
				epsilon);
		log.debug(closedPositions.get(3).toString());

		// close the last position
		execution = createExecution(OrderSide.Buy, 3000, 1.3);
		keeper.processExecution(execution, account);

		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(0, openPositions.size());

		// check closed positions
		closedPositions = keeper.getClosedPositions(Default.getAccount());
		assertEquals(3000.0, closedPositions.get(4).getQty(), epsilon);
		assertEquals(1.3, closedPositions.get(4).getBuyPrice(), epsilon);
		assertEquals(1.0, closedPositions.get(4).getSellPrice(), epsilon);
		assertEquals((1.0 - 1.3) * 3000, closedPositions.get(4).getPnL(),
				epsilon);
		log.debug(closedPositions.get(4).toString());

	}

	@Test
	public void testSellPosition() throws PositionException {
		List<OpenPosition> openPositions = keeper.getOpenPositions(Default
				.getAccount());
		assertEquals(null, openPositions);
		Execution execution = createExecution(OrderSide.Sell, 2000, 0.8);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(-2000.0, openPositions.get(0).getQty(), epsilon);
		assertEquals(0.8, openPositions.get(0).getPrice(), epsilon);
		log.debug(openPositions.get(0).toString());

		execution = createExecution(OrderSide.Sell, 5000, 1.6);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(-5000.0, openPositions.get(1).getQty(), epsilon);
		assertEquals(1.6, openPositions.get(1).getPrice(), epsilon);
		log.debug(openPositions.get(1).toString());

		execution = createExecution(OrderSide.Sell, 4000, 1.2);
		keeper.processExecution(execution, account);
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(-4000.0, openPositions.get(2).getQty(), epsilon);
		assertEquals(1.2, openPositions.get(2).getPrice(), epsilon);
		log.debug(openPositions.get(2).toString());

		OpenPosition overallPosition = keeper.getOverallPosition(account,
				"AUDUSD");
		assertEquals(-2000.0 - 5000.0 - 4000.0, overallPosition.getQty(),
				epsilon);
		double price = (-2000.0 * 0.8 + -5000.0 * 1.6 + -4000.0 * 1.2)
				/ (-2000.0 - 5000.0 - 4000.0);
		assertEquals(price, overallPosition.getPrice(), epsilon);
		log.debug("Overall position: " + overallPosition);

		// check nothing in closed positions
		List<ClosedPosition> closedPositions = keeper
				.getClosedPositions(Default.getAccount());
		assertEquals(0, closedPositions.size());

		// close some positions
		execution = createExecution(OrderSide.Buy, 4000, 1.4);
		keeper.processExecution(execution, account);

		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(-3000.0, openPositions.get(0).getQty(), epsilon);
		assertEquals(1.6, openPositions.get(0).getPrice(), epsilon);
		log.debug(openPositions.get(0).toString());

		assertEquals(-4000.0, openPositions.get(1).getQty(), epsilon);
		assertEquals(1.2, openPositions.get(1).getPrice(), epsilon);
		log.debug(openPositions.get(1).toString());

		// check closed positions now
		closedPositions = keeper.getClosedPositions(Default.getAccount());
		assertEquals(2000.0, closedPositions.get(0).getQty(), epsilon);
		assertEquals(1.4, closedPositions.get(0).getBuyPrice(), epsilon);
		assertEquals(0.8, closedPositions.get(0).getSellPrice(), epsilon);
		assertEquals((0.8 - 1.4) * 2000, closedPositions.get(0).getPnL(),
				epsilon);
		log.debug(closedPositions.get(0).toString());

		assertEquals(2000.0, closedPositions.get(1).getQty(), epsilon);
		assertEquals(1.4, closedPositions.get(1).getBuyPrice(), epsilon);
		assertEquals(1.6, closedPositions.get(1).getSellPrice(), epsilon);
		assertEquals((1.6 - 1.4) * 2000, closedPositions.get(1).getPnL(),
				epsilon);
		log.debug(closedPositions.get(1).toString());

		// close more positions
		execution = createExecution(OrderSide.Buy, 10000, 1.0);
		keeper.processExecution(execution, account);

		// check open positions
		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(3000.0, openPositions.get(0).getQty(), epsilon);
		assertEquals(1, openPositions.get(0).getPrice(), epsilon);
		log.debug(openPositions.get(0).toString());

		// check closed positions
		closedPositions = keeper.getClosedPositions(Default.getAccount());
		assertEquals(3000.0, closedPositions.get(2).getQty(), epsilon);
		assertEquals(1.0, closedPositions.get(2).getBuyPrice(), epsilon);
		assertEquals(1.6, closedPositions.get(2).getSellPrice(), epsilon);
		assertEquals((1.6 - 1.0) * 3000, closedPositions.get(2).getPnL(),
				epsilon);
		log.debug(closedPositions.get(2).toString());

		assertEquals(4000.0, closedPositions.get(3).getQty(), epsilon);
		assertEquals(1.0, closedPositions.get(3).getBuyPrice(), epsilon);
		assertEquals(1.2, closedPositions.get(3).getSellPrice(), epsilon);
		assertEquals((1.2 - 1.0) * 4000, closedPositions.get(3).getPnL(),
				epsilon);
		log.debug(closedPositions.get(3).toString());

		// close the last position
		execution = createExecution(OrderSide.Sell, 3000, 1.3);
		keeper.processExecution(execution, account);

		openPositions = keeper.getOpenPositions(Default.getAccount());
		assertEquals(0, openPositions.size());

		// check closed positions
		closedPositions = keeper.getClosedPositions(Default.getAccount());
		assertEquals(3000.0, closedPositions.get(4).getQty(), epsilon);
		assertEquals(1.0, closedPositions.get(4).getBuyPrice(), epsilon);
		assertEquals(1.3, closedPositions.get(4).getSellPrice(), epsilon);
		assertEquals((1.3 - 1.0) * 3000, closedPositions.get(4).getPnL(),
				epsilon);
		log.debug(closedPositions.get(4).toString());
	}

	private class testRefDataManager implements IRefDataManager {

		RefData refData;

		@Override
		public void init() throws Exception {
			refData = new RefData();
			refData.set("AUDUSD", RefDataField.SYMBOL.value());
			refData.set(0.2, RefDataField.COMMISSION_FEE.value());
			refData.set(40, RefDataField.MARGIN_RATE.value());
		}

		@Override
		public List<RefData> updateAll(String tradeDate) throws Exception {
			return new ArrayList<>();
		}

		@Override
		public RefData getRefData(String symbol) {
			if (symbol.equals("AUDUSD")) {
				return refData;
			}
			return null;
		}

		@Override
		public String getRefDataFile() {
			return null;
		}

		@Override
		public List<RefData> getRefDataList() {
			return null;
		}

		@Override
		public String getMarket() {
			return null;
		}

		@Override
		public void setRefDataFile(String refDataFile) {
		}

		@Override
		public void clearRefData() {

		}

		@Override
		public List<RefData> update(String index, String tradeDate)
				throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(RefData refData) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setQuoteFile(String quoteFile) {
			// TODO Auto-generated method stub

		}

		@Override
		public void saveRefDataToFile() {
			// TODO Auto-generated method stub

		}
	}

}
