using Apache.NMS;
using Common.Transport;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Transport.Transport
{
    class ActiveMQGenericService : ActiveMQService, IObjectTransportService
    {

        public ISerialization defaultSerialization { set; get; }

        public Dictionary<string, ISerialization> serializationMap { set; get; } = new Dictionary<string, ISerialization>();

        private Dictionary<string, List<IObjectListener>> objSubscribers = new Dictionary<string, List<IObjectListener>>();

        private Dictionary<IObjectListener, IMessageConsumer> objConsumers = new Dictionary<IObjectListener, IMessageConsumer>();


        class ObjectSender : IObjectSender
        {
            private ActiveMQGenericService _service;
            private IMessageProducer _producer;
            private ISerialization _serialization;

            public ObjectSender(ActiveMQGenericService service, IMessageProducer producer, ISerialization serialization)
            {
                this._service = service;
                this._producer = producer;
                this._serialization = serialization;
            }

            public void SendMessage(object obj)
            {
                if (obj == null)
                {
                    return;
                }
                object sobj = _serialization.Serialize(obj);
                if (sobj is byte[])
                {
                    byte[] bytes = (byte[])sobj;
                    if (bytes == null || bytes.Length == 0)
                    {
                        return;
                    }
                    IBytesMessage message = _service.session.CreateBytesMessage();
                    message.WriteBytes(bytes);
                    _producer.Send(message);

                }
                else if (sobj is string)
                {
                    string str = (string)sobj;
                    ITextMessage txt = _service.session.CreateTextMessage();
                    _producer.Send(txt);
                }
                else
                {
                    throw new Exception("obj is undefined type!");
                }
            }
        }

        public IObjectSender CreateObjectPublisher(string subject)
        {
            IMessageProducer producer = null;
            if (publishers.ContainsKey(subject))
            {
                producer = publishers[subject];
            }
            else
            {
                IDestination dest = session.GetTopic(subject);
                producer = session.CreateProducer(dest);
                producer.DeliveryMode = persistent;
                senders.Add(subject, producer);              
            }
            ISerialization serialization = getSerializationInstance(subject);           
            return new ObjectSender(this, producer, serialization);
        }

        public IObjectSender CreateObjectSender(string subject)
        {
            IMessageProducer producer = null;
            if (senders.ContainsKey(subject))
            {
                producer = senders[subject];
            } 
            else
            {
                IDestination dest = session.GetQueue(subject);
                producer = session.CreateProducer(dest);
                producer.DeliveryMode = persistent;
                senders.Add(subject, producer);
            }
            ISerialization serialization = getSerializationInstance(subject);
            return new ObjectSender(this, producer, serialization);
        }
        
        public void CreateReceiver(string subject, IObjectListener listener)
        {
            // only one listener per subject allowed for point to point connection
            IMessageConsumer consumer = null;
            if (receivers.ContainsKey(subject))
            {
                consumer = receivers[subject];
            }
            else
            {
                IDestination dest = session.GetQueue(subject);
                consumer = session.CreateConsumer(dest);
                receivers.Add(subject, consumer);
            }
            if ( listener == null )
            {
                consumer.Listener -= Consumer_Listener1;
            }
            consumer.Listener += new MessageListener(Consumer_Listener1);
        }

        /**
       * think about how to process this method
       */
        private void Consumer_Listener1(IMessage message)
        {
            
        }

        public void CreateSubscriber(string subject, IObjectListener listener)
        {
            // many listeners per subject allowed for publish/subscribe
            List<IObjectListener> listeners = null;
            if (objSubscribers.ContainsKey(subject))
            {
                listeners = objSubscribers[subject];
            }
            else
            {
                listeners = new List<IObjectListener>();
                objSubscribers.Add(subject, listeners);
            }
            if (!listeners.Contains(listener))
            {
                IDestination dest = session.GetTopic(subject);
                IMessageConsumer consumer = session.CreateConsumer(dest);
                ISerialization serialization = getSerializationInstance(subject);
                consumer.Listener += new MessageListener(Consumer_Listener2);
                listeners.Add(listener);
                objConsumers.Add(listener, consumer);
            } 
            
        }

        private void Consumer_Listener2(IMessage message)
        {
            
        }

        public void PublishMessage(string subject, object obj)
        {
            CreateObjectPublisher(subject).SendMessage(obj);
        }

        public void RemoveSubscriber(string subject, IObjectListener listener)
        {
            // many listeners per subject allowed for publish/subscribe
            if (!objSubscribers.ContainsKey(subject))
            {
                return;              
            }
            List<IObjectListener>  listeners = objSubscribers[subject];
            if (listeners.Contains(listener))
            {
                if (!objConsumers.ContainsKey(listener))
                {
                    return;
                }
                IMessageConsumer consumer = objConsumers[listener];
                consumer.Listener -= Consumer_Listener2;
                objConsumers.Remove(listener);
                listeners.Remove(listener);
            }
        }

        public void SendMessage(string subject, object obj)
        {
            CreateObjectSender(subject).SendMessage(obj);
        }

        private ISerialization getSerializationInstance(string topic)
        {
            if (topic == null)
            {
                return defaultSerialization;
            }
            if (serializationMap.ContainsKey(topic))
            {
                return serializationMap[topic];
            }
            else
            {
                return defaultSerialization;
            }
        }

    }
}
