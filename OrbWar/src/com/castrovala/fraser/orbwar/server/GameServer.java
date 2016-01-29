package com.castrovala.fraser.orbwar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.PacketParser;
import com.castrovala.fraser.orbwar.net.PacketProcessor;

public class GameServer extends Thread {
	private boolean localserver;
	private boolean isTicking = true;
	private GameThread gamelogic;
	private volatile boolean active = true;
	private ServerSocketChannel serversock;
	private List<NetworkPlayer> players = new ArrayList<>();
	
	public GameServer(boolean localserver) {
		this.localserver = localserver;
	}

	public synchronized boolean isLocalserver() {
		return localserver;
	}

	public synchronized void setLocalserver(boolean localserver) {
		this.localserver = localserver;
	}

	public synchronized boolean isTicking() {
		return isTicking;
	}

	public synchronized void setTicking(boolean isTicking) {
		this.isTicking = isTicking;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	public void run() {
		try {
			serversock = ServerSocketChannel.open();
			serversock.bind(new InetSocketAddress(5555));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("Server born");
		
		// Create gamelogic thread and start it
		gamelogic = new GameThread(this);
		gamelogic.start();
		
		// TODO Manage connections
		
		while (active) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				SocketChannel channel = serversock.accept();
				
				if (channel != null) {
					NetworkPlayer p = new NetworkPlayer(this, channel);
					System.out.println("Player Connected");
					players.add(p);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (NetworkPlayer p : players) {
				try {
					SocketChannel channel = p.getConn();
					for (AbstractPacket pa : p.getPacketQueue()) {
						JSONObject jobj = PacketProcessor.toJSON(pa);
						String raw_message = jobj.toJSONString();
						ByteBuffer buf = ByteBuffer.allocate(raw_message.getBytes().length);
						channel.write(buf);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			
		}
		System.out.println("Stopping game thread");
		gamelogic.stopLogic();
		System.out.println("Server thread terminated...");
	}
	
	public GameThread getGameThread() {
		return gamelogic;
	}
	
	public synchronized void stopServer() {
		active = false;
	}

	public List<NetworkPlayer> getPlayers() {
		return players;
	}

}
