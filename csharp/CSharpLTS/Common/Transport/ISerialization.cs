using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    public interface ISerialization
    {
        object Serialize(object obj);
        object Deserialize(object obj);
    }
}
