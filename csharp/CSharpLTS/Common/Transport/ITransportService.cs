using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface ITransportService
    {
        void StartBroker();
        void CloseBroker();
        void StartService();
        void CloseService();
        ISender CreateSender(string subject);
        void CreateReceiver(string subject, IMessageListener listener);
        void RemoveReceiver(string subject);
        ISender CreatePublisher(string subject);
        void CreateSubscriber(string subject, IMessageListener listener);
        void RemoveSubscriber(string subject, IMessageListener listener);
        void SendMessage(string subject, string message);
        void PublishMessage(string subject, string message);
    }
}
