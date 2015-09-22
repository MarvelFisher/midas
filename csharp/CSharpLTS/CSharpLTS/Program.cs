using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Spring.Context;
using Spring.Context.Support;
using Log;
using Common.Basic;
using Common.Event;

namespace CSharpLTS
{
    class Program
    {

        public IPlugin downStreamManager { set; get; }
        public IRemoteEventManager remoteEventManager { set; get; }

        /// <summary>
        /// 应用程序的主入口点。
        /// </summary>
        [STAThread]
        static void Main(string[] args)
        {

            IApplicationContext ctx = ContextRegistry.GetContext();

            Console.WriteLine("Start Server...");
            Program server = ctx.GetObject("server") as Program;
            server.Init();

            Console.Read();
        }

        public void Init()
        {
            Console.WriteLine("Init...");
            downStreamManager.Init();
            remoteEventManager.init();
           

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
