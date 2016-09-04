package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class ShipDataPacket implements AbstractPacket {
	private String shipid;
	private String name;
	private boolean admin;
	
	public ShipDataPacket(String name, String shipid, boolean admin) {
		this.name = name;
		this.shipid = shipid;
		this.admin = admin;
	}

	@Override
	public String getType() {
		return "supp";
	}

	public String getShipid() {
		return shipid;
	}

	public void setShipid(String shipid) {
		this.shipid = shipid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				ShipDataPacket supp = (ShipDataPacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "supp");
				obj.put("uuid", supp.getShipid());
				obj.put("name", supp.getName());
				obj.put("admin", supp.isAdmin());
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				ShipDataPacket supp = new ShipDataPacket(obj.getAsString("uuid"), obj.getAsString("uuid"), (boolean)obj.get("admin"));
				supp.setName(obj.getAsString("name"));
				return supp;
			}
		};
		
		PacketProcessor.addParser("supp", parser);
	}

}
