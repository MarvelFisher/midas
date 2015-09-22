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

        private IObjectListener objRecListener;


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

        public IObjectSender CreateObjectPublisher()
        {
            IMessageProducer producer = null;
            if (publishers.ContainsKey(publisherTopic))
            {
                producer = publishers[publisherTopic];
            }
            else
            {
                IDestination dest = session.GetTopic(publisherTopic);
                producer = session.CreateProducer(dest);
                producer.DeliveryMode = persistent;
                senders.Add(publisherTopic, producer);              
            }
            ISerialization serialization = getSerializationInstance(publisherTopic);           
            return new ObjectSender(this, producer, serialization);
        }

        public IObjectSender CreateObjectSender()
        {
            IMessageProducer producer = null;
            if (senders.ContainsKey(senderTopic))
            {
                producer = senders[senderTopic];
            } 
            else
            {
                IDestination dest = session.GetQueue(senderTopic);
                producer = session.CreateProducer(dest);
                producer.DeliveryMode = persistent;
                senders.Add(senderTopic, producer);
            }
            ISerialization serialization = getSerializationInstance(senderTopic);
            return new ObjectSender(this, producer, serialization);
        }
        
        public void CreateReceiver( IObjectListener listener)
        {
            // only one listener per subject allowed for point to point connection
            IMessageConsumer consumer = null;
            if (receivers.ContainsKey(senderTopic))
            {
                consumer = receivers[senderTopic];
            }
            else
            {
                IDestination dest = session.GetQueue(senderTopic);
                consumer = session.CreateConsumer(dest);
                receivers.Add(senderTopic, consumer);
            }
            if ( listener == null )
            {
                consumer.Listener -= Consumer_Listener1;
            }
            else
            {
                this.objRecListener = listener;
                consumer.Listener += new MessageListener(Consumer_Listener1);
            }
            
        }
        
        private void Consumer_Listener1(IMessage message)
        {
            //if (message is ITextMessage)
            //{
            //    string str = ((ITextMessage)message).Text;
            //    object obj = getSerializationInstance(senderTopic).Deserialize<object>(str);
            //    objRecListener.OnMessage(obj);
            //}
            if (message is IBytesMessage)
            {
                IBytesMessage bms = (IBytesMessage) message;
                int length = (int)bms.BodyLength;
                if (length == 0)
                {
                    // log error
                    return;
                }
                byte[] bytes = new byte[length];
                bms.ReadBytes(bytes);
                object obj = getSerializationInstance(senderTopic).Deserialize(bytes);
                if (obj == null)
                {
                    return;
                }
                objRecListener.OnMessage(obj);
            }
            else
            {
                // log error
            }
        }

        public void CreateSubscriber( IObjectListener listener)
        {
            // many listeners per subject allowed for publish/subscribe
            List<IObjectListener> listeners = null;
            if (objSubscribers.ContainsKey(publisherTopic))
            {
                listeners = objSubscribers[publisherTopic];
            }
            else
            {
                listeners = new List<IObjectListener>();
                objSubscribers.Add(publisherTopic, listeners);
            }
            if (!listeners.Contains(listener))
            {
                IDestination dest = session.GetTopic(publisherTopic);
                IMessageConsumer consumer = session.CreateConsumer(dest);
                ISerialization serialization = getSerializationInstance(publisherTopic);
                consumer.Listener += new MessageListener(Consumer_Listener2);
                listeners.Add(listener);
                objConsumers.Add(listener, consumer);
            } 
            
        }

        private void Consumer_Listener2(IMessage message)
        {
            //if (message is ITextMessage)
            //{
            //    string str = ((ITextMessage)message).Text;
            //    object obj = getSerializationInstance(publisherTopic).Deserialize(str);
            //    foreach(IObjectListener listener in objSubscribers[publisherTopic])
            //    {
            //        listener.OnMessage(obj);
            //    }
                
            //}
            if (message is IBytesMessage)
            {
                IBytesMessage bms = (IBytesMessage)message;
                int length = (int)bms.BodyLength;
                if (length == 0)
                {
                    // log error
                    return;
                }
                byte[] bytes = new byte[length];
                bms.ReadBytes(bytes);
                object obj = getSerializationInstance(publisherTopic).Deserialize(bytes);
                if (obj == null)
                {
                    return;
                }
                foreach (IObjectListener listener in objSubscribers[publisherTopic])
                {
                    listener.OnMessage(obj);
                }
            }
            else
            {
                // log error
            }
        }

        public void PublishMessage( object obj)
        {
            CreateObjectPublisher().SendMessage(obj);
        }

        public void RemoveSubscriber( IObjectListener listener)
        {
            // many listeners per subject allowed for publish/subscribe
            if (!objSubscribers.ContainsKey(publisherTopic))
            {
                return;              
            }
            List<IObjectListener>  listeners = objSubscribers[publisherTopic];
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

        public void SendMessage( object obj)
        {
            CreateObjectSender().SendMessage(obj);
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
