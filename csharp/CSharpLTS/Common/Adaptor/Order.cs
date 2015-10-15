using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using com.cyanspring.avro.generate.trade.types;

namespace Common.Adaptor
{
    public class Order
    {
        public string symbol { get; set; }
        public string orderId { get; set; }
        public string exchangeAccount { get; set; }
        public string exchangeOrderId { get; set; }
        public double price  { get; set; }
        public double quantity { get; set; }
        public ExecType execType { get; set; }
        public OrdStatus ordStatus { get; set; }
        public TimeInForce timeInForce { get; set; }
        public OrderSide orderSide { get; set; }
        public OrderType orderType { get; set; }
        public string created { get; set; }
        public string clOrderId { get; set; }
        public double cumQty { get; set; }
        public double avgPx { get; set; }
        public string lastMsg { get; set; }
        public string account { get; set; }

        public Order(string exchangeAccount, string symbol, string orderId, double price, double quantity, 
            OrderSide orderSide, OrderType orderType, string account)
        {
            this.exchangeAccount = exchangeAccount;
            this.symbol = symbol;
            this.orderId = orderId;
            this.price = price;
            this.quantity = quantity;
            this.orderSide = orderSide;
            this.orderType = orderType;

            this.execType = ExecType.New;
            this.ordStatus = OrdStatus.PendingNew;
            this.account = account;
        }

        override
        public string ToString()
        {
            return
                "symbol=" + symbol + "; " +
                "orderId=" + orderId + "; " +
                "exchangeAccount=" + exchangeAccount + "; " +
                "exchangeOrderId=" + exchangeOrderId + "; " +
                "price=" + price + "; " +
                "quantity=" + quantity + "; " +
                "cumQty=" + cumQty + "; " +
                "avgPx=" + avgPx + "; " +
                "execType=" + execType + "; " +
                "ordStatus=" + ordStatus + "; " +
                "timeInForce=" + timeInForce + "; " +
                "orderSide=" + orderSide + "; " +
                "orderType=" + orderType + "; " +
                "ordStatus=" + ordStatus + "; " +
                "lastMsg=" + lastMsg + "; " +
                "created=" + created + "; " +
                "clOrderId=" + clOrderId + "; " +
                "account=" + account + "; " +
                "";
        }

    }
}
