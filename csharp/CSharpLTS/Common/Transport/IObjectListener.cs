using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface IObjectListener
    {
        void OnMessage(object obj);
    }
}
