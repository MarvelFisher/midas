using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Transport
{
    public interface ISerialization
    {
        byte[] Serialize(object obj);
        object Deserialize(byte[] bytes);
    }
}
