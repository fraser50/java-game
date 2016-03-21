package com.castrovala.fraser.orbwar.net;

import com.castrovala.fraser.orbwar.util.Position;

import net.minidev.json.JSONObject;

public class ScreenUpdatePacket implements AbstractPacket {
	private Position pos;
	
	public ScreenUpdatePacket(Position pos) {
		this.pos = pos;
	}

	@Override
	public String getType() {
		return "sup";
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				ScreenUpdatePacket sup = (ScreenUpdatePacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", sup.getType());
				obj.put("x", sup.getPos().getX());
				obj.put("y", sup.getPos().getY());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				double x = (double) obj.get("x");
				double y = (double) obj.get("y");
				return new ScreenUpdatePacket(new Position(x, y));
			}
		};
		
		PacketProcessor.addParser("sup", parser);
	}

}
