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

namespace CSharpLTS
{
    class Program
    {
        private static ILog logger = LogManager.GetLogger(typeof(Program));       

        public IPlugin downStreamManager { set; get; }
        public IBusniessManager busniessManager { set; get; }

        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [STAThread]
        static void Main(string[] args)
        {            

            IApplicationContext ctx = ContextRegistry.GetContext();
            
            Program server = ctx.GetObject("server") as Program;

            logger.Error("Server Start...");
            
            server.Init();

            Console.Read();
        }

        public void Init()
        {
            busniessManager.init();
            downStreamManager.Init();


            //NewOrderReply ev1 = new NewOrderReply();           

            //StateUpdate ev = new StateUpdate();
            //ev.exchangeAccount = "ex1";

            //remoteEventManager.Publish(ev);
            //remoteEventManager.Publish(ev);
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
