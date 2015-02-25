package com.cyanspring.adaptor.future.wind.gateway;

import java.io.FileWriter;
import java.util.ArrayList;
//import au.com.bytecode.opencsv.CSVWriter;


public class WriteDataHelper {
	WriteDataHelper(String[] header, String fileName) {
		dataList = new ArrayList<String[]>();
		try{
		    //wrData = new CSVWriter(new FileWriter(fileName),',');
		    //wrData.writeNext(header);
		}catch (Exception e) {
		    e.printStackTrace(); 
		    System.exit(0);
		}
	}
    //
	//private CSVWriter wrData;
	private ArrayList<String[]> dataList;
	
	void addRecord(String[] record){    	
        dataList.add(record);        
	}
	void WriteDataToFile(){		
		try{			
			//wrData.writeAll(dataList);
			dataList.clear();
		}catch (Exception e) {
		    e.printStackTrace();   
		}		
	}
	void WriteRecordToFile(String[] line){
		try{			
			//wrData.writeNext(line);
		}catch (Exception e) {
		    e.printStackTrace();   
		}			
	}
	void Close(){
		try{
		    //wrData.close();
		}catch (Exception e) {
		    e.printStackTrace();   
		}
	}
}
