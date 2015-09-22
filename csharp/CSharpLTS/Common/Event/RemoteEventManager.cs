using Common.Transport;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Event
{
    public class RemoteEventManager : IRemoteEventManager
    {
        public IObjectTransportService transport;

        private string channel;

        public RemoteEventManager (IObjectTransportService transport)
        {
            this.transport = transport;
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
    }
}
