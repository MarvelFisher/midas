package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.ui.basic.BasicComposite;

/**
 * @author Junfeng
 * @create 22 Oct 2015
 */
public class RWInstrumentSummaryComposite extends BasicComposite {

	public RWInstrumentSummaryComposite(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.cyanspring.cstw.ui.basic.BasicComposite#processByType(com.cyanspring.cstw.service.common.RefreshEventType)
	 */
	@Override
	protected void processByType(RefreshEventType type) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.cyanspring.cstw.ui.basic.BasicComposite#createService()
	 */
	@Override
	protected IBasicService createService() {
		// TODO Auto-generated method stub
		return null;
	}

}
