package com.castrovala.fraser.orbwar.item;

import java.util.HashMap;
import java.util.Map;

public class ItemProcessor {
	private static Map<Short, ItemParser> parserids = new HashMap<>();
	private static Map<Class<? extends ItemParser>, ItemParser> parserclasses = new HashMap<>();
	
	public static void addParser(ItemParser parser, Class<? extends ItemParser> c) {
				parserids.put(parser.getID(), parser);
				parserclasses.put(c, parser);
				System.out.println("[Item] Added " + parser.getID() + " for " + c.getSimpleName());
	}

}
