using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using com.cyanspring.avro.generate.trade.types;
using OrderMessage;
using Common.Adaptor;
using Common.Utils;

namespace Adaptor.TwSpeedy.Main
{
    public class FieldConverter
    {
        public static OrderTypeEnum convert(OrderType orderType)
        {
            switch(orderType)
            {
                case OrderType.Limit:
                    return OrderTypeEnum.otLimit;
                case OrderType.Market:
                    return OrderTypeEnum.otMarket;
            }
            throw new DownStreamException("OrderType not support: " + orderType);
        }

        public static OrderType convert(OrderTypeEnum orderType)
        {
            switch (orderType)
            {
                case OrderTypeEnum.otLimit:
                    return OrderType.Limit;
                case OrderTypeEnum.otMarket:
                    return OrderType.Market;
            }
            throw new DownStreamException("OrderType not support: " + orderType);
        }

        public static SideEnum convert(OrderSide orderSide)
        {
            switch(orderSide)
            {
                case OrderSide.Buy:
                    return SideEnum.sBuy;
                case OrderSide.Sell:
                    return SideEnum.sSell;
            }
            throw new DownStreamException("OrderType not support: " + orderSide);
        }

        public static OrderSide convert(SideEnum orderSide)
        {
            switch (orderSide)
            {
                case SideEnum.sBuy:
                    return OrderSide.Buy;
                case SideEnum.sSell:
                    return OrderSide.Sell;
            }
            throw new DownStreamException("OrderType not support: " + orderSide);
        }

        public static ExecType? convert(ExecTypeEnum execType, Order order)
        {
            switch(execType)
            {
                case ExecTypeEnum.etCanceled:
                    return ExecType.Canceled;

                case ExecTypeEnum.etNew:
                    return ExecType.New;

                case ExecTypeEnum.etReplaced:
                    return ExecType.Replace;

                case ExecTypeEnum.etRejected:
                    return ExecType.Rejected;

                case ExecTypeEnum.etPartiallyFilled:
                    if (PriceUtils.EqualGreaterThan(order.cumQty, order.quantity))
                        return ExecType.Filled;
                    else
                        return ExecType.PartiallyFilled;
            }

            return null;
        }

        public static OrdStatus? convert(OrderStatusEnum ordStatus, Order order)
        {
            switch (ordStatus)
            {
                case OrderStatusEnum.osCanceled:
                    return OrdStatus.Canceled;

                case OrderStatusEnum.osNew:
                    return OrdStatus.New;

                case OrderStatusEnum.osReplaced:
                    return OrdStatus.Replaced;

                case OrderStatusEnum.osRejected:
                    if (order.ordStatus == OrdStatus.PendingNew)
                        return OrdStatus.Rejected;

                    return order.ordStatus;

                case OrderStatusEnum.osPartiallyFilled:
                    if (PriceUtils.EqualGreaterThan(order.cumQty, order.quantity))
                        return OrdStatus.Filled;
                    else
                        return OrdStatus.PartiallyFilled;
            }

            return null;
        }
    }
}
