package com.cyanspring.cstw.ui.forms;

import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.model.admin.AssignedModel;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;

/**
 * @author Junfeng
 * @create 16 Nov 2015
 */
public class AssignedTableComposite extends BasicTableComposite {

	public AssignedTableComposite(Composite parent, int style) {
		super(parent, style, TableType.AssignedInfo);
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignedTableLabelProvider(tableViewer);
	}

}

class AssignedTableLabelProvider extends DefaultLabelProviderAdapter {

	private TableViewer viewer;
	
	public AssignedTableLabelProvider(TableViewer tableViewer) {
		this.viewer = tableViewer;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		
		AssignedModel model = (AssignedModel) element;
		switch (columnIndex) {
		case 0:
			if (viewer != null) {
				List<?> input = (List<?>) viewer.getInput();
				int index = input.indexOf(element);
				return "" + (index + 1);
			}
			return "";
		case 1:
			return model.getUserId();
		case 2:
			return model.getRoleType();
		default:
			return "";		
		}
	}
	
}
