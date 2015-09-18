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
            System.Diagnostics.Debug.WriteLine("");
            System.Diagnostics.Debug.WriteLine("========================================");
            System.Diagnostics.Debug.WriteLine("OrderConnection.ExecDupEnum: " + PossDup);
            System.Diagnostics.Debug.WriteLine("Msg.Symbol: " + Msg.Symbol);
            System.Diagnostics.Debug.WriteLine("Msg.ExchangeCode: " + Msg.ExchangeCode);
            System.Diagnostics.Debug.WriteLine("Msg.ExecType: " + Msg.ExecType);
            System.Diagnostics.Debug.WriteLine("Msg.OrderStatus: " + Msg.OrderStatus);
            System.Diagnostics.Debug.WriteLine("Msg.Account: " + Msg.Account);
            System.Diagnostics.Debug.WriteLine("Msg.Market: " + Msg.Market);
            System.Diagnostics.Debug.WriteLine("Msg.NID: " + Msg.NID);
            System.Diagnostics.Debug.WriteLine("Msg.OrderID: " + Msg.OrderID);
            System.Diagnostics.Debug.WriteLine("Msg.ClOrdID: " + Msg.ClOrdID);
            System.Diagnostics.Debug.WriteLine("Msg.CumQty: " + Msg.CumQty);
            System.Diagnostics.Debug.WriteLine("Msg.OrderQty: " + Msg.OrderQty);
            System.Diagnostics.Debug.WriteLine("Msg.Price: " + Msg.Price);
            System.Diagnostics.Debug.WriteLine("Msg.Side: " + Msg.Side);
            System.Diagnostics.Debug.WriteLine("Msg.TimeInForce: " + Msg.TimeInForce);
            System.Diagnostics.Debug.WriteLine("Msg.LastPx: " + Msg.LastPx);
            System.Diagnostics.Debug.WriteLine("Msg.LastQty: " + Msg.LastQty);
            System.Diagnostics.Debug.WriteLine("Msg.LeavesQty: " + Msg.LeavesQty);
            System.Diagnostics.Debug.WriteLine("Msg.PriceBase: " + Msg.PriceBase);
            System.Diagnostics.Debug.WriteLine("Msg.OrdRejReason: " + Msg.OrdRejReason);
            System.Diagnostics.Debug.WriteLine("Msg.CxlRejResponseTo: " + Msg.CxlRejResponseTo);
            System.Diagnostics.Debug.WriteLine("Msg.Text: " + Msg.Text);
            System.Diagnostics.Debug.WriteLine("Msg.Data: " + Msg.Data);
            System.Diagnostics.Debug.WriteLine("========================================");
            System.Diagnostics.Debug.WriteLine("");
        }
    }
}
