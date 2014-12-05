package com.cyanspring.transport;

import java.io.Serializable;

import org.junit.Ignore;

@Ignore
public class TestObject implements Serializable {
	public enum Type { Type1, Type2 }
	public Type type;
	public String name;
	public int age;
	public TestObject(Type type, String name, int age) {
		super();
		this.type = type;
		this.name = name;
		this.age = age;
	}
	
}
