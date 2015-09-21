using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    public interface ITransportService
    {
        void StartBroker();
        void CloseBroker();
        void StartService();
        void CloseService();
        ISender CreateSender();
        void CreateReceiver(IMessageListener listener);
        void RemoveReceiver();
        ISender CreatePublisher();
        void CreateSubscriber(IMessageListener listener);
        void RemoveSubscriber(IMessageListener listener);
        void SendMessage(string message);
        void PublishMessage(string message);
    }
}
