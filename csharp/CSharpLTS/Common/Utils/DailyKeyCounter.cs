using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Utils
{
    public class DailyKeyCounter
    {
        private int limit;
        private DateTime currentDate;
        private ConcurrentDictionary<string, int> countMap = new ConcurrentDictionary<string, int>();

        public DailyKeyCounter(int limit)
        {
            currentDate = getToday();
            this.limit = limit;
        }

        public bool check(string key)
        {
            DateTime now = getToday();
            if (DateTime.Compare(currentDate, now) != 0)
            {
                countMap.Clear();
                countMap.TryAdd(key, 1);
                currentDate = now;
                return true;
            }
            if (!countMap.ContainsKey(key))
            {
                countMap.TryAdd(key, 1);
                return true;
            }
            int count = countMap[key];
            if (count >= limit)
            {
                return false;
            }
            countMap[key] = count + 1;
            return true;
        }

        private DateTime getToday()
        {
            return DateTime.Today;
        }

    }
}
