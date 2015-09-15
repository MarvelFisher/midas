package com.cyanspring.transport.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import com.cyanspring.common.transport.ISerialization;

public class FastSerialization implements ISerialization {

	@Override
	public Object serialize(Object obj) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException(
					"fast serialize object is null !");
		}
		ByteArrayOutputStream byteArrayOutputStream = null;
		FSTObjectOutput out = null;
		try {
			// stream closed in the finally
			byteArrayOutputStream = new ByteArrayOutputStream(512);
			// 32000 buffer size
			out = new FSTObjectOutput(byteArrayOutputStream);
			out.writeObject(obj);
			out.flush();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			return null;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}
	}

	@Override
	public Object deSerialize(Object obj) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException(
					"fast deSerialize object is null !");
		}
		if (obj instanceof byte[]) {
			byte[] bytes = (byte[]) obj;
			ByteArrayInputStream byteArrayInputStream = null;
			FSTObjectInput in = null;
			try {
				byteArrayInputStream = new ByteArrayInputStream(bytes);
				in = new FSTObjectInput(byteArrayInputStream);
				return in.readObject();
			} catch (ClassNotFoundException e) {
				log.error("class AvroSerialization method serialize ClassNotFoundException : "
						+ e.getMessage());
				return null;
			} catch (IOException e) {
				log.error("class AvroSerialization method serialize IOException : "
						+ e.getMessage());
				return null;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (byteArrayInputStream != null) {
						byteArrayInputStream.close();
					}
				} catch (IOException ex) {
					// ignore close exception
				}
			}
		} else {
			throw new IllegalArgumentException(
					"fast deSerialize object is not byte array !");
		}
	}
}
