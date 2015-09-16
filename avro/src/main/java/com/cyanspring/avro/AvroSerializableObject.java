package com.cyanspring.avro;

import java.io.Serializable;

import org.apache.avro.specific.SpecificRecord;

import com.cyanspring.avro.wrap.WrapObjectType;

public class AvroSerializableObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SpecificRecord record;
	private WrapObjectType objectType;

	public AvroSerializableObject(SpecificRecord record,
			WrapObjectType objectType) {
		super();
		this.record = record;
		this.objectType = objectType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public SpecificRecord getRecord() {
		return record;
	}

	public WrapObjectType getObjectType() {
		return objectType;
	}

}
