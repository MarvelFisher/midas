using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface IClientSocketListener
    {
        void OnConnected(bool connected);
        void OnMessage(object obj);
    }
}
