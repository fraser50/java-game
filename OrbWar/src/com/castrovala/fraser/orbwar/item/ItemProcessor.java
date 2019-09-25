package com.castrovala.fraser.orbwar.item;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.castrovala.fraser.orbwar.server.NoItemParserException;

public class ItemProcessor {
	private static Map<Short, ItemParser> parserids = new HashMap<>();
	private static Map<Class<? extends Item>, ItemParser> parserclasses = new HashMap<>();
	
	public static void addParser(ItemParser parser, Class<? extends Item> c) {
				parserids.put(parser.getID(), parser);
				parserclasses.put(c, parser);
				System.out.println("[Item] Added " + parser.getID() + " for " + c.getSimpleName());
	}
	
	public static byte[] toBytes(Item item) throws NoItemParserException {
		
		ItemParser parser = parserclasses.get(item.getClass());
		
		if (parser == null) {
			System.out.println("[Item] No parser for " + item.getClass().getSimpleName());
			throw new NoItemParserException();
		}
		
		byte[] data = parser.toBytes(item);
		
		ByteBuffer buf = ByteBuffer.allocate(data.length + 1);
		buf.putShort(parser.getID());
		buf.put(data);
		return buf.array();
	}
	
	public static Item fromBytes(byte[] data) {
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.put(data[0]);
		buf.put(data[1]);
		buf.position(0);
		short id = buf.getShort();
		
		ItemParser parser = parserids.get(id);
		if (parser == null) {
			return null;
		}
		
		buf = ByteBuffer.allocate(data.length - 2);
		for (int i = 0; i<data.length;i++) {
			if (i < 2) continue;
			buf.put(data[i]);
		}
		
		buf.position(0);
		return parser.fromBytes(buf);
		
	}

}
