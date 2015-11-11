package com.cyanspring.cstw.ui.forms;

import org.eclipse.jface.viewers.LabelProvider;
import com.cyanspring.cstw.model.admin.ExchangeAccountModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;

/**
 * @author Junfeng
 * @create 9 Nov 2015
 */
public class EditTreeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ExchangeAccountModel) {
			return ((ExchangeAccountModel) element).getName();
		} 
		if (element instanceof SubAccountModel) {
			return ((SubAccountModel) element).getName();
		}
		return super.getText(element);
	}

}
