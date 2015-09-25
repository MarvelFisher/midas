using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Utils
{
    class StringUtils
    {
        public static string trim(string str)
        {
            if (str == null)
            {
                return "";
            }
            return str.Trim();
        }
    }
}
