package com.cyanspring.cstw.gui.common;

import java.util.List;

public class ColumnLayout {
	private String key;
	private List<ColumnProperty> columnProperty;
	
	public ColumnLayout(String key, List<ColumnProperty> columnProperty) {
		super();
		this.key = key;
		this.columnProperty = columnProperty;
	}
	public String getKey() {
		return key;
	}
	public List<ColumnProperty> getColumnProperty() {
		return columnProperty;
	}
	
	
}
