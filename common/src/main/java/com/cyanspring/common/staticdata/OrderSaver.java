package com.cyanspring.common.staticdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Order;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.util.TimeUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author elviswu
 */
public class OrderSaver {

    private FileManager fileManager = new FileManager();

    private List<Order> orderList;
    private String filePath;
    private String prefix = "";
    private String suffix = "";

    public void saveOrderToFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Clock.getInstance().now();
        String fileDate = sdf.format(date);
        String path = filePath + "/" + prefix + fileDate + suffix + ".csv";
        fileManager.loadFile(path);
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);

        if (orderList != null && orderList.size() > 0) {
        	Collections.sort(orderList);
        	if (orderList.get(0) instanceof ParentOrder) {
        		fileManager.appendToFile("Account,Symbol,Type,Price,Qty,CumQty,AvgPx,Status,Created,ID");
        		for (Order o : orderList) {
        			ParentOrder pOrder = (ParentOrder)o;
    	            if (!TimeUtil.sameDate(date, pOrder.getCreated())) {
    					continue;
    				}

		            fileManager.appendToFile(pOrder.getAccount() + "," + pOrder.getSymbol() + "," +
		            		pOrder.getOrderType() + "," + df.format(pOrder.getPrice()) + "," +
		            		df.format(pOrder.getQuantity()) + "," + df.format(pOrder.getCumQty()) + "," +
		                    df.format(pOrder.getAvgPx()) + "," + pOrder.getOrdStatus() + "," +
		                    pOrder.getCreated() + "," + pOrder.getId());
    	        }
        	} else if (orderList.get(0) instanceof ChildOrder) {
        		fileManager.appendToFile("Account,Symbol,Price,Qty,CumQty,AvgPx,Status,Created,ID,ExchangeOID");
        		for (Order o : orderList) {
        			ChildOrder cOrder = (ChildOrder)o;
    	            if (!TimeUtil.sameDate(date, cOrder.getCreated())) {
    					continue;
    				}

    	            fileManager.appendToFile(cOrder.getAccount() + "," + cOrder.getSymbol() + "," +
    	            		df.format(cOrder.getPrice()) + "," + df.format(cOrder.getQuantity()) + "," +
    	            		df.format(cOrder.getCumQty()) + "," + df.format(cOrder.getAvgPx()) + "," +
    	            		cOrder.getOrdStatus() + "," + cOrder.getCreated() + "," + cOrder.getId() + "," +
    	            		cOrder.getExchangeOrderId());
    	        }
        	}
        }
        fileManager.close();
    }

    public void addOrderList(List<? extends Order> lstOrder) {
    	if (this.orderList == null) {
			this.orderList = new ArrayList<>();
		}
    	this.orderList.addAll(lstOrder);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
