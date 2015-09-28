using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Spring.Context;
using Spring.Context.Support;
using Common.Basic;
using Common.Event;
using com.cyanspring.avro.generate.@base;
using com.cyanspring.avro.generate.trade.bean;
using System.Reflection;
using log4net;
using System.ServiceProcess;

namespace CSharpLTS
{
    class Program
    {
        private static ILog logger = LogManager.GetLogger(typeof(Program));       

        public IPlugin downStreamManager { set; get; }
        public IBusniessManager busniessManager { set; get; }

        public static Program server { set; get; }

        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [MTAThread]
        static void Main(string[] args)
        {

            ServiceBase[] ServicesToRun;
            ServicesToRun = new ServiceBase[]
            {
                new ExecutionService()
            };
            ServiceBase.Run(ServicesToRun);
           
        }


        public static void Start()
        {
            IApplicationContext ctx = ContextRegistry.GetContext();

            server = ctx.GetObject("server") as Program;

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
            catch(Exception e)
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

        private void RunGUI()
        {
            App app = new App();
            app.InitializeComponent();
            MainWindow window = new MainWindow();
            app.MainWindow = window;
            app.Run();
        }

    }
}
