package com.castrovala.fraser.orbwar.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.castrovala.fraser.orbwar.client.ClientPlayer;
import com.castrovala.fraser.orbwar.client.ServerMessage;
import com.castrovala.fraser.orbwar.gameobject.Asteroid;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.ChatEnterPacket;
import com.castrovala.fraser.orbwar.net.DeleteObjectPacket;
import com.castrovala.fraser.orbwar.net.HealthUpdatePacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.net.PositionUpdatePacket;
import com.castrovala.fraser.orbwar.net.ScreenUpdatePacket;
import com.castrovala.fraser.orbwar.net.ShipDataPacket;
import com.castrovala.fraser.orbwar.net.ShipRemovePacket;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class WorldNetController implements WorldProvider {
	private SocketChannel channel;
	private List<WorldZone> zones = new ArrayList<>();
	private HashMap<String, GameObject> objids = new HashMap<>();
	private ByteBuffer receiveBuffer;
	private ByteBuffer receiveBufferLen;
	private int objectcount;
	private Position pos;
	private HashMap<String, ClientPlayer> clients = new HashMap<>();
	private List<ServerMessage> messages = new ArrayList<>();
	
	public WorldNetController(String host, int port, Position pos) throws IOException {
		//super();
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(host, port));
		
		while (!channel.finishConnect()) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.pos = pos;
	}
	
	public WorldNetController(Position pos) throws IOException {
		this("127.0.0.1", 5555, pos);
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
		return null;
	}

	@Override
	public void addObject(GameObject o) {
		Position zonepos = Util.toZoneCoords(o.getPosition());
		WorldZone zone = getZone(zonepos);
		zone.getGameobjects().add(o);
		objids.put(o.getUuid(), o);
		
	}

	@Override
	public HashMap<GameObject, Float> getScanners() {
		return null;
	}

	@Override
	public GameObject[] allObjects() {
		return null;
	}

	@Override
	public boolean isServer() {
		return false;
	}

	@Override
	public void updateGame() {
		for (WorldZone zone : zones.toArray(new WorldZone[zones.size()])) {
			for (GameObject obj : zone.getGameobjects().toArray(new GameObject[zone.getGameobjects().size()])) {
				
				WorldZone ozone = getZone(Util.toZoneCoords(obj.getPosition()));
				
				if (ozone != zone) {
					zone.getGameobjects().remove(obj);
					ozone.getGameobjects().add(obj);
				}
				
				if (obj.isDeleted() || obj.isCleaned()) {
					zone.getGameobjects().remove(obj);
					objids.remove(obj.getUuid());
				}
				
				if (obj instanceof Asteroid) {
					obj.update();
				}
				
				obj.clientUpdate();
			}
		}
		
	}

	@Override
	public GameObject getGameObject(String uuid) {
		return objids.get(uuid);
	}
	
	public void processPackets(int depth) throws IOException {
		List<AbstractPacket> packets = new ArrayList<>();
		//long start = System.currentTimeMillis();
		
		ByteBuffer buff = receiveBuffer;
		
		if (receiveBufferLen == null) {
			receiveBufferLen = ByteBuffer.allocate(4);
		}
		
		if (receiveBufferLen.hasRemaining()) {
			channel.read(receiveBufferLen);
			if (receiveBufferLen.hasRemaining()) {
				return;
			}
			
		}
		
		receiveBufferLen.position(0);
		
		if (buff == null) {
			receiveBuffer = ByteBuffer.allocate(receiveBufferLen.getInt());
			buff = receiveBuffer;
		}
		
		if (buff.hasRemaining()) {
			channel.read(buff);
			if (buff.hasRemaining()) {
				return;
			}
		}
		
		String value = new String(buff.array());
		
		JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
		JSONObject jobj;
		try {
			jobj = (JSONObject) parser.parse(value);
			AbstractPacket packet = PacketProcessor.fromJSON(jobj);
			
			if (packet == null) {
				System.out.println("Packet is null, no parser exists!");
			}
			
			packets.add(packet);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//long end = System.currentTimeMillis();
		//long delay = end - start;
		//System.out.println("Finished data parsing in " + delay + "ms");
		
		for (AbstractPacket pack : packets) {
			if (pack instanceof ObjectTransmitPacket) {
				if (pack.getType() != "obj_transmit") {
					System.out.println("Not an OTP!");
				}
				
				ObjectTransmitPacket otp = (ObjectTransmitPacket) pack;
				
				if (objids.containsKey(otp.getObj().get("uuid"))) {
					continue;
				}
				
				GameObject obj = GameObjectProcessor.fromJSON(otp.getObj());
				obj.setController(this);
				addObject(obj);
			}
			
			if (pack instanceof PositionUpdatePacket) {
				//System.out.println("Received pup");
				PositionUpdatePacket pup = (PositionUpdatePacket) pack;
				if (getGameObject(pup.getObjectid()) != null) {
					GameObject obj = getGameObject(pup.getObjectid());
					obj.getPosition().setX(pup.getPosition().getX());
					obj.getPosition().setY(pup.getPosition().getY());
				} else {
					//System.out.println("object for pos update doesn't exist!");
				}
			}
			
			if (pack instanceof HealthUpdatePacket) {
				HealthUpdatePacket hup = (HealthUpdatePacket) pack;
				if (getGameObject(hup.getUuid()) != null) {
					GameObject obj = getGameObject(hup.getUuid());	
					obj.setHealth(hup.getHealth());
					
					if (obj.shouldRotate()) {
						obj.setRotation(hup.getRotation());
					}
					
				}
			}
			
			if (pack instanceof DeleteObjectPacket) {
				DeleteObjectPacket dop = (DeleteObjectPacket) pack;
				if (getGameObject(dop.getUuid()) != null) {
					getGameObject(dop.getUuid()).delete();
				}
				
			}
			
			if (pack instanceof ScreenUpdatePacket) {
				ScreenUpdatePacket sup = (ScreenUpdatePacket) pack;
				pos.setX(sup.getPos().getX());
				pos.setY(sup.getPos().getY());
			}
			
			if (pack instanceof ShipDataPacket) {
				ShipDataPacket supp = (ShipDataPacket) pack;
				ClientPlayer p = new ClientPlayer(supp.getName(), supp.isAdmin());
				clients.put(supp.getShipid(), p);
			}
			
			if (pack instanceof ShipRemovePacket) {
				ShipRemovePacket srp = (ShipRemovePacket) pack;
				clients.remove(srp.getUuid());
			}
			
			if (pack instanceof ChatEnterPacket) {
				ChatEnterPacket cep = (ChatEnterPacket) pack;
				messages.add(new ServerMessage(cep.getMessage()));
			}
			
			//System.out.println("Not parsed :(");
			//System.out.println("Serialised data: " + PacketProcessor.toJSON(pack));
		}
		
		receiveBufferLen = ByteBuffer.allocate(4);
		receiveBuffer = null;
		
		channel.read(receiveBufferLen);
		if (receiveBufferLen.position() > 0) {
			processPackets(depth++);
		}
	}
	
	public void processPackets() throws IOException {
		processPackets(0);
	}

	public int getObjectcount() {
		return objectcount;
	}

	public void setObjectcount(int objectcount) {
		this.objectcount = objectcount;
	}
	
	public void sendPacket(AbstractPacket pa) {
		long start = System.currentTimeMillis();
		long json_start = start;
		long start_convert_obj = System.currentTimeMillis();
		JSONObject jobj = PacketProcessor.toJSON(pa);
		long end_convert_obj = System.currentTimeMillis();
		long delay_convert_obj = end_convert_obj - start_convert_obj;
		
		if (delay_convert_obj >= 2) {
			System.out.println("Slow on generating JSONObject! (" + delay_convert_obj + "ms)");
			System.out.println("Slow Object Class: " + pa.getClass().getName());
		}
		
		String raw_message = jobj.toJSONString();
		long json_end = System.currentTimeMillis();
		long json_delay = json_end - json_start;
		
		if (json_delay >= 2) {
			System.out.println("JSON conversion finished in " + json_delay + "ms");
		}
		
		
		ByteBuffer buf = ByteBuffer.allocate(raw_message.getBytes().length + 4);
		
		ByteBuffer len_buf = ByteBuffer.allocate(4);
		len_buf.putInt(raw_message.length());
		byte[] len_bytes = len_buf.array();
		
		buf.put(len_bytes);
		buf.put(raw_message.getBytes());
		
		buf.position(0);
		long end = System.currentTimeMillis();
		long delay = end - start;
		if (delay >= 10) {
			System.out.println("Constructed data packet in " + delay + "ms");
			System.out.println(new String(buf.array()));
		}
		
		try {
			channel.write(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//System.out.println("Server msg: " + new String(buf.array()));
		//p.getPacketQueue().remove(pa);
		//count++;
		//if (count >= 600000000) {
			//break;
		//}
	}

	public HashMap<String, ClientPlayer> getClients() {
		return clients;
	}

	public List<ServerMessage> getMessages() {
		return messages;
	}

}
