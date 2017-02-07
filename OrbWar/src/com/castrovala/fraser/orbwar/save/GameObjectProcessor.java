package com.castrovala.fraser.orbwar.save;

import java.util.HashMap;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.world.Position;

public class GameObjectProcessor {
	private static HashMap<String, GameObjParser> parsers = new HashMap<>();
	
	public static void addParser(String type, GameObjParser parser) {
		parsers.put(type, parser);
		System.out.println("Added parser '" + type + "' successfully!");
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject toJSON(GameObject obj) {
		String uuid = obj.getUuid();
		if (!parsers.containsKey(obj.getType())) {
			System.out.println("couldn't find a parser for '" + obj.getType() + "'");
		}
		
		GameObjParser parser = parsers.get(obj.getType());
		JSONObject json = parser.toJSON(obj);
		json.put("uuid", uuid);
		json.put("rotation", obj.getRotation());
		json.put("health", obj.getHealth());
		return json;
	}
	
	public static GameObject fromJSON(JSONObject obj) {
		String type = (String) obj.get("type");
		String uuid = (String) obj.get("uuid");
		Number dx = (Number) obj.get("x");
		Number dy = (Number) obj.get("y");
		double x, y;
		
		x = dx.doubleValue();
		y = dy.doubleValue();
		float rotation = ((Number)obj.get("rotation")).floatValue();
		int health = ((Number)obj.get("health")).intValue();
		if (!parsers.containsKey(type)) {
			return null;
		}
		
		GameObjParser parser = parsers.get(type);
		GameObject g = parser.fromJSON(obj);
		g.setUuid(uuid);
		g.setRotation(rotation);
		g.setHealth(health);
		g.setPosition(new Position(x, y));
		return g;
	}

}
