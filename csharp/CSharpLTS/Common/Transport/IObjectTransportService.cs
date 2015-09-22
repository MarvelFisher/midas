using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    public interface IObjectTransportService : ITransportService
    {
        void CreateReceiver(IObjectListener listener);
        void CreateSubscriber(IObjectListener listener);
        void RemoveSubscriber(IObjectListener listener);
        void SendMessage(object obj);
        void PublishMessage(object obj);

        IObjectSender CreateObjectSender();
        IObjectSender CreateObjectPublisher();
    }
}
