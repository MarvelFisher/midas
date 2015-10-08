package com.cyanspring.common.staticdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author elviswu
 */
public class OrderSaver {

    private FileManager fileManager = new FileManager();

    private Map<String, ParentOrder> orderMap;
    private String filePath;
    private String prefix;
    private String suffix;

    public void saveOrderToFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Clock.getInstance().now();
        String fileDate = sdf.format(date);
        String path = filePath + "/" + prefix + fileDate + suffix + ".csv";
        fileManager.loadFile(path);
        fileManager.appendToFile("Account,Symbol,Type,Price,Qty,CumQty,AvgPx,Status,Created,ID");
        for (Map.Entry<String, ParentOrder> e : orderMap.entrySet()) {
            ParentOrder o = e.getValue();
            if (!TimeUtil.sameDate(date, o.getCreated()))
                continue;
            fileManager.appendToFile(o.getAccount() + "," + o.getSymbol() + "," +
                    o.getOrderType() + "," + o.getPrice() + "," + o.getQuantity() + "," +
                    o.getCumQty() + "," + o.getAvgPx() + "," + o.getOrdStatus() + "," +
                    o.getCreated() + "," + o.getId());
        }
        fileManager.close();
    }


    public void setOrderMap(Map<String, ParentOrder> orderMap) {
        this.orderMap = orderMap;
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
