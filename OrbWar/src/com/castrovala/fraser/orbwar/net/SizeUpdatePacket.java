package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class SizeUpdatePacket implements AbstractPacket {
	private String uuid;
	private int width;
	private int height;

	public SizeUpdatePacket(String uuid, int width, int height) {
		this.uuid = uuid;
		this.setWidth(width);
		this.setHeight(height);
	}
	
	@Override
	public String getType() {
		return "scale_upd";
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				SizeUpdatePacket packet = (SizeUpdatePacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "scale_upd");
				obj.put("uuid", packet.getUuid());
				obj.put("width", packet.getWidth());
				obj.put("height", packet.getHeight());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				String uuid = (String) obj.get("uuid");
				int width = obj.getAsNumber("width").intValue();
				int height = obj.getAsNumber("height").intValue();
				return new SizeUpdatePacket(uuid, width, height);
			}
		};
		
		PacketProcessor.addParser("scale_upd", parser);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
