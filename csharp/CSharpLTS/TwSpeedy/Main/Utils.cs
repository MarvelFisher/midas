using Common.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace TwSpeedy.Utils
{
    class Utils
    {
        private static ILog logger = LogManager.GetLogger(typeof(Utils));

        public static string getLocalIPAddress()
        {
            var host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (var ip in host.AddressList)
            {
                if (ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                {
                    return ip.ToString();
                }
            }
            return "0.0.0.0";
        }

        public static void printExecutionReport(OrderMessage.IExecutionReportMessage Msg, OrderConnection.ExecDupEnum PossDup)
        {
            logger.Info("");
            logger.Info("========================================");
            logger.Info("OrderConnection.ExecDupEnum: " + PossDup);
            logger.Info("Msg.Symbol: " + Msg.Symbol);
            logger.Info("Msg.ExchangeCode: " + Msg.ExchangeCode);
            logger.Info("Msg.ExecType: " + Msg.ExecType);
            logger.Info("Msg.OrderStatus: " + Msg.OrderStatus);
            logger.Info("Msg.Account: " + Msg.Account);
            logger.Info("Msg.Market: " + Msg.Market);
            logger.Info("Msg.NID: " + Msg.NID);
            logger.Info("Msg.OrderID: " + Msg.OrderID);
            logger.Info("Msg.ClOrdID: " + Msg.ClOrdID);
            logger.Info("Msg.CumQty: " + Msg.CumQty);
            logger.Info("Msg.OrderQty: " + Msg.OrderQty);
            logger.Info("Msg.Price: " + Msg.Price);
            logger.Info("Msg.Side: " + Msg.Side);
            logger.Info("Msg.TimeInForce: " + Msg.TimeInForce);
            logger.Info("Msg.LastPx: " + Msg.LastPx);
            logger.Info("Msg.LastQty: " + Msg.LastQty);
            logger.Info("Msg.LeavesQty: " + Msg.LeavesQty);
            logger.Info("Msg.PriceBase: " + Msg.PriceBase);
            logger.Info("Msg.OrdRejReason: " + Msg.OrdRejReason);
            logger.Info("Msg.CxlRejResponseTo: " + Msg.CxlRejResponseTo);
            logger.Info("Msg.Text: " + Msg.Text);
            logger.Info("Msg.Data: " + Msg.Data);
            logger.Info("Msg.src: " + Msg.src);
            logger.Info("Msg.TransactTime: " + Msg.TransactTime);
            logger.Info("========================================");
            logger.Info("");

        }
    }
}
