package com.cyanspring.cstw.ui.utils;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableUtils {
	private static Collator comparator = Collator.getInstance(Locale
			.getDefault());

	private final static int NONE_SORT = 0;

	private final static int DOWN_SORT = 2;

	/**
	 * Add sorter to the specified column, compares using Collator
	 */
	public static void addSorter(final TableViewer tableViewer,
			final TableColumn column) {
		final Table table = tableViewer.getTable();
		column.addListener(SWT.Selection, new Listener() {
			boolean isAscend = true;
			int sortState = NONE_SORT;// 未排序0,UP为1，DOWN为2

			@Override
			public void handleEvent(Event e) {
				if (sortState == DOWN_SORT) {
					sortState = NONE_SORT;
					table.setSortColumn(null);
					tableViewer.refresh();
					return;
				} else {
					sortState = sortState + 1;
				}
				int columnIndex = getColumnIndex(table, column);
				TableItem[] items = table.getItems();
				for (int i = 1; i < items.length; i++) {
					String value2 = items[i].getText(columnIndex);
					for (int j = 0; j < i; j++) {
						String value1 = items[j].getText(columnIndex);
						boolean isLessThan = comparator.compare(value2, value1) < 0;
						if ((isAscend && isLessThan)
								|| (!isAscend && !isLessThan)) {
							String[] values = getTableItemText(table, items[i]);
							Object obj = items[i].getData();
							items[i].dispose();
							TableItem item = new TableItem(table, SWT.NONE, j);
							item.setText(values);
							item.setData(obj);
							items = table.getItems();
							break;
						}
					}
				}
				table.setSortColumn(column);
				table.setSortDirection((isAscend ? SWT.UP : SWT.DOWN));
				isAscend = !isAscend;
			}
		});
	}

	private static int getColumnIndex(Table table, TableColumn column) {
		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(column))
				return i;
		}
		return -1;
	}

	private static String[] getTableItemText(Table table, TableItem item) {
		int count = table.getColumnCount();
		String[] strs = new String[count];
		for (int i = 0; i < count; i++) {
			strs[i] = item.getText(i);
		}
		return strs;
	}
}
