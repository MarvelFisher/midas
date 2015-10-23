/**
 * 
 */
package com.cyanspring.cstw.service.iservice.riskmgr;

import java.util.List;

import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.model.riskmgr.RCOrderRecordModel;

/**
 * @author Yu-Junfeng
 * @create 30 Jul 2015
 */
public interface IOrderRecordService extends IBasicService {
	void queryOrder();

	void cancelOrder(String orderId);

	List<RCOrderRecordModel> getActivityOrderList();

	List<RCOrderRecordModel> getPendingOrderList();

}
