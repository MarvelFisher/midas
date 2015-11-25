package com.cyanspring.cstw.ui.rw.composite;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.rw.forms.SubPoolManageMasterDetailBlock;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public class SubPoolManageComposite extends BasicComposite {
	
	private ISubPoolManageService service;
	
	private IManagedForm managedForm;
	private SubPoolManageMasterDetailBlock masterDetailBlock;
	
	public SubPoolManageComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
	}

	private void initComponent() {
		setLayout(new FillLayout());
		managedForm = new ManagedForm(this);
		masterDetailBlock = new SubPoolManageMasterDetailBlock(service);
		masterDetailBlock.createContent(managedForm);
	}

	@Override
	protected void processByType(RefreshEventType type) {

	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createSubPoolManageService();
		return service;
	}

}
