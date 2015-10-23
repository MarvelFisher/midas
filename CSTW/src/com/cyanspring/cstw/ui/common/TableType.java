package com.cyanspring.cstw.ui.common;

import org.eclipse.swt.SWT;

/**
 * 
 * @author NingXiaoFeng
 * @create date 2015/05/19
 *
 */
public enum TableType {

	
	// RW
	RWPosition(new String[] { "Account", "Instrument ID", "Instrument Name",
			"Direction", "Quantity", "PNL", "Avg Price", "Trader", },
			new int[] { 100, 100, 100, 100, 100, 100, 100, 100 }, SWT.CENTER,
			true, false),
	
	RWTradeRecord(new String[] { "Account", "Trade ID", "Instrument ID",
			"Instrument", "Trade Type", "Quantity", "Avg Price",
			"Consideration", "Time", "Commission", "Trader" }, new int[] { 100,
			100, 100, 100, 100, 100, 100, 100, 100, 100, 100 }, SWT.CENTER,
			true, false),

	RWInstrumentStatistics(new String[] { "Account", "Instrument ID",
			"Instrument Name", "Realized PNL", "Trades", "Total Quantity",
			"Consideration", "Total Commission", "Trader" }, new int[] { 100,
			100, 100, 100, 100, 100, 100, 100, 100 }, SWT.CENTER, true, false),

	RWUserStatistics(
			new String[] { "Trader", "Realized PNL", "Consideration" },
			new int[] { 100, 100, 100 }, SWT.CENTER, true, false),

	RWInstrumentSummary(new String[] { "Account", "Instrument ID",
			"Instrument Name", "Realized PNL", "Trades", "Total Quantity",
			"Consideration", "Commission" }, new int[] { 100, 100, 100, 100,
			100, 100, 100, 100 }, SWT.CENTER, true, false),
	
	// BW
	BWPosition(new String[]{ "Account", "Instrument ID", "Instrument Name",
			"Direction", "Quantity", "PNL", "Avg Price", }, new int[]{ 100, 100, 100, 100, 100, 100, 100, }, SWT.CENTER, true, false),
	
	BWTradeRecord(new String[]{ "Account", "Trade ID", "Instrument ID",
			"Instrument", "Trade Type", "Quantity", "Avg Price",
			"Consideration", "Time", "Commission", }, new int[]{ 100,
			100, 100, 100, 100, 100, 100, 100, 100, 100, }, SWT.CENTER, true, false),
	
	BWInstrumentStatistics(new String[]{ "Account", "Instrument ID",
			"Instrument Name", "Realized PNL", "Trades", "Total Quantity",
			"Consideration", "Total Commission", }, new int[]{ 100,
			100, 100, 100, 100, 100, 100, 100, }, SWT.CENTER, true, false),
	
	BWInstrumentSummary(new String[]{ "Account", "Instrument ID",
			"Instrument Name", "Realized PNL", "Trades", "Total Quantity",
			"Consideration", "Commission" }, new int[]{ 100, 100, 100, 100,
			100, 100, 100, 100 }, SWT.CENTER, true, false),
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
