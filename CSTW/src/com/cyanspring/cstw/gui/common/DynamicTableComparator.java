/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.cstw.gui.common;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.util.ReflectionUtil;
import com.cyanspring.cstw.gui.PositionView;

public class DynamicTableComparator extends ViewerComparator {
	private static final Logger log = LoggerFactory
			.getLogger(DynamicTableComparator.class);
	
	private int propertyIndex;
	private boolean descending;

	public DynamicTableComparator() {
		this.propertyIndex = 0;
		descending = true;
	}

	public int getDirection() {
		return descending ? SWT.DOWN : SWT.UP;
	}
	
	public int getColumn(){
		return propertyIndex;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			descending = descending?false:true;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			descending = true;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(viewer instanceof DynamicTableViewer)) 
			return 0;
		
		DynamicTableViewer vw = (DynamicTableViewer)viewer;
		final Table table = vw.getTable();
		TableColumn col = table.getColumn(propertyIndex);
		
		Object obj1 = null;
		Object obj2 = null;

		try {
			obj1 = vw.getColumnValue(e1, col);
			obj2 = vw.getColumnValue(e2, col);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		Integer result = 0;
		if(null == obj1 && null == obj2) {
			return 0;
		} else if (null == obj1 && null != obj2) {
			return 1;
		} else if (null != obj1 && null == obj2) {
			return -1;
		} else {
			result = ReflectionUtil.callMethod(Integer.TYPE, obj1, "compareTo", new Object[]{obj2});
			if (result == null) {
				result = obj1.toString().compareTo(obj2.toString());
			}
		}

		// If descending order, flip the direction
		if (descending) {
			result = -result;
		}
		return result;

	}
}
