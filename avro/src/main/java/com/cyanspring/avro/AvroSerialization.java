package com.cyanspring.avro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import com.cyanspring.avro.wrap.WrapObjectType;
import com.cyanspring.common.transport.ISerialization;

public class AvroSerialization implements ISerialization {

	private SchemaManager schemaManager;

	public AvroSerialization() {
		schemaManager = new SchemaManager();
	}

	@Override
	public Object serialize(Object obj) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException(
					"avro serialize object is null !");
		}
		if (obj instanceof AvroSerializableObject) {
			AvroSerializableObject avroObject = (AvroSerializableObject) obj;
			int code = avroObject.getObjectType().getCode();
			DatumWriter<SpecificRecord> datumWriter = schemaManager
					.getDatumWriter(code);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
				datumWriter.write(avroObject.getRecord(), encoder);
				encoder.flush();
				byte[] codeByte = intToBytes(code);
				byte[] dataBytes = out.toByteArray();
				byte[] bytes = new byte[codeByte.length + dataBytes.length];
				System.arraycopy(codeByte, 0, bytes, 0, codeByte.length);
				System.arraycopy(dataBytes, 0, bytes, 4, dataBytes.length);
				return bytes;
			} catch (IOException e) {
				log.error("class AvroSerialization method serialize IOException : "
						+ e.getMessage());
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					// ignore close exception
				}
			}
			return null;
		} else {
			throw new IllegalArgumentException(
					"avro serialize object is undefined type !");
		}
	}

	@Override
	public Object deSerialize(Object obj) throws Exception {
		if (obj == null) {
			throw new IllegalArgumentException(
					"avro deSerialize object is null !");
		}
		if (obj instanceof byte[]) {
			byte[] bytes = (byte[]) obj;
			byte[] codeBytes = new byte[4];
			byte[] dataBytes = new byte[bytes.length - 4];
			System.arraycopy(bytes, 0, codeBytes, 0, 4);
			System.arraycopy(bytes, 4, dataBytes, 0, dataBytes.length);
			int code = bytesToInt(codeBytes);
			Decoder decoder = DecoderFactory.get().binaryDecoder(dataBytes,
					null);
			DatumReader<SpecificRecord> datumReader = schemaManager
					.getDatumReader(code);
			try {
				SpecificRecord record = (SpecificRecord) datumReader.read(null,
						decoder);
				return new AvroSerializableObject(record,
						WrapObjectType.valueOf(code));
			} catch (IOException e) {
				log.error("class AvroSerialization method deSerialize IOException : "
						+ e.getMessage());
				return null;
			}
		} else {
			throw new IllegalArgumentException(
					"avro serialize object is not byte array !");
		}
	}

	private int bytesToInt(byte[] bytes) {
		ByteBuffer bf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		bf.put(bytes);
		return bf.getInt(0);
	}

	private byte[] intToBytes(int intValue) {
		ByteBuffer bf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
		bf.putInt(intValue);
		return bf.array();
	}

	/**
	 * inner class
	 * 
	 * @author Phoenix
	 */
	private class SchemaManager {

		private HashMap<Integer, DatumReader<SpecificRecord>> datumReaderMap;

		private HashMap<Integer, DatumWriter<SpecificRecord>> datumWriterMap;

		private SchemaManager() {
			initSchemaMap();
		}

		/**
		 * init Schema HashMap
		 */
		private void initSchemaMap() {
			datumReaderMap = new HashMap<Integer, DatumReader<SpecificRecord>>();
			datumWriterMap = new HashMap<Integer, DatumWriter<SpecificRecord>>();
			for (WrapObjectType objectType : WrapObjectType.values()) {

				datumReaderMap.put(
						objectType.getCode(),
						new SpecificDatumReader<SpecificRecord>(objectType
								.getSchema()));

				datumWriterMap.put(
						objectType.getCode(),
						new SpecificDatumWriter<SpecificRecord>(objectType
								.getSchema()));
			}
		}

		DatumWriter<SpecificRecord> getDatumWriter(Integer code) {
			return datumWriterMap.get(code);
		}

		DatumReader<SpecificRecord> getDatumReader(Integer code) {
			return datumReaderMap.get(code);
		}
	}

}
