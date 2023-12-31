package com.castrovala.fraser.orbwar.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.ResetPacket;
import com.castrovala.fraser.orbwar.net.ShipDataPacket;
import com.castrovala.fraser.orbwar.net.ShipRemovePacket;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;
import com.castrovala.fraser.orbwar.world.WorldZone;

public class NetworkPlayer implements ControlUser {
	private UUID id = UUID.randomUUID();
	private GameServer server;
	private volatile List<AbstractPacket> packetQueue = Collections.synchronizedList(new ArrayList<AbstractPacket>());
	private SocketChannel conn;
	private String name = "";
	private Controllable ship;
	private List<WorldZone> seenZones = new ArrayList<>();
	private Position currentpos = new Position(0, 0);
	private volatile ByteBuffer received;
	private volatile ByteBuffer receivedLen;
	private boolean admin;
	private int screenWidth = 0;
	private int screenHeight = 0;
	
	public boolean left;
	public boolean right;
	public boolean forward;
	public boolean fire;
	
	private WorldProvider universe;
	
	public NetworkPlayer(GameServer server, SocketChannel conn) {
		this.server = server;
		this.conn = conn;
	}

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

	public List<AbstractPacket> getPacketQueue() {
		synchronized (packetQueue) {
			return packetQueue;
		}
	}
	
	public void sendPacket(AbstractPacket packet) {
		if (packet == null) {
			throw new NullPointerException("Value was null");
		}
		
		synchronized (packetQueue) {
			getPacketQueue().add(packet);
		}
	}

	public SocketChannel getConn() {
		return conn;
	}

	public void setConn(SocketChannel conn) {
		this.conn = conn;
	}

	@Override
	public Controllable getControl() {
		return ship;
	}

	@Override
	public void setControl(Controllable c) {
		if (ship != null) {
			ShipRemovePacket srp = new ShipRemovePacket( ((GameObject)ship).getUuid());
			sendPacket(srp);
		}
		
		ship = c;
		
		if (ship != null) {
			ShipDataPacket supp = new ShipDataPacket(name, ((GameObject)ship).getUuid(), admin);
			sendPacket(supp);
		}
		
	}

	public synchronized List<WorldZone> getSeenZones() {
		return seenZones;
	}

	public ByteBuffer getReceived() {
		return received;
	}

	public synchronized void setReceived(ByteBuffer received) {
		this.received = received;
	}

	public Position getCurrentpos() {
		return currentpos;
	}

	public void setCurrentpos(Position currentpos) {
		this.currentpos = currentpos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public ByteBuffer getReceivedLen() {
		return receivedLen;
	}

	public void setReceivedLen(ByteBuffer receivedLen) {
		this.receivedLen = receivedLen;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public WorldProvider getUniverse() {
		return universe;
	}

	public void setUniverse(WorldProvider universe) {
		this.universe = universe;
		getSeenZones().clear();
		sendPacket(new ResetPacket());
	}
	
	public void setUniverseUnsafe(WorldProvider universe) {
		this.universe = universe;
	}

}
