using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface IObjectTransportService : ITransportService
    {
        void CreateReceiver(string subject, IObjectListener listener);
        void CreateSubscriber(string subject, IObjectListener listener);
        void RemoveSubscriber(string subject, IObjectListener listener);
        void SendMessage(string subject, object obj);
        void PublishMessage(string subject, object obj);

        IObjectSender CreateObjectSender(string subject);
        IObjectSender CreateObjectPublisher(string subject);
    }
}
