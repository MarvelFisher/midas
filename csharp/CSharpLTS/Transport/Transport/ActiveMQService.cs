using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Common.Transport;
using Apache.NMS.ActiveMQ;
using Apache.NMS;

namespace Transport.Transport
{
    class ActiveMQService : ITransportService
    {
        // ActiveMQ configuration parameters
        private string user;
        private string password;
        private string url = "nio://localhost:61616";
        protected MsgDeliveryMode persistent;
        private bool transacted;
        private AcknowledgementMode ackMode;
        private long memoryLimit = 128 * 1024 * 1024;

        // members
        protected IConnection connection;
        protected ISession session;

        protected Dictionary<string, IMessageConsumer> receivers = new Dictionary<string, IMessageConsumer>();
        protected Dictionary<string, IMessageProducer> senders = new Dictionary<string,IMessageProducer>();
        protected Dictionary<string, IMessageProducer> publishers = new Dictionary<string, IMessageProducer>();
        private Dictionary<string, List<IMessageListener>> subscribers = new Dictionary<string, List<IMessageListener>>();
        private Dictionary<IMessageListener, IMessageConsumer> consumers = new Dictionary<IMessageListener, IMessageConsumer>();

        public event IMessageListener OnMessage;
        private IMessageListener OnXXX;

        class Sender : ISender
        {
            private ActiveMQService service_;
            private IMessageProducer producer_;
            public Sender(ActiveMQService service, IMessageProducer producer)
            {
                this.service_ = service;
                this.producer_ = producer;
            }

            public void SendMessage(string message)
            {
                ITextMessage txt = service_.session.CreateTextMessage(message);
                producer_.Send(txt);
            }
        }

        public void StartBroker()
        {
            throw new NotImplementedException();
        }

        public void CloseBroker()
        {
            throw new NotImplementedException();
        }

        public void StartService()
        {
            ConnectionFactory connectionFactory = new ConnectionFactory(url);
            connection = connectionFactory.CreateConnection(user, password);
            connection.Start();
            connection.ExceptionListener += new ExceptionListener(connection_ExceptionListener);
            session = connection.CreateSession(ackMode);
        }

        private void connection_ExceptionListener(Exception e)
        {
            // log exception
        }

        public void CloseService()
        {
            removeAllReceivers();
            session.Close();
            connection.Close();
        }

        private void removeAllReceivers() {
            foreach (IMessageConsumer cum in receivers.Values)
            {
                cum.Listener -= consumer_MessageListener;
            }
	    }

        public Common.Transport.ISender CreateSender(string subject)
        {
            if (!senders.ContainsKey(subject))
            {
                IDestination dest = session.GetQueue(subject);
                IMessageProducer newProducer = session.CreateProducer(dest);
                newProducer.DeliveryMode = persistent;
                senders.Add(subject, newProducer);
            }
            IMessageProducer producer = senders[subject];
            return new Sender(this, producer);
        }

        public void CreateReceiver(string subject, Common.Transport.IMessageListener listener)
        {
            if (!receivers.ContainsKey(subject))
            {
                IDestination dest = session.GetQueue(subject);
                IMessageConsumer newConsumer = session.CreateConsumer(dest);
                receivers.Add(subject, newConsumer);
            }
            IMessageConsumer consumer = receivers[subject];
            if (listener == null)
            {
                consumer.Listener -= consumer_MessageListener;
            }
            else
            {
                consumer.Listener += new MessageListener(consumer_MessageListener);
            }
        }

        private void consumer_MessageListener(IMessage message)
        {
            if (message is ITextMessage)
            {
                OnMessage.Invoke(((ITextMessage)message).Text);
            }
            else
            {
                // log error
            }
        }

        public void RemoveReceiver(string subject)
        {
            if (!receivers.ContainsKey(subject))
            {
                return;
            }
            IMessageConsumer consumer = receivers[subject];
            consumer.Listener -= consumer_MessageListener;
        }

        public Common.Transport.ISender CreatePublisher(string subject)
        {
            if (!publishers.ContainsKey(subject)) {
                IDestination dest = session.GetTopic(subject);
                IMessageProducer newProducer = session.CreateProducer(dest);
                newProducer.DeliveryMode = persistent;
                publishers.Add(subject, newProducer);
            }
            IMessageProducer producer = publishers[subject];
            return new Sender(this, producer);
        }

        public void CreateSubscriber(string subject, Common.Transport.IMessageListener listener)
        {
            if (!subscribers.ContainsKey(subject)) 
            {
                subscribers.Add(subject, new List<IMessageListener>());
            }
            List<IMessageListener> listeners = subscribers[subject];
            if (!listeners.Contains(listener))
            {
                IDestination dest = session.GetTopic(subject);
                IMessageConsumer consumer = session.CreateConsumer(dest);
                consumer.Listener += new MessageListener(consumer_MessageListener);
                listeners.Add(listener);
                consumers[listener] = consumer;
            }
            
        }

        public void RemoveSubscriber(string subject, Common.Transport.IMessageListener listener)
        {
            if (!subscribers.ContainsKey(subject))
            {
                return;
            }
            List<IMessageListener> listeners = subscribers[subject];
            if (listeners.Contains(listener))
            {
                if (!consumers.ContainsKey(listener))
                {
                    return;
                }
                IMessageConsumer consumer = consumers[listener];
                consumer.Listener -= consumer_MessageListener;
                consumers.Remove(listener);
                listeners.Remove(listener);
            }
        }

        public void SendMessage(string subject, string message)
        {
            CreateSender(subject).SendMessage(message);
        }

        public void PublishMessage(string subject, string message)
        {
            CreatePublisher(subject).SendMessage(message);
        }
    }
}
