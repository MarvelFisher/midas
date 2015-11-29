package com.cyanspring.cstw.service.impl.riskmgr;

import java.util.List;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.cstw.service.common.BasicServiceImpl;
import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.admin.IInputChangeListener;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public class MockSubPoolManageServiceImpl extends BasicServiceImpl implements
		ISubPoolManageService {

	@Override
	protected List<Class<? extends AsyncEvent>> getReplyEventList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RefreshEventType handleEvent(AsyncEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSubAccountInputChangeListener(
			IInputChangeListener iInputChangeListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<?> getInstrumentInfoModelListBySubAccountId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> getInstrumentInfoModelListBySubPoolId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
