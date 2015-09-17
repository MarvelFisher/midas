using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Common.Basic;

namespace Common.Transport
{
    interface IClientSocketService : IPlugin 
    {
        bool SendMessage(Object obj);
        bool AddListener(IClientSocketListener listener);
        bool RemoveListener(IClientSocketListener listener);
    }
}
