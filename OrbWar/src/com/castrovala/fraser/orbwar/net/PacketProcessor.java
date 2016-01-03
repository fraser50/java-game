package com.castrovala.fraser.orbwar.net;

import java.util.HashMap;

import org.json.simple.JSONObject;

public class PacketProcessor {
	private static HashMap<String, PacketParser> parsers = new HashMap<>();
	
	public static void addParser(String type, PacketParser parser) {
		parsers.put(type, parser);
	}
	
	public static JSONObject toJSON(AbstractPacket p) {
		if (!parsers.containsKey(p.getType())) {
			return null;
		}
		
		PacketParser parser = parsers.get(p.getType());
		return parser.toJSON(p);
	}
	
	public static AbstractPacket fromJSON(JSONObject obj) {
		String type = (String) obj.get("type");
		if (!parsers.containsKey(type)) {
			return null;
		}
		
		PacketParser parser = parsers.get(type);
		return parser.fromJSON(obj);
	}

}
