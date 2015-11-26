package com.cyanspring.cstw.ui.admin.composite;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.service.iservice.ServiceFactory;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;
import com.cyanspring.cstw.ui.admin.forms.SubAccountManageMasterDetailBlock;
import com.cyanspring.cstw.ui.basic.BasicComposite;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class SubAccountManageComposite extends BasicComposite {
	
	private ISubAccountManagerService service;
	
	private IManagedForm managedForm;
	private SubAccountManageMasterDetailBlock masterDetailBlock;
	
	public SubAccountManageComposite(Composite parent, int style) {
		super(parent, style);
		initComponent();
	}

	private void initComponent() {
		setLayout(new FillLayout());
		managedForm = new ManagedForm(this);
		managedForm.setContainer(this);
		masterDetailBlock = new SubAccountManageMasterDetailBlock(service);
		masterDetailBlock.createContent(managedForm);
	}

	@Override
	protected void processByType(RefreshEventType type) {
		if (RefreshEventType.InstrumentPoolUpdate == type) {
			masterDetailBlock.refresh();
		}
	}

	@Override
	protected IBasicService createService() {
		service = ServiceFactory.createSubAccountManagerService();
		return service;
	}

}
