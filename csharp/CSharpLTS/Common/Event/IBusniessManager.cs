using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Event
{
    public interface IBusniessManager : IAsyncEventManager
    {
        void init();
        void uninit();

    }
}
