package com.castrovala.fraser.orbwar.net;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EditorTransmitPacket implements AbstractPacket {
	private JSONObject obj;
	
	public EditorTransmitPacket(JSONObject obj) {
		this.setObj(obj);
		if (obj == null) {
			throw new NullPointerException("ETP obj was null!");
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
				EditorTransmitPacket etp = (EditorTransmitPacket) p;
				
				etp.getObj().put("x", (double)etp.getObj().get("x"));
				etp.getObj().put("y", (double)etp.getObj().get("y"));
				String objdata = etp.getObj().toJSONString();
				return objdata.getBytes();
			}
			
			@Override
			public AbstractPacket fromBytes(byte[] data) {
				try {
					
					JSONObject jobj = (JSONObject) (new JSONParser()).parse(new String(data));
					EditorTransmitPacket etp = new EditorTransmitPacket(jobj);
					return etp;
				} catch (ParseException e) {
					e.printStackTrace();
					throw new NullPointerException("Invalid JSON for ETP!");
				}
			}
		};
		
		PacketProcessor.addParser(parser, EditorTransmitPacket.class);
		
	}

}
