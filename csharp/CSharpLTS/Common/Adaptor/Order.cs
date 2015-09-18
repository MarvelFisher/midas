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
        public OrdStatus ordStatus { get; set; }
        public TimeInForce timeInForce { get; set; }
        public OrderSide orderSide { get; set; }
        public OrderType orderType { get; set; }
        public string created { get; set; }
        public string clOrderId { get; set; }

    }
}
