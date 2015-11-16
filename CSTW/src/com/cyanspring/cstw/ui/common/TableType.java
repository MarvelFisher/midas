package com.cyanspring.cstw.ui.common;

import org.eclipse.swt.SWT;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public enum TableType {
	
	// Super Admin
	InstrumentInfo(new String[] { "Instrument", "Instrument Name", "Quantity" },
			new int[] { 100, 100, 100 }, SWT.CENTER, false, false),
			
	AssignedInfo(new String[] { "No.", "User", "Role" },
			new int[] { 100, 100, 100 }, SWT.CENTER, false, false),

	// RW Order
	RWPendingOrder(new String[] { "Order ID", "Instrument ID", "Trade Type",
			"Price", "Volume", "Status", "Time", "Trader" }, new int[] { 80,
			80, 80, 80, 80, 80, 80, 80 }, SWT.LEFT, true, false),

	RWActivityOrder(new String[] { "Order ID", "Instrument ID", "Trade Type",
			"Price", "Volume", "Status", "Consumed Volume", "Time", "Trader" },
			new int[] { 80, 80, 80, 80, 80, 80, 80, 80, 80 }, SWT.LEFT, true,
			false),

	// RW
	RWPosition(new String[] { "Instrument ID", "Direction", "Quantity", "UR PnL",
			"Avg Price", "Trader", },
			new int[] { 100, 100, 100, 100, 100, 100 }, SWT.CENTER, true, false),

	RWTradeRecord(new String[] { "Trade ID", "Instrument ID", "Trade Type",
			"Quantity", "Avg Price", "Consideration", "Time", "Trader" },
			new int[] { 100, 100, 100, 100, 100, 100, 100, 100, }, SWT.CENTER,
			true, false),

	RWInstrumentStatistics(new String[] { "Instrument ID", "PnL",
			"Trades", "Total Quantity", "Consideration", "Trader" }, new int[] {
			100, 100, 100, 100, 100, 100, }, SWT.CENTER, true, false),

	RWUserStatistics(
			new String[] { "Trader", "PnL", "Consideration" },
			new int[] { 100, 100, 100 }, SWT.CENTER, true, false),

	RWInstrumentSummary(new String[] { "Instrument ID", "PnL",
			"Trades", "Total Quantity", "Consideration" }, new int[] { 100,
			100, 100, 100, 100, }, SWT.CENTER, true, false),

	// BW
	BWPosition(new String[] { "Instrument ID", "Direction", "Quantity", "UR PnL",
			"Avg Price", }, new int[] { 100, 100, 100, 100, 100, }, SWT.CENTER,
			true, false),

	BWTradeRecord(new String[] { "Trade ID", "Instrument ID", "Trade Type",
			"Quantity", "Avg Price", "Consideration", "Time", }, new int[] {
			100, 100, 100, 100, 100, 100, 100, }, SWT.CENTER, true, false),

	BWInstrumentStatistics(new String[] { "Instrument ID", "PnL",
			"Trades", "Total Quantity", "Consideration", }, new int[] { 100,
			100, 100, 100, 100, }, SWT.CENTER, true, false),

	BWInstrumentSummary(new String[] { "Instrument ID", "PnL",
			"Trades", "Total Quantity", "Consideration" }, new int[] { 100,
			100, 100, 100, 100, }, SWT.CENTER, true, false), ;

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

	public int getColumnIndex(String columnName) {
		int i = 0;
		for (String title : columnTiles) {
			if (title.equals(columnName)) {
				return i;
			}
			i++;
		}
		return -1;
	}
}
