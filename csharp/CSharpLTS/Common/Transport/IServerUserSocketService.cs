using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Common.Basic;

namespace Common.Transport
{
    public interface IServerUserSocketService : IPlugin
    {
        IUserSocketContext GetContext(string key);
        List<IUserSocketContext> GetContextByUser(string user);
        void SetUserContext(string user, IUserSocketContext ctx);
        bool AddListener(IServerSocketListener listener);
        bool RemoveListener(IServerSocketListener listener);
    }
}
