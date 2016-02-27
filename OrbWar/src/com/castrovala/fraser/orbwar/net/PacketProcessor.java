package com.castrovala.fraser.orbwar.net;

import java.util.HashMap;

import net.minidev.json.JSONObject;

public class PacketProcessor {
	private static HashMap<String, PacketParser> parsers = new HashMap<>();
	
	public static void addParser(String type, PacketParser parser) {
		parsers.put(type, parser);
	}
	
	public static JSONObject toJSON(AbstractPacket p) {
		if (!parsers.containsKey(p.getType())) {
			//System.out.println("Parser not available");
			//System.out.println("Packet type name: " + p.getType());
			return null;
		}
		
		//System.out.println("Looking for parser with name '" + p.getType() + "'");
		long start = System.currentTimeMillis();
		PacketParser parser = parsers.get(p.getType());
		long end = System.currentTimeMillis();
		long delay = end - start;
		if (delay >= 2) {
			System.out.println("Found packet processor in " + delay + "ms");
		}
		
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
