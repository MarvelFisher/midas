package com.cyanspring.cstw.service.iservice;

import com.cyanspring.cstw.service.common.RefreshEventType;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/10/22
 *	moved from Project S
 */
public interface IUiListener {

	void refreshByType(RefreshEventType type);

	void handleErrorMessage(String errorMessage);
}
