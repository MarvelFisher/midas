
using Avro.Generic;
using Avro.Specific;
using Common.Transport;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Avro.Serialization
{
    
    class AvroSerialization : ISerialization
    {

        public object Deserialize(object obj)
        {
            throw new NotImplementedException();
        }

        public object Serialize(object obj)
        {
            throw new NotImplementedException();
        }

        private class SchemaManager
        {

            private Dictionary<int, DatumReader<ISpecificRecord>> datumReaderMap;
            private Dictionary<int, DatumWriter<ISpecificRecord>> datumWriterMap;

            private SchemaManager()
            {
                initSchemaMap();
            }

            private void initSchemaMap()
            {
                datumReaderMap = new Dictionary<int, DatumReader<ISpecificRecord>>();
                datumWriterMap = new Dictionary<int, DatumWriter<ISpecificRecord>>();

                //foreach()

            }

        }

    }
    
}
