using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    public interface IMessageListener
    {
        void OnMessage(string message);
    }
}
