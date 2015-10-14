package com.cyanspring.common.staticdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.util.TimeUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author elviswu
 */
public class OrderSaver {

    private FileManager fileManager = new FileManager();

    private List<ParentOrder> orderList;
    private String filePath;
    private String prefix = "";
    private String suffix = "";

    public void saveOrderToFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Clock.getInstance().now();
        String fileDate = sdf.format(date);
        String path = filePath + "/" + prefix + fileDate + suffix + ".csv";
        fileManager.loadFile(path);
        fileManager.appendToFile("Account,Symbol,Type,Price,Qty,CumQty,AvgPx,Status,Created,ID");
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        for (ParentOrder o : orderList) {
            if (!TimeUtil.sameDate(date, o.getCreated())) {
				continue;
			}
            fileManager.appendToFile(o.getAccount() + "," + o.getSymbol() + "," +
                    o.getOrderType() + "," + df.format(o.getPrice()) + "," +
            		df.format(o.getQuantity()) + "," + df.format(o.getCumQty()) + "," +
                    df.format(o.getAvgPx()) + "," + o.getOrdStatus() + "," +
                    o.getCreated() + "," + o.getId());
        }
        fileManager.close();
    }


    public void addOrderMap(ParentOrder order) {
    	if (this.orderList == null) {
			this.orderList = new ArrayList<>();
		}
    	this.orderList.add(order);
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
