package com.cyanspring.common.staticdata;

import com.cyanspring.common.Clock;
import com.cyanspring.common.account.Account;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        for (Account a : accounts) {
            fileManager.appendToFile(a.getId() + "," + a.getValue() + "," +
                    a.getCash() + "," + a.getCashAvailable() + "," + a.getDailyPnL() + "," +
                    a.getPnL() + "," + a.getUrPnL());
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
