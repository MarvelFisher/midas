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
using System.IO;

namespace CSharpLTS
{
    class Program
    {
        private static ILog logger = LogManager.GetLogger(typeof(Program));       

        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [MTAThread]
        static void Main(string[] args)
        {
            System.IO.Directory.SetCurrentDirectory(System.AppDomain.CurrentDomain.BaseDirectory);
            
#if DEBUG
            Server.Start();
            Console.ReadLine();
            Server.Stop();
#else
            ServiceBase[] ServicesToRun;
            ServicesToRun = new ServiceBase[]
            {
                new ExecutionService()
            };
            ServiceBase.Run(ServicesToRun);
#endif


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
