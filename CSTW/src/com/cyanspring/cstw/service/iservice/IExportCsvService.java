package com.cyanspring.cstw.service.iservice;

import org.eclipse.swt.widgets.Table;

/**
 * @author Junfeng
 * @create 3 Nov 2015
 */
public interface IExportCsvService {
	void exportCsv(Table tableViewer, String file);
}
