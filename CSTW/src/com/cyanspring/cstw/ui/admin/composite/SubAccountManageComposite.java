package com.cyanspring.cstw.ui.admin.composite;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;

import com.cyanspring.cstw.service.common.RefreshEventType;
import com.cyanspring.cstw.service.iservice.IBasicService;
import com.cyanspring.cstw.ui.basic.BasicComposite;
import com.cyanspring.cstw.ui.forms.SubAccountManageMasterDetailBlock;

/**
 * @author Junfeng
 * @create 10 Nov 2015
 */
public class SubAccountManageComposite extends BasicComposite {
	
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
		masterDetailBlock = new SubAccountManageMasterDetailBlock();
		masterDetailBlock.createContent(managedForm);
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