using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface IServerSocketListener
    {
        void OnConnected(bool connected, IUserSocketContext ctx);
        void OnMessage(object obj, IUserSocketContext ctx);
    }
}
