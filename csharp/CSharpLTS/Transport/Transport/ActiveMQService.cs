using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Common.Transport;
using Apache.NMS.ActiveMQ;

namespace Transport.Transport
{
    class ActiveMQService : ITransportService
    {
        // ActiveMQ configuration parameters
        private string user;
        private string password;
        private string url = "nio://localhost:61616";
        protected int persistent;
        private bool transacted;
        private int ackMode;
        private long memoryLimit = 128 * 1024 * 1024;

        // members
        BrokerService broker;


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
            throw new NotImplementedException();
        }

        public void CloseService()
        {
            throw new NotImplementedException();
        }

        public Common.Transport.ISender CreateSender(string subject)
        {
            throw new NotImplementedException();
        }

        public void CreateReceiver(string subject, Common.Transport.IMessageListener listener)
        {
            throw new NotImplementedException();
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
            throw new NotImplementedException();
        }

        public void PublishMessage(string subject, string message)
        {
            throw new NotImplementedException();
        }
    }
}
