package com.cyanspring.common.staticdata;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.Order;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.util.TimeUtil;

public class CumQuantitySaver extends OrderSaver {

	private final String TAIL_TITLE = "Total";

	@Override
    public void saveToFile() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Clock.getInstance().now();
        String fileDate = sdf.format(date);
        String path = filePath + "/" + prefix + fileDate + suffix + ".csv";
        fileManager = new FileManager();
        fileManager.loadFile(path);
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);

        // The following is to generate report looks like below:
        //         TXFJ5 | TXFK5 | TXFL5 | Total
        // Acc1  |   2   |   1   |   2   |   5
        // Acc2  |   1   |   3   |   2   |   6
        // Acc3  |   3   |   1   |   2   |   6
        // Total |   6   |   5   |   6   |   17
        if (orderList != null && orderList.size() > 0) {
        	Map<String, Map<String, Integer>> mapAccountQuantity = new HashMap<>();
        	List<String> lstCurrTrdSymbol = new ArrayList<>();
        	Map<String, Integer> mapSymbolQuantity = new HashMap<>();
        	int totalQ = 0;
        	for (Iterator<Order> iterator = orderList.iterator(); iterator.hasNext();) {
        		ParentOrder pOrder = (ParentOrder)iterator.next();
        		if (!TimeUtil.sameDate(date, pOrder.getCreated())) {
        	        iterator.remove();
        	    } else {
        	    	String account = pOrder.getAccount();
        	    	String symbol = pOrder.getSymbol();
        	    	if (!lstCurrTrdSymbol.contains(symbol)) {
        	    		lstCurrTrdSymbol.add(symbol);
        	    	}

        	    	Integer q = mapSymbolQuantity.get(symbol);
        	    	if (q == null) {
						mapSymbolQuantity.put(symbol, 1);
					} else {
						mapSymbolQuantity.put(symbol, q + 1);
					}

        	    	Map<String, Integer> mapQuantity = mapAccountQuantity.get(account);
        	    	if (mapQuantity == null) {
        	    		mapQuantity = new HashMap<>();
        	    		mapQuantity.put(symbol, 1);
        	    		mapAccountQuantity.put(account, mapQuantity);
					} else {
						Integer quantity = mapQuantity.get(symbol);
						if (quantity == null) {
							mapQuantity.put(symbol, 1);
						} else {
							mapQuantity.put(symbol, quantity + 1);
						}
					}
				}
        	}

        	fileManager.append(",");

        	Collections.sort(lstCurrTrdSymbol);
        	int size = lstCurrTrdSymbol.size();
        	for (int i = 0; i < size; i++) {
				fileManager.append(lstCurrTrdSymbol.get(i) + ",");
			}
        	fileManager.appendToFile(TAIL_TITLE);

        	List<String> lstAccounts = new ArrayList<>(mapAccountQuantity.keySet());
        	Collections.sort(lstAccounts);
    		for (String account : lstAccounts) {
    			int total = 0;
    			fileManager.append(account + ",");
    			for (String symbol : lstCurrTrdSymbol) {
    				Map<String, Integer> mapQuantity = mapAccountQuantity.get(account);
    				if (mapQuantity != null) {
    					Integer quantity = mapQuantity.get(symbol);
    					if (quantity == null) {
    						fileManager.append("0,");
						} else {
							int q = quantity.intValue();
							total += q;
							fileManager.append(q + ",");
						}
					}
    			}
    			fileManager.appendToFile(total + "");
	        }

    		fileManager.append(TAIL_TITLE + ",");
    		for (String symbol : lstCurrTrdSymbol) {
    			Integer q = mapSymbolQuantity.get(symbol);
    			if (q == null) {
    				fileManager.append("0,");
    			} else {
    				totalQ += q;
					fileManager.append(q + ",");
				}
    		}
    		fileManager.appendToFile(totalQ + "");
        } else {
        	fileManager.appendToFile("No orders to display on " + fileDate);
        }
        fileManager.close();
    }

}
