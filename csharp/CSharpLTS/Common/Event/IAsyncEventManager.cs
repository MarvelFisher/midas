using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Event
{
    interface IAsyncEventManager
    {
        bool Subscribe(Type clazz, IAsyncEventListener listener);
    }
}
