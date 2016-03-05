package com.castrovala.fraser.orbwar.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.WorldZone;

public class NetworkPlayer implements ControlUser {
	private GameServer server;
	private volatile List<AbstractPacket> packetQueue = Collections.synchronizedList(new ArrayList<AbstractPacket>());
	private SocketChannel conn;
	private String name;
	private Controllable ship;
	private List<WorldZone> seenZones = new ArrayList<>();
	public Position currentpos = new Position(0, 0);
	private volatile ByteBuffer received;
	
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
		
		//System.out.println("I have been told to send: " + packet.getClass().getName());
		
		synchronized (packetQueue) {
			/*if (packet instanceof HealthUpdatePacket) {
				for (AbstractPacket p : packetQueue.toArray(new AbstractPacket[packetQueue.size()])) {
					if (p instanceof HealthUpdatePacket) {
						packetQueue.remove(p);
					}
				}
			}*/
			getPacketQueue().add(packet);
		}
		
		//System.out.println("Contains packet: " + getPacketQueue().contains(packet));
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
		ship = c;
		
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

}
