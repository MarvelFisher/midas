package com.cyanspring.common.client;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.account.Account;
import com.cyanspring.common.pool.ExchangeAccount;
import com.cyanspring.common.pool.ExchangeSubAccount;
import com.cyanspring.common.pool.InstrumentPool;
import com.cyanspring.common.pool.InstrumentPoolRecord;

/**
 * @author GuoWei
 * @since 11/09/2015
 */
public interface IInstrumentPoolKeeper {

	/**
	 * 根据不同参数类型查询InstrumentPoolRecord List
	 */
	public enum ModelType {
		EXCHANGE_ACCOUNT, EXCHANGE_SUB_ACCOUNT, INSTRUMENT_POOL
	}

	/**
	 * 根据券商账号ID获取券商账号
	 * 
	 * @return
	 */
	ExchangeAccount getExchangeAccount(String exchangeAccountId);

	/**
	 * 获取所有的券商账号列表
	 * 
	 * @return
	 */
	List<ExchangeAccount> getExchangeAccountList();

	/**
	 * 获取给定券商账号对应的所有交易分账号
	 * 
	 * @param exchangeAccount
	 * @return
	 */
	List<ExchangeSubAccount> getExchangeSubAccountList(String exchangeAccount);

	/**
	 * 获取给定交易分账号对应的所有股票池
	 * 
	 * @param exchangeSubAccount
	 * @return
	 */
	List<InstrumentPool> getInstrumentPoolList(String exchangeSubAccount);

	/**
	 * 获取给定股票池对应的所有股票信息
	 * 
	 * @param id
	 *            - ExchangeAccountId/ExchangeSubAccountId/InstrumentPoolId
	 * @param ModelType
	 * @return
	 */
	List<InstrumentPoolRecord> getInstrumentPoolRecordList(String id,
			ModelType type);

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

	/**
	 * 获取ExchangeSubAccount关联的User Id list(包含RiskManager & Group)
	 * 
	 * @param subAccount
	 *            id
	 * @return User Id list
	 */
	List<String> getAssignedAdminsBySubAccount(String subAccount);

	/**
	 * 获取系统所有的的ExchangeSubAccount List
	 * 
	 * @return
	 */
	List<ExchangeSubAccount> getAllSubAccountList();

	/**
	 * 根据ExchangeSubAccount id获取对应的ExchangeSubAccount
	 * 
	 * @param subAccountId
	 * @return
	 */
	ExchangeSubAccount getSubAccountById(String subAccountId);

	/**
	 * 获取风控所能管理的ExchangeSubAccount List
	 * 
	 * @param user
	 * @return
	 */
	List<ExchangeSubAccount> getAssignedSubAccounts(String user);
}
