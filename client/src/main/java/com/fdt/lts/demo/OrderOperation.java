package com.fdt.lts.demo;

import com.cyanspring.apievent.obj.*;
import com.fdt.lts.client.LtsApi;
import com.fdt.lts.client.TradeAdaptor;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author elviswu
 * @version 1.0
 * @since 1.0
 */
public class OrderOperation {
    static {
        DOMConfigurator.configure("conf/apilog4j.xml");
    }

    private static Logger log = LoggerFactory.getLogger(OrderOperation.class);
    private String ip;
    private int port;
    private List<String> symbolList;
    private String user;
    private String pwd;
    private boolean error;
    private Order newOrder;
    private Order replyNewOrder;
    private Order amendOrder;
    private Order replyAmendOrder;
    private Order cancelOrder;
    private Order replyCancelOrder;

    public OrderOperation(String ip, int port, String user, String pwd, List<String> symbolList) {
        this.ip = ip;
        this.port = port;
        this.symbolList = symbolList;
        this.user = user;
        this.pwd = pwd;
    }

    public void start(){
        LtsApi api = new LtsApi(ip, port);
        TradeAdaptor adaptor = new TradeAdaptor() {
            public void onStart() {
                log.info("Server is connected!");
            }

            public void onAccountUpdate() {

            }

            public void onQuote(Quote quoteData) {
                log.info(quoteData.toString());
                String symbol = quoteData.getSymbol();
                log.info("> Place a new order");
                newOrder = new Order();
                newOrder.setSymbol(symbol);
                newOrder.setSide(OrderSide.Buy);
                newOrder.setType(OrderType.Limit);
                newOrder.setPrice(0.01);
                newOrder.setQuantity(1000);
                newOrder(newOrder);
            }

            public void onNewOrderReply(Order order) {
                log.info("> Amend a order: " + order.getId());
                amendOrder = new Order();
                amendOrder.setId(order.getId());
                amendOrder.setSymbol(order.getSymbol());
                amendOrder.setPrice(0.02);
                amendOrder.setQuantity(2000);
                amendOrder(amendOrder);
            }

            public void onAmendOrderReply(Order order) {
                log.info("> Cancel a order: " + order.getId());
                cancelOrder = new Order();
                cancelOrder.setId(order.getId());
                cancelOrder(cancelOrder);
            }

            public void onCancelOrderReply(Order order) {
                log.info("> Cancel order complete: " + order.getId());
                terminate();
            }

            public void onOrderUpdate(Order order) {
                log.info("Order update, ID: " + order.getId());
            }

            public void onError(int i, String s) {
                error = true;
                log.error(s + ", " + i);
            }
        };

        api.start(user, pwd, symbolList, adaptor);
    }

    public boolean isError() {
        return error;
    }

    public void setNewOrder(Order newOrder) {
        this.newOrder = newOrder;
    }

    public Order getNewOrder() {
        return newOrder;
    }

    public void setReplyNewOrder(Order replyNewOrder) {
        this.replyNewOrder = replyNewOrder;
    }

    public Order getReplyNewOrder() {
        return replyNewOrder;
    }

    public void setAmendOrder(Order amendOrder) {
        this.amendOrder = amendOrder;
    }

    public Order getAmendOrder() {
        return amendOrder;
    }

    public void setReplyAmendOrder(Order replyAmendOrder) {
        this.replyAmendOrder = replyAmendOrder;
    }

    public Order getReplyAmendOrder() {
        return replyAmendOrder;
    }

    public void setCancelOrder(Order cancelOrder) {
        this.cancelOrder = cancelOrder;
    }

    public Order getCancelOrder() {
        return cancelOrder;
    }

    public void setReplyCancelOrder(Order replyCancelOrder) {
        this.replyCancelOrder = replyCancelOrder;
    }

    public Order getReplyCancelOrder() {
        return replyCancelOrder;
    }

    public static void main(String[] args) {
        List<String> subscribeList = new ArrayList<String>();
        subscribeList.add("AUDUSD");
        OrderOperation op = new OrderOperation("localhost", 52368, "test1", "xxx", subscribeList);
        op.start();
    }
}
