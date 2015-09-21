using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Adaptor.TwSpeedy.Main;
using Common.Adaptor;
using com.cyanspring.avro.generate.trade.types;
using System.Threading;

namespace Adaptor
{
    public partial class TradeTest : Form, IDownStreamListener
    {
        TradeAdaptor adaptor;
        Order order;

        public TradeTest()
        {
            InitializeComponent();
            adaptor = new TradeAdaptor();
            adaptor.addListener(this);
            adaptor.init();
        }

        public void onOrder(Order order)
        {
            this.order = order;
            System.Diagnostics.Debug.WriteLine("Order: " + order);
            this.edExchangeOrderId.BeginInvoke((MethodInvoker)delegate () {
                edExchangeOrderId.Text = order.exchangeOrderId;
            });
        }

        public void onState(bool up)
        {
            this.lbState.BeginInvoke((MethodInvoker)delegate () {
                lbState.Text = "On line";
                lbState.BackColor = Color.Green;
            });

        }

        private void btNew_Click(object sender, EventArgs e)
        {
            Order order = new Order(edSymbol.Text, "Order id",
                Double.Parse(edPrice.Text), Double.Parse(edQuantity.Text),
                (OrderSide)Enum.Parse(typeof(OrderSide), cbSide.Text),
                (OrderType)Enum.Parse(typeof(OrderType), cbType.Text));
            adaptor.newOrder(order);
        }

        private void btAmend_Click(object sender, EventArgs e)
        {
            try
            {
                adaptor.amendOrder(edExchangeOrderId.Text,
                    Double.Parse(edPrice.Text),
                    Double.Parse(edQuantity.Text));
            }
            catch (DownStreamException ex)
            {
                System.Diagnostics.Debug.WriteLine(ex);
                System.Diagnostics.Debug.WriteLine(ex.StackTrace);
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine(ex);
                System.Diagnostics.Debug.WriteLine(ex.StackTrace);
            }
        }

        private void btCancel_Click(object sender, EventArgs e)
        {
            try
            {
                adaptor.cancelOrder(edExchangeOrderId.Text);
            }
            catch (DownStreamException ex)
            {
                System.Diagnostics.Debug.WriteLine(ex);
                System.Diagnostics.Debug.WriteLine(ex.StackTrace);
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine(ex);
                System.Diagnostics.Debug.WriteLine(ex.StackTrace);
            }

        }

        private void TradeTest_FormClosing(object sender, FormClosingEventArgs e)
        {
            adaptor.uninit();
            Thread.Sleep(1000);
        }
    }
}
