using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    //public delegate void IObjectListener(object message); 

    public interface IObjectListener
    {
        void OnMessage(object obj);
    }
}
