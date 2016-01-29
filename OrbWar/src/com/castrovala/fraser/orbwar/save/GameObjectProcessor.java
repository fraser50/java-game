package com.castrovala.fraser.orbwar.save;

import java.util.HashMap;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class GameObjectProcessor {
	private static HashMap<String, GameObjParser> parsers = new HashMap<>();
	
	public static void addParser(String type, GameObjParser parser) {
		parsers.put(type, parser);
	}
	
	public static JSONObject toJSON(GameObject obj) {
		if (!parsers.containsKey(obj.getType())) {
			return null;
		}
		
		GameObjParser parser = parsers.get(obj.getType());
		return parser.toJSON(obj);
	}
	
	public static GameObject fromJSON(JSONObject obj) {
		String type = (String) obj.get("type");
		if (!parsers.containsKey(type)) {
			return null;
		}
		
		GameObjParser parser = parsers.get(type);
		return parser.fromJSON(obj);
	}

}
