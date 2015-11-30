package com.cyanspring.cstw.ui.rw.forms;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.cyanspring.cstw.model.admin.InstrumentPoolModel;
import com.cyanspring.cstw.model.admin.SubAccountModel;

/**
 * @author Junfeng
 * @create 24 Nov 2015
 */
public class EditTreeLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element) {
		if (element instanceof SubAccountModel) {
			return ((SubAccountModel)element).getName();
		}
		if (element instanceof InstrumentPoolModel) {
			return ((InstrumentPoolModel)element).getName();
		}
		
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof SubAccountModel) {
			return Activator.getDefault().getImageRegistry()
					.getDescriptor(ImageID.SUBACCOUNT_ICON.toString())
					.createImage();
		}
		if (element instanceof InstrumentPoolModel) {
			return Activator.getDefault().getImageRegistry()
					.getDescriptor(ImageID.SUBPOOL_ICON.toString())
					.createImage();
		}

		return super.getImage(element);
	}
	
}
