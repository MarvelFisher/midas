using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Avro.Serialization
{
    public interface ISerialization
    {
        byte[] Serialize(object obj);
        object Deserialize(byte[] bytes);
    }
}
