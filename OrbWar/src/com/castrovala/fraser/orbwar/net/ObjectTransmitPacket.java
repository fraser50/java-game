package com.castrovala.fraser.orbwar.net;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ObjectTransmitPacket implements AbstractPacket {
	private JSONObject obj;
	
	public ObjectTransmitPacket(JSONObject obj) {
		this.setObj(obj);
		if (obj == null) {
			throw new NullPointerException("OTP obj was null!");
		}
	}


	public JSONObject getObj() {
		return obj;
	}


	public void setObj(JSONObject obj) {
		this.obj = obj;
	}
	
	public static void registerPacket() {
		PacketParser parser = new PacketParser() {
			@Override
			public byte[] toBytes(AbstractPacket p) {
				ObjectTransmitPacket packet = (ObjectTransmitPacket) p;
				
				packet.getObj().put("x", (double)packet.getObj().get("x"));
				packet.getObj().put("y", (double)packet.getObj().get("y"));
				String objdata = packet.getObj().toJSONString();
				return objdata.getBytes();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				try {
					
					JSONObject jobj = (JSONObject) (new JSONParser()).parse(new String(data));
					ObjectTransmitPacket otp = new ObjectTransmitPacket(jobj);
					return otp;
				} catch (ParseException e) {
					e.printStackTrace();
					throw new NullPointerException("Invalid JSON for OTP!");
				}
			}
		};
		
		PacketProcessor.addParser(parser, ObjectTransmitPacket.class);
		
	}

}
