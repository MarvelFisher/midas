using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Adaptor
{
    public interface IDownStreamManager
    {
        List<IDownStreamAdaptor> adaptors { get; }
        IDownStreamAdaptor getAdaptorById(string id);
    }
}
