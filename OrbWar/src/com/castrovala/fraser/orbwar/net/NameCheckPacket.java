package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public class NameCheckPacket implements AbstractPacket {
	private boolean login;
	private String name;
	
	public NameCheckPacket(boolean login, String name) {
		this.login = login;
		this.name = name;
	}

	@Override
	public String getType() {
		return "ncp";
	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			
			@Override
			public JSONObject toJSON(AbstractPacket p) {
				NameCheckPacket ncp = (NameCheckPacket) p;
				JSONObject obj = new JSONObject();
				obj.put("type", "ncp");
				obj.put("login", ncp.isLogin());
				obj.put("name", ncp.getName());
				
				return obj;
			}
			
			@Override
			public AbstractPacket fromJSON(JSONObject obj) {
				NameCheckPacket ncp = new NameCheckPacket((boolean)obj.get("login"), obj.getAsString("name"));
				return ncp;
			}
		};
		
		PacketProcessor.addParser("ncp", parser);
	}

}
