using Common.Adaptor;
using Common.Basic;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Adaptor
{
    class DownStreamManager : IPlugin
    {

        private List<IDownStreamAdaptor> _adaptors;

        public DownStreamManager(List<IDownStreamAdaptor> adaptors)
        {
            this._adaptors = adaptors;
        }

        public void Init()
        {
            //debug
            Console.WriteLine("Init DownStreamManager");
            foreach(IDownStreamAdaptor adaptor in _adaptors )
            {
                adaptor.init();
            }
        }

        public void UnInit()
        {
            foreach (IDownStreamAdaptor adaptor in _adaptors)
            {
                adaptor.addListener(null);
                adaptor.uninit();
            }
        }
    }
}
