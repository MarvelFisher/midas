
using Avro.Generic;
using Avro.IO;
using Avro.Specific;
using com.cyanspring.avro.generate.@base;
using com.cyanspring.avro.generate.@base.types;
using com.cyanspring.avro.generate.market.bean;
using com.cyanspring.avro.generate.trade.bean;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace Avro.Serialization
{
    
    class AvroSerialization : ISerialization
    {
        private SpecificDatumWriter<ISpecificRecord> writer;
        private SpecificDatumReader<ISpecificRecord> reader;

        public AvroSerialization()
        {           
        }

        public byte[] Serialize(object obj)
        {
            try
            {
                ISpecificRecord record = (ISpecificRecord)obj;
                writer = new SpecificDatumWriter<ISpecificRecord>(record.Schema);
                using(MemoryStream ms = new MemoryStream())
                {
                    int function = (int)record.Get(0);
                    ms.Write(BitConverter.GetBytes(function), 0, 4);
                    BinaryEncoder encoder = new BinaryEncoder(ms);
                    writer.Write(record, encoder);
                    return ms.ToArray();
                }
            } 
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
            }
            return null;
        }

        public object Deserialize(byte[] bytes)
        {
            if (bytes != null)
            {
                try
                {
                    
                    int function = BitConverter.ToInt32(bytes, 0);
                    ObjectType type = (ObjectType)function;
                    Schema schema = staticMap[type];
                    reader = new SpecificDatumReader<ISpecificRecord>(schema, schema);
                    using (MemoryStream ms = new MemoryStream(bytes, 4, bytes.Length - 4,  false))
                    {
                        BinaryDecoder decoder = new BinaryDecoder(ms);
                        return reader.Read(null, decoder);
                    }
                }
                catch (Exception)
                {

                }
            }
            return null;
        }

        private static Dictionary<ObjectType, Schema> staticMap = new Dictionary<ObjectType, Schema>();
        static AvroSerialization()
        {
            staticMap.Add(ObjectType.AmendOrderReply, AmendOrderReply._SCHEMA);
            staticMap.Add(ObjectType.AmendOrderRequest, AmendOrderRequest._SCHEMA);
            staticMap.Add(ObjectType.CancelOrderReply, CancelOrderReply._SCHEMA);
            staticMap.Add(ObjectType.CancelOrderRequest, CancelOrderRequest._SCHEMA);
            staticMap.Add(ObjectType.NewOrderReply, NewOrderReply._SCHEMA);
            staticMap.Add(ObjectType.NewOrderRequest, NewOrderRequest._SCHEMA);
            staticMap.Add(ObjectType.OrderUpdate, OrderUpdate._SCHEMA);
            staticMap.Add(ObjectType.StateUpdate, StateUpdate._SCHEMA);
            staticMap.Add(ObjectType.Quote, Quote._SCHEMA);
            staticMap.Add(ObjectType.SubscribeQuote, SubscribeQuote._SCHEMA);
            staticMap.Add(ObjectType.UnsubscribeQuote, UnsubscribeQuote._SCHEMA);         

        }

        

    }
    
}
