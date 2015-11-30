package com.cyanspring.transport.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.transport.ISerialization;
import com.google.gson.Gson;

/**
 * 
 * @author Phoenix
 *
 */
public class GsonSerialization implements ISerialization {

	private Gson gson = new Gson();
	
	private Logger log = LoggerFactory.getLogger(FastSerialization.class);

	private class InnerWrapObj {
		String className;
		String jsonStr;
	}

	@Override
	public Object serialize(Object obj) throws IllegalArgumentException {
		String className = obj.getClass().getName();
		log.debug("serialize class is : " + className);
		String jsonStr = gson.toJson(obj);
		InnerWrapObj innerWrapObj = new InnerWrapObj();
		innerWrapObj.className = className;
		innerWrapObj.jsonStr = jsonStr;
		return gson.toJson(innerWrapObj);
	}

	@Override
	public Object deSerialize(Object obj) throws IllegalArgumentException,
			ClassNotFoundException {
		if (obj == null) {
			throw new IllegalArgumentException(
					"Gson deSerialize object is null !");
		} else if (obj instanceof String) {
			String json = (String) obj;
			if ("".equals(json)) {
				throw new IllegalArgumentException(
						"Gson deSerialize json is empty string !");
			}
			InnerWrapObj innerWrapObj = gson.fromJson(json, InnerWrapObj.class);
			Class<?> clazz = null;
			try {
				clazz = Class.forName(innerWrapObj.className);
			} catch (ClassNotFoundException e) {
				log.error(innerWrapObj.className + "is not find!");
				throw new ClassNotFoundException(
						"Gson deSerialize class is null !");
			}
			return gson.fromJson(innerWrapObj.jsonStr, clazz);
		} else {
			throw new IllegalArgumentException(
					"flex deSerialize object is not string !");
		}
	}

	// public static void main(String[] args) {
	//
	// GsonSerialization serialization = new GsonSerialization();
	//
	// Person person = new Person();
	// person.setName("JinPeng");
	// person.setAge(28);
	// person.setSex(Person.Sex.Male);
	// Node node = new Node();
	// node.setT1("te1");
	// node.setT2("te2");
	// person.setTestList(Arrays.asList(0.3D, 5.68D, 7.89D));
	// ConcurrentHashMap<Integer, String> testMap = new
	// ConcurrentHashMap<Integer, String>();
	// testMap.put(1, "t1");
	// testMap.put(2, "t2");
	// person.setTestMap(testMap);
	// person.setNode(node);
	//
	// Object json = serialization.serialize(person);
	//
	// System.out.println(json.getClass());
	// System.out.println(json.toString());
	//
	// Object obj = serialization.deSerialize(json);
	//
	// System.out.println(obj.getClass());
	//
	// Person p = (Person) obj;
	//
	// System.out.println(p.getTestList().getClass());
	// System.out.println(p.getTestMap().getClass());
	//
	// System.out.println(obj.toString());
	//
	// }

}
