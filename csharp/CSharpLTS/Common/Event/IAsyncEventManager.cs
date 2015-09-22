using Common.Transport;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Event
{
    public interface IAsyncEventManager
    {
        void Subscribe(IObjectListener listener);
        void Receive(IObjectListener listener);
        void Publish(object ev);
        void SendEvent(object ev);
    }
}
