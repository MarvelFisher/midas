/**
 * 
 */
package com.cyanspring.cstw.ui.rw.forms;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.cyanspring.cstw.model.admin.InstrumentInfoModel;
import com.cyanspring.cstw.ui.basic.BasicTableComposite;
import com.cyanspring.cstw.ui.basic.DefaultLabelProviderAdapter;
import com.cyanspring.cstw.ui.common.TableType;
import com.cyanspring.cstw.ui.utils.LTWStringUtils;

/**
 * @author marve_000
 *
 */
public class AssignInstrumentInfoTableComposite extends BasicTableComposite {

	public AssignInstrumentInfoTableComposite(Composite parent, int style) {
		super(parent, style, TableType.RWAssignedInstrument);
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		return new AssignInstrumentInfoTableLabelProvider();
	}

}

class AssignInstrumentInfoTableLabelProvider extends DefaultLabelProviderAdapter {

	@Override
	public String getColumnText(Object element, int columnIndex) {
		InstrumentInfoModel model = (InstrumentInfoModel) element;
		switch (columnIndex) {
		case 0:
			return model.getSymbolId();
		case 1:
			return model.getSymbolName();
		case 2:
			return LTWStringUtils.doubleToString(model.getStockQuanity());	
		default:
			return "";
		}
	}
	
}

