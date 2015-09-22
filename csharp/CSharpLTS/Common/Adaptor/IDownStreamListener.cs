using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Adaptor
{
    public interface IDownStreamListener
    {
        void onState(bool up);
        void onOrder(Order order);
    }
}
