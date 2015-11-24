package com.cyanspring.cstw.ui.admin.forms;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.service.iservice.admin.ISubAccountManagerService;

/**
 * @author Junfeng
 * @create 9 Nov 2015
 */
public class EditTreeContentProvider implements ITreeContentProvider {
	
	private ISubAccountManagerService service;
	
	public EditTreeContentProvider(ISubAccountManagerService service) {
		this.service = service;
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List) inputElement).toArray();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ExchangeAccountModel) {
			ExchangeAccountModel parent = (ExchangeAccountModel) parentElement;
			return service.getSubAccountListByExchangeAccountId(parent.getId()).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ExchangeAccountModel) {
			ExchangeAccountModel parent = (ExchangeAccountModel) element;
			return !service.getSubAccountListByExchangeAccountId(parent.getId()).isEmpty();
		}
		return false;
	}

}
