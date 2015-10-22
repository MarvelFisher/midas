package com.cyanspring.cstw.ui.common;

import org.eclipse.swt.SWT;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public enum TableType {

	
	// Common RiskControl
	AccountTable4RiskCtrl(new String[] { "交易账号", "总交易额", "已实现盈利" }, new int[] {
			100, 100, 100 }, SWT.CENTER, false, false),

	InstrumentSummary4RiskCtrl(new String[] { "交易账号", "证券代码", "证券名称", "已实现盈利",
			"交易笔数", "总交易股数", "总交易额", "总交易费用" }, new int[] { 100, 100, 100, 100,
			100, 100, 100, 100 }, SWT.CENTER, false, false),

	CapitalTable4RiskCtrl(new String[] { "交易账号", "可用资金", "挂单资金" }, new int[] {
			100, 100, 100 }, SWT.CENTER, false, false),

	;

	private String[] columnTiles;

	private int[] columnWidths;

	private int columnStyle;

	private boolean isSortable;

	private boolean isCheckStyle;

	private TableType(String[] columnTiles, int[] columnWidths,
			int columnStyle, boolean isSortable, boolean isCheckStyle) {
		this.columnTiles = columnTiles;
		this.columnWidths = columnWidths;
		this.columnStyle = columnStyle;
		this.isSortable = isSortable;
		this.isCheckStyle = isCheckStyle;
	}

	public String[] getColumnTiles() {
		return columnTiles;
	}

	public int[] getColumnWidths() {
		return columnWidths;
	}

	public int getColumnStyle() {
		return columnStyle;
	}

	public boolean isSortable() {
		return isSortable;
	}

	public String getTableName() {
		return name();
	}

	public boolean isCheckStyle() {
		return isCheckStyle;
	}
}
