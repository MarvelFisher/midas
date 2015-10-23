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
public class OrderSaver extends AbstractReportSaver {

    protected List<Order> orderList;

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

        if (orderList != null && orderList.size() > 0) {
        	Collections.sort(orderList);
        	if (orderList.get(0) instanceof ParentOrder) {
        		fileManager.appendToFile("ID,Account,Symbol,OrdSide,Type,Price,Qty,CumQty,AvgPx,Status,Created");
        		for (Order o : orderList) {
        			ParentOrder pOrder = (ParentOrder)o;
    	            if (!TimeUtil.sameDate(date, pOrder.getCreated())) {
    					continue;
    				}

    	            fileManager.appendToFile(pOrder.getId() + "," + pOrder.getAccount() + "," +
    	            		pOrder.getSymbol() + "," + pOrder.getSide() + "," + pOrder.getOrderType() + "," +
    	            		df.format(pOrder.getPrice()) + "," + df.format(pOrder.getQuantity()) + "," +
    	            		df.format(pOrder.getCumQty()) + "," + df.format(pOrder.getAvgPx()) + "," +
    	            		pOrder.getOrdStatus() + "," + pOrder.getCreated());
    	        }
        	} else if (orderList.get(0) instanceof ChildOrder) {
        		fileManager.appendToFile("ID,Account,Symbol,OrdSide,Price,Qty,CumQty,AvgPx,Status,Created,ParentOID,ExchangeOID");
        		for (Order o : orderList) {
        			ChildOrder cOrder = (ChildOrder)o;
    	            if (!TimeUtil.sameDate(date, cOrder.getCreated())) {
    					continue;
    				}

    	            fileManager.appendToFile(cOrder.getId() + "," +cOrder.getAccount() + "," + cOrder.getSymbol() + "," +
    	            		cOrder.getSide() + "," + df.format(cOrder.getPrice()) + "," + df.format(cOrder.getQuantity()) + "," +
    	            		df.format(cOrder.getCumQty()) + "," + df.format(cOrder.getAvgPx()) + "," +
    	            		cOrder.getOrdStatus() + "," + cOrder.getCreated() + "," +
    	            		cOrder.getParentOrderId() + "," + cOrder.getExchangeOrderId());
    	        }
        	}
        } else {
        	fileManager.appendToFile("No orders to display on " + fileDate);
        }
        fileManager.close();
    }

    public void addOrderList(List<? extends Order> lstOrder) {
    	if (this.orderList == null) {
			this.orderList = new ArrayList<>();
		}
    	this.orderList.addAll(lstOrder);
    }

}
