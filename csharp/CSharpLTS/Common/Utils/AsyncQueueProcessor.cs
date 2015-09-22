using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Common.Utils
{
    public class AsyncQueueProcessor<T>
    {
        private BlockingCollection<T> queue = new BlockingCollection<T>();
        private Thread thread;

        public delegate void AsyncQueueHandler(T t);
        public event AsyncQueueHandler AsyncQueueEvent;

        public AsyncQueueProcessor(AsyncQueueHandler handler)
        {
            AsyncQueueEvent += handler;
            thread = new Thread(new ThreadStart(this.Run));
        }

        public void init()
        {
            thread.Start(); 
        }

        public void uninit()
        {
            queue.CompleteAdding();
        }

        public void add(T data)
        {
            queue.Add(data);
        }

        private void Run()
        {
            while (!queue.IsCompleted)
            {
                T data = default(T);
                try
                {
                    data = queue.Take();
                }
                catch (InvalidOperationException) { }

                if (data != null && null != AsyncQueueEvent)
                {
                    AsyncQueueEvent(data);
                }
            }
        }
    }
}
