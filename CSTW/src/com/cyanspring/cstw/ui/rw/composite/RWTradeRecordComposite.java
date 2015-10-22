package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.ui.basic.BasicComposite;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWTradeRecordComposite extends BasicComposite {

	public RWTradeRecordComposite(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	protected void processByType(RefreshEventType type) {
		// TODO Auto-generated method stub

	}

	@Override
	protected IBasicService createService() {
		// TODO Auto-generated method stub
		return null;
	}

}
