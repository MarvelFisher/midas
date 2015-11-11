package com.cyanspring.common.client;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public interface IInstrumentPoolKeeper {

	/**
	 * 根据交易员账号和输入的股票，返回其对应的ExchangeSubAccountId和股票池信息List<InstrumentPoolRecord>
	 * 
	 * @param account
	 * @param symbol
	 * @return
	 */
	Map<String, List<InstrumentPoolRecord>> getSubAccountInstrumentPoolRecordMap(
			Account account, String symbol);

	/**
	 * 更新股票池数量InstrumentPoolRecord
	 * 
	 * @param instrumentPoolRecord
	 */
	void update(InstrumentPoolRecord instrumentPoolRecord);

	/**
	 * 根据InstrumentPoolId和Symbol获取对应股票数量的InstrumentPoolRecord
	 * 
	 * @param instrumentPoolId
	 * @param symbol
	 * @return
	 */
	InstrumentPoolRecord getInstrumentPoolRecord(String instrumentPoolId,
			String symbol);

}
