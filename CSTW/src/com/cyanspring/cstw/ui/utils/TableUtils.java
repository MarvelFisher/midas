package com.cyanspring.cstw.ui.utils;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableUtils {
	/**
	 * Add sorter to the specified column, compares using Collator
	 */
	public static void addSorter(final Table table, final TableColumn column) {
		column.addListener(SWT.Selection, new Listener() {
			boolean isAscend = true;
			Collator comparator = Collator.getInstance(Locale.getDefault());

			public void handleEvent(Event e) {
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

	public static int getColumnIndex(Table table, TableColumn column) {
		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(column))
				return i;
		}
		return -1;
	}

	public static String[] getTableItemText(Table table, TableItem item) {
	 	int count = table.getColumnCount();
		String[] strs = new String[count];
		for (int i = 0; i < count; i++) {
			strs[i] = item.getText(i);
		}
		return strs;
	}
}
