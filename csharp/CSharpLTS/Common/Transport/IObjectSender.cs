using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    public interface IObjectSender
    {
        void SendMessage(object obj);
    }
}
