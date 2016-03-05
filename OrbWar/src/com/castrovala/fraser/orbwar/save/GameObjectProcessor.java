package com.castrovala.fraser.orbwar.save;

import java.util.HashMap;

import net.minidev.json.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class GameObjectProcessor {
	private static HashMap<String, GameObjParser> parsers = new HashMap<>();
	
	public static void addParser(String type, GameObjParser parser) {
		parsers.put(type, parser);
		System.out.println("Added parser '" + type + "' successfully!");
	}
	
	public static JSONObject toJSON(GameObject obj) {
		String uuid = obj.getUuid();
		if (!parsers.containsKey(obj.getType())) {
			return null;
		}
		
		GameObjParser parser = parsers.get(obj.getType());
		JSONObject json = parser.toJSON(obj);
		json.put("uuid", uuid);
		return json;
	}
	
	public static GameObject fromJSON(JSONObject obj) {
		String type = (String) obj.get("type");
		String uuid = (String) obj.get("uuid");
		if (!parsers.containsKey(type)) {
			return null;
		}
		
		GameObjParser parser = parsers.get(type);
		GameObject g = parser.fromJSON(obj);
		g.setUuid(uuid);
		return g;
	}

}
