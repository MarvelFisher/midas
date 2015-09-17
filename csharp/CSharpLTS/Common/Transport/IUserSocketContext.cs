using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    interface IUserSocketContext
    {
        string GetId();
        string GetUser();
        void Send(object obj);
        void Close();
        bool IsOpen();
    }
}
