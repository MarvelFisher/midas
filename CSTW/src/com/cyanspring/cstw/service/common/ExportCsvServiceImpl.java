package com.cyanspring.cstw.service.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.cstw.service.iservice.IExportCsvService;

/**
 * @author Junfeng
 * @create 3 Nov 2015
 */
public class ExportCsvServiceImpl implements IExportCsvService {
	private static Logger log = LoggerFactory.getLogger(ExportCsvServiceImpl.class);
	
	@Override
	public void exportCsv(Table table, String file) {
		List<String> fileHeader = new ArrayList<String>();
		for (TableColumn column : table.getColumns()) {
			if ( (column.getWidth() != 0) || (column.getResizable() != false) ) {
				fileHeader.add(column.getText());
			} // else -> hide column
		}
		
		FileWriter fileWriter = null;
		CSVPrinter csvPrinter = null;
		CSVFormat csvFileFormat = CSVFormat.DEFAULT;
		
		try {
			
			fileWriter = new FileWriter(file);
			csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
			csvPrinter.printRecord(fileHeader);
			for ( int i = 0; i < table.getItems().length; i++ ){
				List<String> record = new ArrayList<String>();
				TableItem item = table.getItem(i);
				for (int j = 0; j < table.getColumns().length ; j++) {
					TableColumn col = table.getColumn(j);
					if ( (col.getWidth() != 0) || (col.getResizable() != false) ) {
						record.add(item.getText(j));
					} // else -> hide column
				}
				// print a line
				csvPrinter.printRecord(record);				
			}
		
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvPrinter.close();
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}
		}
		
	}

}
