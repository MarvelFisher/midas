using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Adaptor
{
    public interface IDownStreamAdaptor
    {
        void init();
        void uninit();
        string id { get; }
        bool getState();
        void newOrder(Order order);
        void amendOrder(string exchangeOrderId, double price, double qty);
        void cancelOrder(string exchangeOrderId);
        void addListener(IDownStreamListener listener);
        void removeListener(IDownStreamListener listener);
    }
}
