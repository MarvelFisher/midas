package com.cyanspring.cstw.service.iservice.trader;

import com.cyanspring.cstw.common.CustomOrderType;
import com.cyanspring.cstw.model.trader.ParentOrderModel;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/11/17
 *
 */
public interface IParentOrderService {

	void quickEnterOrder(ParentOrderModel model, CustomOrderType type);

}
