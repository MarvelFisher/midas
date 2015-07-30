package com.fdt.lts.demo;

import com.cyanspring.apievent.obj.*;
import com.fdt.lts.client.LtsApi;
import com.fdt.lts.client.TradeAdaptor;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class ListAccountInfo {
    static {
        DOMConfigurator.configure("conf/apilog4j.xml");
    }

    private static Logger log = LoggerFactory.getLogger(ListAccountInfo.class);
    private String ip;
    private int port;
    private String user;
    private String pwd;
    private boolean error;
    private AccountInfo info;
    private int positionCount;
    private int executionCount;
    private int orderCount;

    public ListAccountInfo(String ip, int port, String user, String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public void show(){
        LtsApi api = new LtsApi(ip, port);
        TradeAdaptor adaptor = new TradeAdaptor() {
            public void onStart() {
                log.info("> ===== Base account information =====");
                log.info("> Value: " + accountInfo.getAccount().getValue());
                log.info("> Currency: " + accountInfo.getAccount().getCurrency());
                log.info("> Cash: " + accountInfo.getAccount().getCash());
                log.info("> CashAvailable: " + accountInfo.getAccount().getCashAvailable());
                log.info("> DailyPnL: " + accountInfo.getAccount().getDailyPnL());
                log.info("> PnL: " + accountInfo.getAccount().getPnL());
                log.info("> UrPnL: " + accountInfo.getAccount().getUrPnL());
                log.info("> AllTimePnL: " + accountInfo.getAccount().getAllTimePnL());

                log.info("> ===== Open positions information =====");
                List<String> symbolList = new ArrayList<String>();
                for (OpenPosition position : accountInfo.getOpenPositions()){
                    log.info("Symbol: " + position.getSymbol());
                    symbolList.add(position.getSymbol());
                    positionCount++;
                }

                log.info("> ===== Executions =====");
                for (String symbol : symbolList){
                    List<Execution> executions = accountInfo.getExecutions(symbol);
                    for (Execution execution : executions){
                        log.info("> Symbol: " + execution.getSymbol() + ", EXEID: " + execution.getExecID());
                        executionCount++;
                    }
                }

                log.info("> ===== Orders =====");
                for (Map.Entry<String, Order> entry : orderMap.entrySet()){
                    log.info("> ID: " + entry.getKey() + ", Order: " + entry.getValue().toString());
                    orderCount++;
                }
                terminate();
            }

            public void onAccountUpdate() {

            }

            public void onQuote(Quote quoteData) {

            }

            public void onNewOrderReply(Order order) {

            }

            public void onAmendOrderReply(Order order) {

            }

            public void onCancelOrderReply(Order order) {

            }

            public void onOrderUpdate(Order order) {

            }

            public void onError(int i, String s) {
                error = true;
                log.error(s + ", " + i);
            }
        };

        api.start(user, pwd, new ArrayList<String>(), adaptor);
    }

    public void setInfo(AccountInfo info) {
        this.info = info;
    }

    public AccountInfo getAccountInfo(){
        return this.info;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public boolean isError() {
        return error;
    }

    public static void main(String[] args) {
        ListAccountInfo accountInfo = new ListAccountInfo("localhost", 52368, "test1", "xxx");
        accountInfo.show();
    }
}
