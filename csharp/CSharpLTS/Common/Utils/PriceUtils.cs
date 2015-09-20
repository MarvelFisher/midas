using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Common.Utils
{
    public class PriceUtils
    {
        private static readonly int scale = 7;
        private static readonly double roundingFactor = Math.Pow(10, (double)scale);
        private static readonly double EPSILON = 1.0 / roundingFactor;

        static public bool Equal(double x, double y)
        {
            return Math.Abs(x - y) < EPSILON;
        }

        static public bool GreaterThan(double x, double y)
        {
            return x - y > EPSILON;
        }

        static public bool LessThan(double x, double y)
        {
            return y - x > EPSILON;
        }

        static public bool EqualGreaterThan(double x, double y)
        {
            return Equal(x, y) || GreaterThan(x, y);
        }

        static public bool EqualLessThan(double x, double y)
        {
            return Equal(x, y) || LessThan(x, y);
        }

        static public int Compare(double x, double y)
        {
            if (Equal(x, y))
                return 0;

            if (GreaterThan(x, y))
                return 1;
            else
                return -1;
        }

        static public bool validPrice(double price)
        {
            return GreaterThan(price, 0);
        }

        static public bool isZero(double x)
        {
            return PriceUtils.Equal(x, 0);
        }
    }
}
