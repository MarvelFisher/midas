package com.cyanspring.transport.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.transport.ISerialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * 
 * @author Phoenix
 *
 */
public class GsonSerialization implements ISerialization {

	Gson gson = new Gson();

	JsonParser jsonParser = new JsonParser();

	private Logger log = LoggerFactory.getLogger(FastSerialization.class);

	@Override
	public Object serialize(Object obj) throws IllegalArgumentException {
		String className = obj.getClass().getName();
		log.debug("serialize class is : " + className);
		JsonElement jsonTemp = new JsonPrimitive(className);
		JsonElement jsonTree = gson.toJsonTree(obj);
		jsonTree.getAsJsonObject().add("class", jsonTemp);
		return jsonTree.toString();
	}

	@Override
	public Object deSerialize(Object obj) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException(
					"flex deSerialize object is null !");
		} else if (obj instanceof String) {
			String json = (String) obj;
			if ("".equals(json)) {
				return null;
			}
			JsonElement jsonElement = jsonParser.parse(json);
			JsonElement className = jsonElement.getAsJsonObject().get("class");
			jsonElement.getAsJsonObject().remove("class");
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className.getAsString());
			} catch (ClassNotFoundException e) {
				log.error(className.getAsString() + "is not find!");
			}
			return gson.fromJson(json, clazz);
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
