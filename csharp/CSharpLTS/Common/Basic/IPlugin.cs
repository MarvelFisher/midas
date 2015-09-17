using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Basic
{
    interface IPlugin
    {
        void Init();
        void UnInit();
    }
}
