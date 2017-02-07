package com.castrovala.fraser.orbwar.net;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.castrovala.fraser.orbwar.server.NoPacketParserException;

public class PacketProcessor {
	//private static List<PacketParser> parsers = new ArrayList<>();
	private static Map<Byte, PacketParser> parserids = new HashMap<>();
	private static Map<Class<? extends AbstractPacket>, PacketParser> parserclasses = new HashMap<>();
	
	public static void addParser(PacketParser parser, Class<? extends AbstractPacket> c) {
		//parsers.add(parser);
		parserids.put(parser.getID(), parser);
		parserclasses.put(c, parser);
		System.out.println("Added " + parser.getID() + " for " + c.getSimpleName());
	}
	
	public static byte[] toBytes(AbstractPacket p) throws NoPacketParserException {
		if (p == null) {
			throw new NullPointerException("Packet can't be null!!!");
		}
		PacketParser parser = parserclasses.get(p.getClass());
		
		
		
		if (parser == null) {
			System.out.println("No parser for " + p.getClass().getSimpleName());
			throw new NoPacketParserException();
			//throw new NullPointerException("Problems");
		}
		
		byte[] data = parser.toBytes(p);
		
		ByteBuffer buf = ByteBuffer.allocate(data.length + 1);
		buf.put(parser.getID());
		buf.put(data);
		return buf.array();
		
	}
	
	public static AbstractPacket fromBytes(byte[] data) {
		byte id = data[0];
		
		ByteBuffer buf = ByteBuffer.allocate(data.length - 1);
		boolean pastfirst = false;
		
		for (byte b : data) {
			if (pastfirst) {
				buf.put(b);
			} else {
				pastfirst = true;
			}
		}
		
		data = buf.array();
		
		if (!parserids.containsKey(id)) {
			System.out.println("No parser for id " + id + " found!");
			return null;
		}
		
		return parserids.get(id).fromBytes(data);
	}

}
