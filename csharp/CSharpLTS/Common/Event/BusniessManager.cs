using Avro.Specific;
using com.cyanspring.avro.generate.@base;
using com.cyanspring.avro.generate.@base.types;
using com.cyanspring.avro.generate.market.bean;
using com.cyanspring.avro.generate.trade.bean;
using com.cyanspring.avro.generate.trade.types;
using Common.Adaptor;
using Common.Logging;
using Common.Transport;
using Common.Utils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Timers;

namespace Common.Event
{
    public class BusniessManager : IBusniessManager
    {
        private static ILog logger = LogManager.GetLogger(typeof(BusniessManager));

        public IObjectTransportService transport { set; get; }

        public IDownStreamManager downStreamManager { set; get; }

        public long timerInterval { set; get; } = 5000;

        private Timer timer;


        public BusniessManager (IObjectTransportService transport)
        {
            this.transport = transport;
            timer = new Timer();
            timer.Interval = timerInterval; 
            timer.Elapsed += timer_SendAdaptorStatus;
        }

        private void timer_SendAdaptorStatus(object sender, ElapsedEventArgs e)
        {
            //Task.Factory.StartNew(() => { SendAdaptorStatus(); });
            SendAdaptorStatus();

        }

        void SendAdaptorStatus()
        {
            foreach (IDownStreamAdaptor adaptor in downStreamManager.adaptors)
            {
                StateUpdate ev = new StateUpdate();
                ev.exchangeAccount = adaptor.id;
                ev.online = adaptor.getState();
                Publish(ev);
            }
        }

        public void init()
        {
            logger.Info("Init BusniessManager...");
            if (transport == null)
            {
                throw new Exception("Transport isn't instantiated");
            }
            transport.StartService();
            Subscribe(new AvroEventProcess(this));
            foreach (IDownStreamAdaptor adaptor in downStreamManager.adaptors)
            {
                adaptor.addListener(new AdaptorListener(this, adaptor));
            }
            timer.Enabled = true;
        }

        void transportReady(bool ready)
        {

        }

        public void SendEvent(object ev)
        {
            transport.SendMessage(ev);
        }

        public void Publish(object ev)
        {
            transport.PublishMessage(ev);
        }

        public void Subscribe(IObjectListener listener)
        {
            transport.CreateSubscriber(listener);
        }

        public void Receive(IObjectListener listener)
        {
            transport.CreateReceiver(listener);
        }

        public void uninit()
        {
            transport.CloseService();
        }


        private class AdaptorListener : IDownStreamListener
        {
            private BusniessManager _manager;
            private IDownStreamAdaptor _adaptor;

            public AdaptorListener(BusniessManager remoteEventManager, IDownStreamAdaptor adaptor)
            {
                this._manager = remoteEventManager;
                this._adaptor = adaptor;
            }

            public void onOrder(Order order)
            {
                OrderUpdate update = new OrderUpdate();
                update.orderId = StringUtils.trim(order.orderId);
                update.orderSide = (int)order.orderSide;
                update.orderType = (int)order.orderType;
                update.ordStatus = (int)order.ordStatus;
                update.price = order.price;
                update.quantity = order.quantity;
                update.symbol = StringUtils.trim(order.symbol);
                update.timeInForce = (int)order.timeInForce;

                update.exchangeOrderId = StringUtils.trim(order.exchangeOrderId);
                update.avgPx = order.avgPx;
                update.clOrderId = StringUtils.trim(order.clOrderId);
                update.created = StringUtils.trim(order.created);
                update.cumQty = order.cumQty;
                update.exchangeAccount = StringUtils.trim(_adaptor.id);
                update.execType = (int)order.execType;
                
                _manager.Publish(update);
                logger.Info("Send OrderUpdate: " + order.ToString());

            }

            public void onState(bool up)
            {
                logger.Info("Connect Ready, " + (up ? "Timer Enable" : "Timer Disable"));
                // enable timer to send StateUpdate
                _manager.timer.Enabled = up;
            }
        }


        private class AvroEventProcess : IObjectListener
        {
            private BusniessManager _manager;
            public AvroEventProcess(BusniessManager manager)
            {
                this._manager = manager;
            }

            public void OnMessage(object obj)
            {
                if (!(obj is ISpecificRecord))
                {
                    return;
                }
                ISpecificRecord avroObj = (ISpecificRecord)obj;
                ObjectType type = (ObjectType)avroObj.Get(0);
                switch (type)
                {
                    case ObjectType.AmendOrderRequest:
                        {
                            
                            AmendOrderRequest req = (AmendOrderRequest)avroObj;
                            logger.Info("Get AmendOrderRequest: " + req.ToString());
                            IDownStreamAdaptor adaptor = _manager.downStreamManager.getAdaptorById(req.exchangeAccount);
                            processAmendOrder(adaptor, req);
                            
                            break;
                        }
                    case ObjectType.CancelOrderRequest:
                        {                           
                            CancelOrderRequest req = (CancelOrderRequest)avroObj;
                            logger.Info("Get CancelOrderRequest: " + req.ToString());
                            IDownStreamAdaptor adaptor = _manager.downStreamManager.getAdaptorById(req.exchangeAccount);
                            processCancelOrder(adaptor, req);

                            break;
                        }
                    case ObjectType.NewOrderRequest:
                        {                           
                            NewOrderRequest req = (NewOrderRequest)avroObj;
                            logger.Info("Get NewOrderRequest: " + req.ToString());
                            IDownStreamAdaptor adaptor = _manager.downStreamManager.getAdaptorById(req.exchangeAccount);
                            processNewOrder(adaptor, req);

                            break;
                        }
                    case ObjectType.SubscribeQuote:
                        {
                            SubscribeQuote req = (SubscribeQuote)avroObj;
                            
                            break;
                        }
                    case ObjectType.UnsubscribeQuote:
                        {
                            break;
                        }
                    default:
                        {
                            //Log Error
                            logger.Error("Undefined ObjectType: " + type);
                            return;
                        }
                }
            }

            private void processAmendOrder(IDownStreamAdaptor adaptor, AmendOrderRequest req)
            {
                if (adaptor != null)
                {
                    try
                    {
                        adaptor.amendOrder(req.orderId, req.price, req.quantity);
                        AmendOrderReply rsp = new AmendOrderReply();
                        rsp.result = true;
                        rsp.orderId = req.orderId;
                        rsp.exchangeAccount = req.exchangeAccount;
                        rsp.message = "sucess";
                        _manager.Publish(rsp);
                    }
                    catch (Exception e)
                    {
                        AmendOrderReply rsp = new AmendOrderReply();
                        rsp.result = false;
                        rsp.orderId = req.orderId;
                        rsp.message = e.Message;
                        _manager.Publish(rsp);
                    }
                }
                else
                {
                    AmendOrderReply rsp = new AmendOrderReply();
                    rsp.result = false;
                    rsp.orderId = req.orderId;
                    rsp.message = req.exchangeAccount + " not exist";
                    _manager.Publish(rsp);
                }
            }

            private void processCancelOrder(IDownStreamAdaptor adaptor, CancelOrderRequest req)
            {
                if (adaptor != null)
                {
                    try
                    {
                        adaptor.cancelOrder(req.orderId);
                        CancelOrderReply rsp = new CancelOrderReply();
                        rsp.result = true;
                        rsp.orderId = req.orderId;
                        rsp.exchangeAccount = req.exchangeAccount;
                        rsp.message = "success";
                        _manager.Publish(rsp);
                    }
                    catch(Exception e)
                    {
                        CancelOrderReply rsp = new CancelOrderReply();
                        rsp.result = false;
                        rsp.orderId = req.orderId;
                        rsp.exchangeAccount = req.exchangeAccount;
                        rsp.message = e.Message;
                        _manager.Publish(rsp);
                    }
                    
                }
                else
                {
                    CancelOrderReply rsp = new CancelOrderReply();
                    rsp.result = false;
                    rsp.orderId = req.orderId;
                    rsp.exchangeAccount = req.exchangeAccount;
                    rsp.message = req.exchangeAccount + " not exist";
                    _manager.Publish(rsp);
                }
                
            }

            private void processNewOrder(IDownStreamAdaptor adaptor, NewOrderRequest req)
            {
                if (adaptor != null)
                {
                    try
                    {
                        Order order = new Order(req.symbol, req.orderId, req.price, req.quantity, (OrderSide)req.orderSide, (OrderType)req.orderType);
                        adaptor.newOrder(order);
                        NewOrderReply rsp = new NewOrderReply();
                        rsp.result = true;
                        rsp.orderId = req.orderId;
                        rsp.exchangeAccount = req.exchangeAccount;
                        rsp.message = "sucess";
                        _manager.Publish(rsp);
                    }
                    catch(Exception e)
                    {
                        NewOrderReply rsp = new NewOrderReply();
                        rsp.result = false;
                        rsp.orderId = req.orderId;
                        rsp.exchangeAccount = req.exchangeAccount;
                        rsp.message = e.Message;
                        _manager.Publish(rsp);
                    }
                }
                else
                {
                    NewOrderReply rsp = new NewOrderReply();
                    rsp.result = false;
                    rsp.orderId = req.orderId;
                    rsp.exchangeAccount = req.exchangeAccount;
                    rsp.message = req.exchangeAccount + " not exist";
                    _manager.Publish(rsp);
                }
               
            }

        }


    }


}
