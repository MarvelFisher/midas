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
using Common.Utils;
using com.cyanspring.avro.generate.trade.types;
using Common.Logging;

namespace Adaptor.TwSpeedy.Main
{
    class TradeAdaptor : IDownStreamAdaptor
    {
        private static ILog logger = LogManager.GetLogger(typeof(TradeAdaptor));


        private IDownStreamListener listener;
        private bool state;
        private ConcurrentDictionary<string, Order> orders = new ConcurrentDictionary<string, Order>();
        private ConcurrentDictionary<long, Order> pendings = new ConcurrentDictionary<long, Order>();

        private ConcurrentDictionary<string, Order> recoverOrders = new ConcurrentDictionary<string, Order>();
        private TaifexConnection exchangeConnection;
        private string localIP;
        private bool recovering;
        public Persistence persistence { set; get; }

        public string id { get; set; } = "YF01";
        public string user { get; set; } = "haida";
        public string account { get; set; } = "1365651";
        public string password { get; set; } = "123456";
        public string subAccount { get; set; } = "1365651";
        public string brokerID { get; set; } = "F018000";
        public string memberID { get; set; } = "F018";
        public OrderMessage.MarketEnum market { get; set; } = MarketEnum.mFutures;
        public string host { get; set; } = "Speedy150.masterlink.com.tw";
        public int port { get; set; } = 23456;
        public bool cancelOrdersAtSTart { get; set; } = true;
        public int maxOrderCount { get; set; } = 1000;
        private DailyKeyCounter placeOrderCount;

        public void init()
        {
            logger.Info("Init TradeAdaptor");
            try
            {
                placeOrderCount = new DailyKeyCounter(maxOrderCount);
                recovering = false;
                state = false;
                localIP = Utils.getLocalIPAddress();
                persistence.init();

                exchangeConnection = new TaifexConnection();
                exchangeConnection.OnConnected += new OrderConnection.ITaifexConnectionEvents_OnConnectedEventHandler(onExchangeConnected);
                exchangeConnection.OnDisconnected += new OrderConnection.ITaifexConnectionEvents_OnDisconnectedEventHandler(onExchangeDisconnected);
                exchangeConnection.OnExecutionReport += new OrderConnection.ITaifexConnectionEvents_OnExecutionReportEventHandler(onExecutionReport);
                exchangeConnection.OnLogonReply += new OrderConnection.ITaifexConnectionEvents_OnLogonReplyEventHandler(onLogonReply);
                exchangeConnection.OnRecoverFinished += new OrderConnection.ITaifexConnectionEvents_OnRecoverFinishedEventHandler(onRecoverFinished);
                connect();
            }
            catch (Exception e)
            {
                logger.Error(e.Message);
                logger.Error(e.StackTrace);
                System.Diagnostics.Debug.WriteLine(e.Message);
                System.Diagnostics.Debug.WriteLine(e.StackTrace);
            }
        }

        public void uninit()
        {
            persistence.uninit();
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
            logger.Info("====================Connected====================");
            login();
        }

        private void onExchangeDisconnected()
        {
            logger.Info("====================Disconnected====================");
        }

        void onLogonReply(string ReplyString, OrderConnection.LogonResultEnum LogonResult, int ConnectionID)
        {
            if (LogonResult == OrderConnection.LogonResultEnum.lrFailed)
            {
                logger.Info("Login failed");
            }
            else if (LogonResult == OrderConnection.LogonResultEnum.lrOK)
            {
                logger.Info("Login succeeded");
                recovering = true;
                exchangeConnection.Recover("031000", OrderConnection.RecoverTypeEnum.rtAll, OrderConnection.RecoverMarketEnum.rmAll);
            }
        }

        void onRecoverFinished(int Count)
        {
            //回補
            logger.Info("====================Recovery Done====================");
            recovering = false;

            if (cancelOrdersAtSTart)
                cancelAllOrders();

            state = true;
            if (null != this.listener)
                this.listener.onState(state);
        }

        void onExecutionReport(OrderMessage.IExecutionReportMessage msg, OrderConnection.ExecDupEnum possDup)
        {
            Utils.printExecutionReport(msg, possDup);
            Order existingOrder;
            if (null != msg.OrderID && msg.OrderID != "")
            {
                if(this.recovering) // during recovery there won't be an order in the cache
                {
                    Order order = new Order(msg.Symbol, "unknown", msg.Price, msg.OrderQty,
                        FieldConverter.convert(msg.Side), FieldConverter.convert(msg.OrderType));

                    if (updateOrder(order, msg))
                    {
                        orders[order.exchangeOrderId] = order;
                        PersistItem item = persistence.getItem(order.exchangeOrderId);
                        if (null == item) // log error here !!!
                        {
                            logger.Error("Error: Cant find order in peristence: " + order.exchangeOrderId);
                            return;
                        }
                        // This is the only reason we need persistence!
                        order.symbol = item.symbol;
                        order.orderId = item.orderId;

                        logger.Info("Recovery add order: " + order);
                    }
                }
                else
                {
                    if (orders.TryGetValue(msg.OrderID, out existingOrder))
                    {
                        if (updateOrder(existingOrder, msg))
                        {
                            persistence.save(existingOrder);
                            this.onOrder(existingOrder);
                        }
                    }
                    else if (msg.NID != 0 && pendings.TryGetValue(msg.NID, out existingOrder))
                    {
                        if (updateOrder(existingOrder, msg))
                        {
                            pendings.TryRemove(msg.NID, out existingOrder);
                            orders[msg.OrderID] = existingOrder;
                            persistence.save(existingOrder);
                            this.onOrder(existingOrder);
                        }
                    }
                    else
                    {
                        logger.Info("Cant find corresponding order in cache");
                    }

                }
            }
            else
            {
                logger.Info("ExecutionReport OrderId is null or empty");
            }
        }

        private bool updateOrder(Order order, IExecutionReportMessage er)
        {
            // stupid protocol...but this is what the API sends...
            if(er.ExecType == ExecTypeEnum.etPartiallyFilled ||
                er.ExecType == ExecTypeEnum.etFilled)
            {
                order.avgPx = (order.avgPx * order.cumQty + er.OrderQty * er.Price) / (order.cumQty + er.OrderQty);
                order.cumQty += er.OrderQty;
            }
            else if(er.ExecType == ExecTypeEnum.etReplaced)
            {
                if (!PriceUtils.isZero(er.OrderQty))
                    order.quantity = er.OrderQty;

                if (!PriceUtils.isZero(er.Price))
                    order.price = er.Price;
            }

            ExecType? execType = FieldConverter.convert(er.ExecType, order);
            OrdStatus? ordStatus = FieldConverter.convert(er.OrderStatus, order);

            if(null != execType && null != ordStatus)
            {
                order.exchangeOrderId = er.OrderID;
                order.ordStatus = ordStatus??order.ordStatus;
                order.execType = execType??order.execType;
                order.lastMsg = er.Text;
                return true;
            }
            return false;
        }

        private void onOrder(Order order)
        {
            if (this.listener != null)
                this.listener.onOrder(order);
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
            if (!placeOrderCount.check(order.symbol)){
                string msg = "Max new order count reached: " + this.maxOrderCount;
                logger.Error(msg);
                throw new Exception(msg);
            }

            if (order.orderId == null)
                throw new DownStreamException("Order id can't be null");

            OrderMessage.NewOrderMessage newOrderMsg = new OrderMessage.NewOrderMessage();
            newOrderMsg.AE = user;
            newOrderMsg.Account = account;
            newOrderMsg.BrokerID = brokerID;
            newOrderMsg.Symbol = order.symbol;

            newOrderMsg.OrderType = FieldConverter.convert(order.orderType);
            newOrderMsg.Side = FieldConverter.convert(order.orderSide);
            newOrderMsg.Price = order.price;
            newOrderMsg.OrderQty = (int)order.quantity;
            newOrderMsg.Market = this.market;
            //TO DO
            if (order.orderType == OrderType.Market)
            {
                newOrderMsg.TimeInForce = OrderMessage.TimeInForceEnum.tifIOC;
            }               
            else
            {
                newOrderMsg.TimeInForce = OrderMessage.TimeInForceEnum.tifROD;
            }                

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

            if (PriceUtils.EqualGreaterThan(qty, order.quantity))
            {
                throw new DownStreamException("Can't amend quantity up: " + exchangeOrderId);
            }

            if (PriceUtils.EqualLessThan(qty, order.cumQty))
            {
                throw new DownStreamException("Can't amend quantity to equal or less than filled quantity: " + exchangeOrderId);
            }

            if (!PriceUtils.Equal(price, order.price) && !PriceUtils.Equal(qty, order.quantity))
            {
                throw new DownStreamException("Can't amend both price and quantity: " + exchangeOrderId);
            }

            if (price == order.price && qty == order.quantity)
            {
                throw new DownStreamException("Can't amend price and qty to same value: " + exchangeOrderId);
            }

            OrderMessage.ReplaceOrderMessage replaceOrderMessage = new OrderMessage.ReplaceOrderMessage();
            replaceOrderMessage.AE = user;
            replaceOrderMessage.Account = account;
            replaceOrderMessage.BrokerID = brokerID;
            replaceOrderMessage.Symbol = order.symbol;
            replaceOrderMessage.OrderID = order.exchangeOrderId;
            replaceOrderMessage.Side = FieldConverter.convert(order.orderSide);
            if(!PriceUtils.Equal(price, order.price))
            {
                replaceOrderMessage.Price = price;
            }

            if(!PriceUtils.Equal(qty, order.quantity))
            {
                replaceOrderMessage.OrderQty = (int)qty;
            }
            replaceOrderMessage.Data = formatData(replaceOrderMessage.OrderQty);

            replaceOrderMessage.NID = exchangeConnection.GenerateUniqueID(this.market, OrderMessage.MessageTypeEnum.mtReplace);
            exchangeConnection.ReplaceOrder(replaceOrderMessage);
        }

        public void cancelOrder(string exchangeOrderId)
        {
            Order order;
            if (!orders.TryGetValue(exchangeOrderId, out order))
            {
                throw new DownStreamException("Cancel order not found: " + exchangeOrderId);
            }

            OrderMessage.CancelOrderMessage cancelOrderMessage = new OrderMessage.CancelOrderMessage();
            cancelOrderMessage.AE = user;
            cancelOrderMessage.Account = account;
            cancelOrderMessage.BrokerID = brokerID;
            cancelOrderMessage.Symbol = order.symbol;
            cancelOrderMessage.OrderID = order.exchangeOrderId;
            cancelOrderMessage.Side = FieldConverter.convert(order.orderSide);
            cancelOrderMessage.Data = formatData((int)order.quantity);

            cancelOrderMessage.NID = exchangeConnection.GenerateUniqueID(this.market, OrderMessage.MessageTypeEnum.mtCancel);
            exchangeConnection.CancelOrder(cancelOrderMessage);
        }

        private void cancelAllOrders()
        {
            logger.Info("Cancelling all orders");
            foreach (KeyValuePair<string, Order> entry in orders)
            {
                Order order = entry.Value;
                if(order.ordStatus == OrdStatus.New ||
                    order.ordStatus == OrdStatus.PartiallyFilled ||
                    order.ordStatus == OrdStatus.Replaced && PriceUtils.LessThan(order.cumQty, order.quantity))
                {
                    logger.Info("Start up cancel order: " + order.orderId + ":" + order.exchangeOrderId);
                    cancelOrder(order.exchangeOrderId);
                }
            }
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
