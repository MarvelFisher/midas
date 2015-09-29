using Common.Basic;
using Common.Event;
using Common.Logging;
using Spring.Context;
using Spring.Context.Support;
using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceProcess;
using System.Text;
using System.Threading.Tasks;

namespace CSharpLTS
{
    class Server
    {
        private static ILog logger = LogManager.GetLogger(typeof(Program));

        public IPlugin downStreamManager { set; get; }
        public IBusniessManager busniessManager { set; get; }

        public static Server server { set; get; }


        public static void Start()
        {
            IApplicationContext ctx = ContextRegistry.GetContext();

            server = ctx.GetObject("server") as Server;

            logger.Info("Server Start...");

            server.Init();
        }

        public static void Stop()
        {
            logger.Info("Server Stop...");
            server.UnInit();
        }

        public void Init()
        {
            try
            {
                busniessManager.init();
                downStreamManager.Init();
            }
            catch (Exception e)
            {
                logger.Error(e.Message);
            }

        }

        public void UnInit()
        {
            try
            {
                busniessManager.uninit();
                downStreamManager.UnInit();
            }
            catch (Exception e)
            {
                logger.Error(e.Message);
            }
        }
    }
}
