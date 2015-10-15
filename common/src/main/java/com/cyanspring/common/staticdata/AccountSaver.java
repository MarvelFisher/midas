package com.cyanspring.common.staticdata;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.Clock;
import com.cyanspring.common.account.Account;

/**
 * @author elviswu
 */
public class AccountSaver {
    private FileManager fileManager = new FileManager();
    private List<Account> accounts;
    private String filePath;
    private String prefix = "";
    private String suffix = "";

    public void saveAccountToFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = Clock.getInstance().now();
        String fileDate = sdf.format(date);
        String path = filePath + "/" + prefix + fileDate + suffix + ".csv";
        fileManager.loadFile(path);
        fileManager.appendToFile("ID,Account Value,Account Cash,Cash Available," +
                "DailyPnl,Pnl,UrPnl");
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        for (Account a : accounts) {
            fileManager.appendToFile(a.getId() + "," + df.format(a.getValue()) + "," +
            		df.format(a.getCash()) + "," + df.format(a.getCashAvailable()) + "," +
            		df.format(a.getDailyPnL()) + "," + df.format(a.getPnL()) + "," +
            		df.format(a.getUrPnL()));
        }
        fileManager.close();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
