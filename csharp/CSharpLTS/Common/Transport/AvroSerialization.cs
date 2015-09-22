
using Avro.Generic;
using Avro.IO;
using Avro.Specific;
using com.cyanspring.avro.generate.@base.types;
using Common.Transport;
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
            catch(Exception)
            {

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

                    using (MemoryStream ms = new MemoryStream(bytes, false))
                    {
                        BinaryDecoder decoder = new BinaryDecoder(ms);

                    }
                }
                catch (Exception)
                {

                }
            }
            return null;
        }



        

    }
    
}
