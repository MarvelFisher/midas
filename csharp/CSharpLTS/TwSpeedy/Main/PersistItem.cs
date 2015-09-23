﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using com.cyanspring.avro.generate.trade.types;

namespace Adaptor.TwSpeedy.Main
{
    class PersistItem
    {
        public DateTime time { get; set; }
        public string exchangeOrderId { get; set; }
        public string orderId { get; set; }
        public string symbol { get; set; }
        public string ordStatus { get; set; }

        public PersistItem(DateTime time, string exchangeOrderId, string orderId, string symbol, string ordStatus)
        {
            this.time = time;
            this.exchangeOrderId = exchangeOrderId;
            this.orderId = orderId;
            this.symbol = symbol;
            this.ordStatus = ordStatus;
        }

        public static PersistItem deserialize(string str)
        {
            string[] list = str.Split(':');
            if (list.Length != 5)
                return null;

            return new PersistItem(DateTime.Parse(list[0]),
                list[1], list[2], list[3], list[4]);
            
        }

        public string serialize()
        {
            return
            String.Format("{0:yyyy-MM-dd}", this.time) + ':' +
            exchangeOrderId + ':' +
            orderId + ':' +
            symbol + ':' +
            ordStatus;
        }
    }
}