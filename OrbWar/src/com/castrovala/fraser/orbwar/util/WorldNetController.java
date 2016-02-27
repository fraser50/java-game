package com.castrovala.fraser.orbwar.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import com.castrovala.fraser.orbwar.Constants;
import com.castrovala.fraser.orbwar.gameobject.Asteroid;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.HealthUpdatePacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.net.PositionUpdatePacket;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;

public class WorldNetController implements WorldProvider {
	private SocketChannel channel;
	private List<WorldZone> zones = new ArrayList<>();
	private HashMap<String, GameObject> objids = new HashMap<>();
	private List<AbstractPacket> packetQueue = new ArrayList<>();
	private int objectcount;
	
	public WorldNetController(String host, int port) {
		//super();
		
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(host, port));
			
			while (!channel.finishConnect()) {
				try {
					Thread.sleep(2);
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
		objids.put(o.getUuid(), o);
		
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
		for (WorldZone zone : zones) {
			for (GameObject obj : zone.getGameobjects().toArray(new GameObject[zone.getGameobjects().size()])) {
				
				if (obj.isDeleted() || obj.isCleaned()) {
					zone.getGameobjects().remove(obj);
					objids.remove(obj.getUuid());
				}
				
				//TODO Add client-side game-object update code
				if (obj instanceof Asteroid) {
					obj.update();
				}
			}
		}
		
	}

	@Override
	public GameObject getGameObject(String uuid) {
		return objids.get(uuid);
	}
	
	public void processPackets() throws IOException {
		//System.out.println("Process packets called");
		List<AbstractPacket> packets = new ArrayList<>();
		long start = System.currentTimeMillis();
		ByteBuffer buff = ByteBuffer.allocate( Constants.packetsize ); // 65536
		while (buff.hasRemaining()) {
			channel.read(buff);
		}
		
		String value = new String(buff.array());
		//System.out.println("value: " + value);
		while (value.trim() != "") {
			//System.out.println("Value: " + value);
			//if (value.length() <= 4) {
			//	break;
			//}
			
			String length_str = "";
			try {
				length_str = value.substring(0, 4);
			} catch (StringIndexOutOfBoundsException e) {
				System.out.println("Length failed");
				break;
			}
			
			byte[] length_bytes = length_str.getBytes();
			int length = ByteBuffer.wrap(length_bytes).getInt();
			
			if (length == 0) {
				break;
			}
			
			//if (length + 4 < value.length()) {
			//	break;
			//}
			
			String json_str;
			try {
				json_str = value.substring(4, 4 + length);
			} catch (StringIndexOutOfBoundsException e) {
				System.out.println("Out of range");
				return;
			}
			
			
			JSONParser parser = new JSONParser();
			JSONObject jobj;
			try {
				jobj = (JSONObject) parser.parse(json_str);
				AbstractPacket packet = PacketProcessor.fromJSON(jobj);
				
				if (packet == null) {
					System.out.println("Packet is null, no parser exists!");
				}
				
				packets.add(packet);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			value = value.substring(4 + length, value.length());
			break;
		}
		
		long end = System.currentTimeMillis();
		long delay = end - start;
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
			
			//System.out.println("Not parsed :(");
			//System.out.println("Serialised data: " + PacketProcessor.toJSON(pack));
		}
		
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
		
		
		ByteBuffer buf = ByteBuffer.allocate(Constants.packetsize);
		
		ByteBuffer len_buf = ByteBuffer.allocate(4);
		len_buf.putInt(raw_message.length());
		byte[] len_bytes = len_buf.array();
		
		buf.put(len_bytes);
		
		
		//int l = ByteBuffer.wrap(len_bytes).getInt();
		//System.out.println("Byte length: " + l);
		
		
		buf.put(raw_message.getBytes());
		long padding_start = System.currentTimeMillis();
		//for (int i = 1; i < (Constants.packetsize) - (len_bytes.length + raw_message.getBytes().length) + 1; i++) {
		//	buf.put((byte)'a');
		//}
		
		//String padding = new String(new char[(Constants.packetsize) - (len_bytes.length + raw_message.getBytes().length)]).replaceFirst("\0", "a");
		//buf.put(padding.getBytes());
		
		//buf.put(new byte[(Constants.packetsize) - (len_bytes.length + raw_message.getBytes().length)]);
		
		long padding_end = System.currentTimeMillis();
		long padding_delay = padding_end - padding_start;
		
		if (padding_delay >= 3) {
			System.out.println("Finished padding in " + padding_delay + "ms");
		}
		
		
		//buf.flip();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("Server msg: " + new String(buf.array()));
		//p.getPacketQueue().remove(pa);
		//count++;
		//if (count >= 600000000) {
			//break;
		//}
	}
	

}
