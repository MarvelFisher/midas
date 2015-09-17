using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface ISocketStateListener
    {
        void OnState(bool connected);
    }
}
