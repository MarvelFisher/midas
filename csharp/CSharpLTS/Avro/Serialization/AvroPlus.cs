using com.cyanspring.avro.generate.@base.types;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace com.cyanspring.avro.generate.@base
{
    public partial class StateUpdate
    {
        public StateUpdate()
        {
            this._objectType = (int)ObjectType.StateUpdate;
            this._exchangeAccount = "";
        }
    }
}

namespace com.cyanspring.avro.generate.market.bean
{
    public partial class Quote
    {
        public Quote()
        {
            this._objectType = (int)ObjectType.Quote;
            this._symbol = "";
        }
    }

    public partial class SubscribeQuote
    {
        public SubscribeQuote()
        {
            this._objectType = (int)ObjectType.SubscribeQuote;
            this._symbols = new List<string>();
        }
    }

    public partial class UnsubscribeQuote
    {
        public UnsubscribeQuote()
        {
            this._objectType = (int)ObjectType.UnsubscribeQuote;
            this._symbols = new List<string>();
        }
    }
}

namespace com.cyanspring.avro.generate.trade.bean
{
    public partial class AmendOrderReply
    {
        public AmendOrderReply()
        {
            this._objectType = (int)ObjectType.AmendOrderReply;
            this._exchangeAccount = "";
            this._message = "";
            this._orderId = "";
            this._txId = "";
        }
    }

    public partial class CancelOrderReply
    {
        public CancelOrderReply()
        {
            this._objectType = (int)ObjectType.CancelOrderReply;
            this._exchangeAccount = "";
            this._message = "";
            this._orderId = "";
            this._txId = "";
        }
    }

    public partial class NewOrderReply
    {
        public NewOrderReply()
        {
            this._objectType = (int)ObjectType.NewOrderReply;
            this._exchangeAccount = "";
            this._message = "";
            this._orderId = "";
            this._txId = "";
        }
    }

    public partial class OrderUpdate
    {
        public OrderUpdate()
        {
            this._orderType = (int)ObjectType.OrderUpdate;
            this._clOrderId = "";
            this._created = "";
            this._exchangeAccount = "";
            this._exchangeOrderId = "";
            this._msg = "";
            this._orderId = "";
            this._symbol = "";
            this._txId = "";
        }
    }
}
