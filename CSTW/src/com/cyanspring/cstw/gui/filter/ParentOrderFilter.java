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
package com.cyanspring.cstw.gui.filter;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.springframework.util.StringUtils;

public class ParentOrderFilter extends ViewerFilter {
	String column;
	String pattern;
	List<String> lst;
	public void setMatch(String column, String pattern) {
		this.column = column;
		this.pattern = pattern;
	}
	
	public void setMatch(String column, List<String> lst){
		this.column = column;
		this.lst = lst;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof HashMap))
			return true;
		
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>)element;
		Object field = map.get(column);
		if (null == field){			
			if(StringUtils.hasText(pattern))
				return false;
			
			return true;
		}
		if(pattern != null){
			if(field.toString().toUpperCase().indexOf(pattern.toUpperCase()) > -1)
				return true;			
		}
		if(lst != null){
			return lst.contains(field.toString());
		}
		return false;
	}

	public String getColumn() {
		return column;
	}

	public String getPattern() {
		return pattern;
	}

}
