using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Adaptor
{
    public class DownStreamException : Exception
    {
        public DownStreamException(string message) : base(message)
        {
        }
    }
}
