package com.cyanspring.id;

import io.netty.util.internal.StringUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.cyanspring.id.Library.Util.DateUtil;
import com.cyanspring.id.Library.Util.FixStringBuilder;

public class TickItem {

	private final ReentrantReadWriteLock readWriteLockList = new ReentrantReadWriteLock();
	private final Lock writeLockList = readWriteLockList.writeLock();

	Date time;
	double price = 0, bid = 0, ask = 0;

	/**
	 * 
	 */
	TickItem() {
		clear();
	}

	/**
	 * 
	 */
	void reset() {
		writeLockList.lock();

		// m_arrTokens.clear();

		writeLockList.unlock();
	}

	/**
	 * 
	 */
	void clear() {
		time = new Date(0);
		price = 0;
		bid = 0;
		ask = 0.0;
		reset();
	}

	/**
	 * 
	 * @param nField
	 * @param tValue
	 */
	void setValue(int nField, Date tValue) {
		writeLockList.lock();
		switch (nField) {
		case FieldID.LastTradeTime: {	
			time = tValue;
			//Calendar cal = Calendar.getInstance();
			//cal.setTime(time);
		}
			break;
		default:
			break;
		}
		writeLockList.unlock();
	}

	/**
	 * 
	 * @param nField
	 * @param dValue
	 */
	void setValue(int nField, double dValue) {
		writeLockList.lock();

		switch (nField) {
		case FieldID.AskPrice: {
			if (ask != dValue) {
				ask = dValue;
			}
		}
			break;
		case FieldID.BidPrice: {
			if (bid != dValue) {
				bid = dValue;
			}
		}
			break;
		case FieldID.CurrentPrice: {
			price = dValue;
		}
			break;
		default:
			break;
		}
		writeLockList.unlock();
	}

	/**
	 * 
	 * @param nDP
	 * @return
	 */
	String toString(int nDP) {
		FixStringBuilder tick = new FixStringBuilder('=', '|');

		tick.append("Time");
		tick.append(time);
		tick.append("Bid");
		tick.append(bid, nDP);
		tick.append("Ask");
		tick.append(ask, nDP);
		tick.append("Price");
		tick.append(price, nDP);

		return tick.toString();
	}

	/**
	 * 
	 * @param strLine
	 * @return
	 * @throws ParseException
	 */
	boolean loadFromFile(String strLine) throws ParseException {
		clear();
		String[] vec = StringUtil.split(strLine, '|');

		for (int i = 0; i < vec.length; i++) {
			String[] vecTokens = StringUtil.split(vec[i], '=');
			if (vecTokens.length != 2)
				return false;

			switch (vecTokens[0]) {
			case "Time": {
				time = DateUtil.parseDate(vecTokens[1],
						"yyyy-MM-dd HH:mm:ss");
			}
				break;
			case "Bid": {
				bid = Double.parseDouble(vecTokens[1]);
			}
				break;
			case "Ask": {
				ask = Double.parseDouble(vecTokens[1]);
			}
				break;
			case "Price": {
				price = Double.parseDouble(vecTokens[1]);
			}
				break;

			default:
				break;
			}
		}
		return true;
	}
}
