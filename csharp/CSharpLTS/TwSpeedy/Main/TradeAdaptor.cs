using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Common.Adaptor;
using System.Collections.Concurrent;
using OrderConnection;
using OrderMessage;
using TwSpeedy.Utils;

namespace Adaptor.TwSpeedy.Main
{
    class TradeAdaptor : IDownStreamAdaptor
    {
        private IDownStreamListener listener;
        private bool state;
        private ConcurrentDictionary<string, Order> orders = new ConcurrentDictionary<string, Order>();
        private ConcurrentDictionary<long, Order> pendings = new ConcurrentDictionary<long, Order>();
        private ConcurrentDictionary<string, IExecutionReportMessage> ers = new ConcurrentDictionary<string, IExecutionReportMessage>();

        private ConcurrentDictionary<string, Order> recoverOrders = new ConcurrentDictionary<string, Order>();
        private TaifexConnection exchangeConnection;
        private string localIP;
        private bool recovering;
        public string user { get; set; } = "haida";
        public string account { get; set; } = "1365651";
        public string password { get; set; } = "123456";
        public string subAccount { get; set; } = "1365651";
        public string brokerID { get; set; } = "F018000";
        public string memberID { get; set; } = "F018";
        public string host { get; set; } = "Speedy150.masterlink.com.tw";
        public int port { get; set; } = 23456;


        public void init()
        {
            localIP = Utils.getLocalIPAddress();

            TaifexConnection exchangeConnection = new TaifexConnection();
            exchangeConnection.OnConnected += new OrderConnection.ITaifexConnectionEvents_OnConnectedEventHandler(onExchangeConnected);
            exchangeConnection.OnDisconnected += new OrderConnection.ITaifexConnectionEvents_OnDisconnectedEventHandler(onExchangeDisconnected);
            exchangeConnection.OnExecutionReport += new OrderConnection.ITaifexConnectionEvents_OnExecutionReportEventHandler(onExecutionReport);
            exchangeConnection.OnLogonReply += new OrderConnection.ITaifexConnectionEvents_OnLogonReplyEventHandler(onLogonReply);
            exchangeConnection.OnRecoverFinished += new OrderConnection.ITaifexConnectionEvents_OnRecoverFinishedEventHandler(onRecoverFinished);

        }

        private void connect()
        {
            exchangeConnection.Destroy();
            exchangeConnection.Create2(account);
            exchangeConnection.BrokerID = brokerID;
            exchangeConnection.ClearMemberID = memberID;
            exchangeConnection.SetLanguage(OrderConnection.MessageLanguageEnum.mlChinese);
            exchangeConnection.Connect(host, port);
        }

        private void login()
        {
            exchangeConnection.Logon(user, password, account, OrderConnection.ConnectionTypeEnum.ctBoth);
        }

        private void onExchangeConnected()
        {
            System.Diagnostics.Debug.WriteLine("====================Connected====================");
            login();
        }

        private void onExchangeDisconnected()
        {
            System.Diagnostics.Debug.WriteLine("====================Disconnected====================");
        }

        void onExecutionReport(OrderMessage.IExecutionReportMessage msg, OrderConnection.ExecDupEnum possDup)
        {
            Utils.printExecutionReport(msg, possDup);
            if(recovering)
            {
                // TODO
            } else
            {
                processExecutionReport(msg, possDup);
            }
        }

        void processExecutionReport(OrderMessage.IExecutionReportMessage msg, OrderConnection.ExecDupEnum possDup)
        {
            if(possDup == OrderConnection.ExecDupEnum.edNewExecution)
            {
                ers.TryAdd(msg.OrderID, msg);

                Order existingOrder;
                if(null != msg.OrderID && msg.OrderID != "")
                {
                    if(orders.TryGetValue(msg.OrderID, out existingOrder))
                    {
                        updateOrder(existingOrder, msg);
                    }
                    else if (msg.NID != 0 && pendings.TryRemove(msg.NID, out existingOrder))
                    {
                        updateOrder(existingOrder, msg);
                        orders[msg.OrderID] = existingOrder;
                    }
                    else
                    {
                        System.Diagnostics.Debug.WriteLine("Cant find corresponding order in cache");
                    }

                }
                else
                {
                    System.Diagnostics.Debug.WriteLine("ExecutionReport OrderId is null or empty");
                }
            }
        }

        private void onOrder(Order order)
        {
            if (this.listener != null)
                this.listener.onOrder(order);
        }

        private void updateOrder(Order order, IExecutionReportMessage er)
        {
            //TODO
            order.exchangeOrderId = er.OrderID;
            switch(er.ExecType)
            {
                case ExecTypeEnum.etCanceled:

                    break;
            }
        }

        void onLogonReply(string ReplyString, OrderConnection.LogonResultEnum LogonResult, int ConnectionID)
        {
            if (LogonResult == OrderConnection.LogonResultEnum.lrFailed)
            {
                System.Diagnostics.Debug.WriteLine("Login failed");
            }
            else if (LogonResult == OrderConnection.LogonResultEnum.lrOK)
            {
                System.Diagnostics.Debug.WriteLine("Login succeeded");
                recovering = true;
                exchangeConnection.Recover("031000", OrderConnection.RecoverTypeEnum.rtAll, OrderConnection.RecoverMarketEnum.rmAll);
            }
        }

        void onRecoverFinished(int Count)
        {
            //回補
            System.Diagnostics.Debug.WriteLine("====================Recovery Done====================");
            return;
        }

        public void addListener(IDownStreamListener listener)
        {
            this.listener = listener;
        }

        public void removeListener(IDownStreamListener listener)
        {
            if(this.listener != listener)
                throw new DownStreamException("Can't find this listener to remove");

            this.listener = null;
        }

        public void newOrder(Order order)
        {
            OrderMessage.NewOrderMessage newOrderMsg = new OrderMessage.NewOrderMessage();
            newOrderMsg.AE = user;
            newOrderMsg.Account = account;
            newOrderMsg.BrokerID = brokerID;
            newOrderMsg.Symbol = order.symbol;

            // TODO
            if(order.orderType == 0)
            {
                newOrderMsg.OrderType = OrderMessage.OrderTypeEnum.otMarket;
            }
            else if (order.orderType == 1)
            {
                newOrderMsg.OrderType = OrderMessage.OrderTypeEnum.otLimit;
            } else
            {
                throw new DownStreamException("Unsupported order type: " + order.orderType);
            }

            // TODO
            if (order.orderSide == 0)
            {
                newOrderMsg.Side = OrderMessage.SideEnum.sBuy;
            }
            else if (order.orderSide == 1)
            {
                newOrderMsg.Side = OrderMessage.SideEnum.sSell;
            }
            else
            {
                throw new DownStreamException("Unsupported order side: " + order.orderSide);
            }

            newOrderMsg.Price = order.price;
            newOrderMsg.OrderQty = (int)order.quantity;
            newOrderMsg.Market = OrderMessage.MarketEnum.mFutures;
            //TO DO
            newOrderMsg.TimeInForce = OrderMessage.TimeInForceEnum.tifROD;
            newOrderMsg.Data = formatData(newOrderMsg.OrderQty);
            newOrderMsg.TradingSessionID = 0;
            newOrderMsg.PositionEffect = OrderMessage.PositionEffectEnum.peClose;
            //DateTime dt = new DateTime();
            //newOrderMsg.ClOrdID = dt.ToString("yyyy-MM-dd hh:mm:ss.fff");
            //System.Diagnostics.Debug.WriteLine("ClOrderID: " + newOrderMsg.ClOrdID);

            newOrderMsg.NID = exchangeConnection.GenerateUniqueID(newOrderMsg.Market, OrderMessage.MessageTypeEnum.mtNew);

            pendings[newOrderMsg.NID] = order;
            exchangeConnection.NewOrder(newOrderMsg);
        }

        public void amendOrder(String exchangeOrderId, double price, double qty)
        {
            Order order;
            if(!orders.TryGetValue(exchangeOrderId, out order))
            {
                throw new DownStreamException("Amend order not found: " + exchangeOrderId);
            }

            if(qty != 0 && qty > order.quantity)
            {
                throw new DownStreamException("Can't amend quantity up: " + exchangeOrderId);
            }

            if(price == order.price && qty == order.quantity)
            {
                throw new DownStreamException("Can't amend price and qty to same value: " + exchangeOrderId);
            }

            OrderMessage.ReplaceOrderMessage replaceOrderMessage = new OrderMessage.ReplaceOrderMessage();
            replaceOrderMessage.AE = user;
            replaceOrderMessage.Account = account;
            replaceOrderMessage.BrokerID = brokerID;
            replaceOrderMessage.Symbol = order.symbol;
            replaceOrderMessage.OrderID = order.exchangeOrderId;
            replaceOrderMessage.Side = XXX;
            replaceOrderMessage.OrderQty = (int)qty;
            replaceOrderMessage.Data = formatData(replaceOrderMessage.OrderQty);

            replaceOrderMessage.NID = exchangeConnection.GenerateUniqueID(xxx.Market, OrderMessage.MessageTypeEnum.mtReplace);
            exchangeConnection.ReplaceOrder(replaceOrderMessage);
        }

        public void cancelOrder(string exchangeOrderId)
        {
            Order order;
            if (!orders.TryGetValue(exchangeOrderId, out order))
            {
                throw new DownStreamException("Amend order not found: " + exchangeOrderId);
            }

            OrderMessage.CancelOrderMessage cancelOrderMessage = new OrderMessage.CancelOrderMessage();
            cancelOrderMessage.AE = user;
            cancelOrderMessage.Account = account;
            cancelOrderMessage.BrokerID = brokerID;
            cancelOrderMessage.Symbol = order.symbol;
            cancelOrderMessage.OrderID = order.exchangeOrderId;
            cancelOrderMessage.Side = XXX;
            cancelOrderMessage.Data = formatData((int)order.quantity);

            cancelOrderMessage.NID = exchangeConnection.GenerateUniqueID(xxx.Market, OrderMessage.MessageTypeEnum.mtCancel);
            exchangeConnection.CancelOrder(cancelOrderMessage);
        }

        public bool getState()
        {
            return state;
        }

        private string formatData(long qty)
        {
            return String.Format("{0,-7}{1,-20}{2,10}{3:D4}",
                subAccount, localIP, " ", qty);
        }

    }
}
