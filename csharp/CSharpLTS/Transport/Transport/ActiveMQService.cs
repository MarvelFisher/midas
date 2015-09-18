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
        protected Dictionary<string, IMessageProducer> publisher = new Dictionary<string, IMessageProducer>();
        private Dictionary<string, List<IMessageListener>> subscribers = new Dictionary<string, List<IMessageListener>>();
        private Dictionary<IMessageListener, IMessageConsumer> consumers = new Dictionary<IMessageListener, IMessageConsumer>();

        public event IObjectListener OnMessage;

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
            throw new NotImplementedException();
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
            throw new NotImplementedException();
        }

        public Common.Transport.ISender CreatePublisher(string subject)
        {
            throw new NotImplementedException();
        }

        public void CreateSubscriber(string subject, Common.Transport.IMessageListener listener)
        {
            throw new NotImplementedException();
        }

        public void RemoveSubscriber(string subject, Common.Transport.IMessageListener listener)
        {
            throw new NotImplementedException();
        }

        public void SendMessage(string subject, string message)
        {
            CreateSender(subject).SendMessage(message);
        }

        public void PublishMessage(string subject, string message)
        {
            throw new NotImplementedException();
        }
    }
}
