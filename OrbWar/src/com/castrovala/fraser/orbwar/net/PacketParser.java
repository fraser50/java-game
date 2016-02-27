package com.castrovala.fraser.orbwar.net;

import net.minidev.json.JSONObject;

public interface PacketParser {
	public JSONObject toJSON(AbstractPacket p);
	public AbstractPacket fromJSON(JSONObject obj);

}
