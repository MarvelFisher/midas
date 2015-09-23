using Avro.Specific;
using com.cyanspring.avro.generate.@base;
using com.cyanspring.avro.generate.@base.types;
using com.cyanspring.avro.generate.market.bean;
using com.cyanspring.avro.generate.trade.bean;
using com.cyanspring.avro.generate.trade.types;
using Common.Adaptor;
using Common.Transport;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Timers;

namespace Common.Event
{
    public class RemoteEventManager : IRemoteEventManager
    {
        public IObjectTransportService transport { set; get; }

        public IDownStreamManager downStreamManager { set; get; }

        private Timer timer;


        public RemoteEventManager (IObjectTransportService transport)
        {
            this.transport = transport;
            timer = new Timer();
            timer.Interval = 5000; 
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
            // debug
            Console.WriteLine("Init RemoteEventManager");
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
            private RemoteEventManager _manager;
            private IDownStreamAdaptor _adaptor;

            public AdaptorListener(RemoteEventManager remoteEventManager, IDownStreamAdaptor adaptor)
            {
                this._manager = remoteEventManager;
                this._adaptor = adaptor;
            }

            public void onOrder(Order order)
            {
                OrderUpdate update = new OrderUpdate();
                update.orderId = order.orderId;
                update.orderSide = (int)order.orderSide;
                update.orderType = (int)order.orderType;
                update.ordStatus = (int)order.ordStatus;
                update.price = order.price;
                update.quantity = order.quantity;
                update.symbol = order.symbol;
                update.timeInForce = (int)order.timeInForce;

                update.exchangeOrderId = order.exchangeOrderId;
                update.avgPx = order.avgPx;
                update.clOrderId = order.clOrderId;
                update.created = order.created;
                update.cumQty = order.cumQty;
                update.exchangeAccount = order.exchangeAccount;
                update.execType = (int)order.execType;

                _manager.Publish(update);

            }

            public void onState(bool up)
            {
                Console.WriteLine("Connect Ready, " + (up ? "Timer Enable" : "Timer Disable"));
                // enable timer to send StateUpdate
                _manager.timer.Enabled = up;
            }
        }


        private class AvroEventProcess : IObjectListener
        {
            private RemoteEventManager _manager;
            public AvroEventProcess(RemoteEventManager manager)
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
                            IDownStreamAdaptor adaptor = _manager.downStreamManager.getAdaptorById(req.exchangeAccount);
                            adaptor.amendOrder(req.orderId, req.price, req.quantity);
                            AmendOrderReply rsp = new AmendOrderReply();
                            rsp.result = true;
                            rsp.orderId = req.orderId;
                            _manager.Publish(rsp);
                            break;
                        }
                    case ObjectType.CancelOrderRequest:
                        {
                            CancelOrderRequest req = (CancelOrderRequest)avroObj;
                            IDownStreamAdaptor adaptor = _manager.downStreamManager.getAdaptorById(req.exchangeAccount);
                            adaptor.cancelOrder(req.orderId);
                            CancelOrderReply rsp = new CancelOrderReply();
                            rsp.result = true;
                            rsp.orderId = req.orderId;
                            _manager.Publish(rsp);
                            break;
                        }
                    case ObjectType.NewOrderRequest:
                        {
                            NewOrderRequest req = (NewOrderRequest)avroObj;
                            IDownStreamAdaptor adaptor = _manager.downStreamManager.getAdaptorById(req.exchangeAccount);
                            Order order = new Order(req.symbol, req.orderId, req.price, req.quantity, (OrderSide)req.orderSide, (OrderType)req.orderType);
                            adaptor.newOrder(order);
                            NewOrderReply rsp = new NewOrderReply();
                            rsp.result = true;
                            rsp.orderId = req.orderId;
                            _manager.Publish(rsp);
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
                            return;
                        }
                }
            }
        }


    }


}
