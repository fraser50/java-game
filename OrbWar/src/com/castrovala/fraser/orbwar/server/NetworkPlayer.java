package com.castrovala.fraser.orbwar.server;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import com.castrovala.fraser.orbwar.net.AbstractPacket;

public class NetworkPlayer {
	private GameServer server;
	private ArrayList<AbstractPacket> packetQueue = new ArrayList<>();
	private SocketChannel conn;
	
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

	public ArrayList<AbstractPacket> getPacketQueue() {
		return packetQueue;
	}
	
	public void sendPacket(AbstractPacket packet) {
		packetQueue.add(packet);
	}

	public SocketChannel getConn() {
		return conn;
	}

	public void setConn(SocketChannel conn) {
		this.conn = conn;
	}

}
