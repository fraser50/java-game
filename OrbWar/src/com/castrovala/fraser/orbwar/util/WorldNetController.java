package com.castrovala.fraser.orbwar.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;

public class WorldNetController implements WorldProvider {
	private SocketChannel channel;
	private List<WorldZone> zones = new ArrayList<>();
	
	public WorldNetController(String host, int port) {
		//super();
		
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(host, port));
			
			while (!channel.finishConnect()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public WorldNetController() {
		this("127.0.0.1", 5555);
	}

	@Override
	public WorldZone getZone(Position pos) {
		for (WorldZone zone : zones) {
			if (zone.x == pos.x && zone.y == pos.y) {
				return zone;
			}
		}
		WorldZone zone = new WorldZone((long)pos.x, (long)pos.y, this);
		zones.add(zone);
		return zone;
	}

	@Override
	public List<WorldZone> getZones() {
		return zones;
	}

	@Override
	public List<Position> getStarpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addObject(GameObject o) {
		Position zonepos = Util.toZoneCoords(o.getPosition());
		WorldZone zone = getZone(zonepos);
		zone.getGameobjects().add(o);
		
	}

	@Override
	public HashMap<GameObject, Float> getScanners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GameObject[] allObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isServer() {
		return true;
	}

	@Override
	public void updateGame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GameObject getGameObject(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void processPackets() throws IOException {
		List<AbstractPacket> packets = new ArrayList<>();
		ByteBuffer buff = ByteBuffer.allocate(65536);
		if (channel.read(buff) < 1) {
			return;
		}
		
		String value = new String(buff.array());
		System.out.println("Client: " + value);
		while (value.trim() != "") {
			String length_str = value.substring(0, 4);
			System.out.println("Len str: " + length_str);
			byte[] length_bytes = length_str.getBytes();
			int length = ByteBuffer.wrap(length_bytes).getInt();
			
			if (length == 0) {
				break;
			}
			
			System.out.println("Length: " + length);
			String json_str = value.substring(4, 4 + length);
			System.out.println("JSON str: " + json_str);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			JSONParser parser = new JSONParser();
			JSONObject jobj;
			try {
				jobj = (JSONObject) parser.parse(json_str);
				System.out.println("parsed string fine");
				AbstractPacket packet = PacketProcessor.fromJSON(jobj);
				
				if (packet == null) {
					System.out.println("Packet is null, no parser exists!");
				}
				
				packets.add(packet);
				System.out.println("Name: " + packet.getClass().getName());
				System.out.println("Added packet to list");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			value = value.substring(4 + length, value.length());
		}
		
		System.out.println("Packet count: " + packets.size());
		for (AbstractPacket pack : packets) {
			System.out.println("Packet class name: " + pack.getClass().getName());
			if (pack instanceof ObjectTransmitPacket) {
				ObjectTransmitPacket otp = (ObjectTransmitPacket) pack;
				GameObject obj = GameObjectProcessor.fromJSON(otp.getObj());
				obj.setController(this);
				addObject(obj);
				System.out.println("Received object from server");
			}
		}
		
	}
	

}
