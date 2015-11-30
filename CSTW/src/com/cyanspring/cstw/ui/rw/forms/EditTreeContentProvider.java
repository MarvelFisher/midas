package com.cyanspring.cstw.ui.rw.forms;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.cyanspring.cstw.model.admin.SubAccountModel;
import com.cyanspring.cstw.service.iservice.riskmgr.ISubPoolManageService;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public class EditTreeContentProvider implements ITreeContentProvider {

	private ISubPoolManageService service;
	
	public EditTreeContentProvider(ISubPoolManageService service) {
		this.service = service;
	}
	
	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List)inputElement).toArray();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof SubAccountModel) {
			SubAccountModel parent = (SubAccountModel) parentElement;
			return service.getSubPoolListByAccountId(parent.getId()).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof SubAccountModel) {
			SubAccountModel parent = (SubAccountModel) element;
			return !service.getSubPoolListByAccountId(parent.getId()).isEmpty();
		}
		return false;
	}

}
