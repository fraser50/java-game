package com.castrovala.fraser.orbwar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.Position;

public class GameServer extends Thread {
	private boolean localserver;
	private boolean isTicking = true;
	private GameThread gamelogic;
	private volatile boolean active = true;
	private ServerSocketChannel serversock;
	private List<NetworkPlayer> players = new ArrayList<>();
	private ServerState state = ServerState.STARTING;
	
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
			serversock.configureBlocking(false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("Server born");
		
		// Create gamelogic thread and start it
		gamelogic = new GameThread(this);
		gamelogic.start();
		
		state = ServerState.RUNNING;
		
		// TODO Manage connections
		
		while (active) {
			//System.out.println("Server tick");
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				SocketChannel channel = serversock.accept();
				
				if (channel != null) {
					NetworkPlayer p = new NetworkPlayer(this, channel);
					channel.configureBlocking(false);
					channel.finishConnect();
					System.out.println("Player Connected");
					
					ObjectTransmitPacket otp = new ObjectTransmitPacket(null);
					synchronized (gamelogic) {
						PlayerShip ship = new PlayerShip(new Position(100, 100), gamelogic.getController());
						otp.setObj(GameObjectProcessor.toJSON(ship));
						p.sendPacket(otp);
						
					}
					players.add(p);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (NetworkPlayer p : players.toArray(new NetworkPlayer[players.size()])) {
				//System.out.println("Iterating over players");
				try {
					SocketChannel channel = p.getConn();
					for (AbstractPacket pa : p.getPacketQueue()) {
						JSONObject jobj = PacketProcessor.toJSON(pa);
						String raw_message = jobj.toJSONString();
						ByteBuffer buf = ByteBuffer.allocate(raw_message.getBytes().length + 4);
						
						ByteBuffer len_buf = ByteBuffer.allocate(4);
						len_buf.putInt(raw_message.length());
						byte[] len_bytes = len_buf.array();
						
						buf.put(len_bytes);
						
						//int l = ByteBuffer.wrap(len_bytes).getInt();
						//System.out.println("Byte length: " + l);
						
						buf.put(raw_message.getBytes());
						buf.flip();
						System.out.println("Server: " + buf.toString());
						channel.write(buf);
						
					}
					p.getPacketQueue().clear();
					
					ByteBuffer buff = ByteBuffer.allocate(65536);
					if (p.getConn().read(buff) < 1) {
						continue;
					}
					
					List<AbstractPacket> packets = new ArrayList<>();
					String value = buff.toString();
					while (value != "") {
						String length_str = value.substring(0, 4);
						byte[] length_bytes = length_str.getBytes();
						int length = ByteBuffer.wrap(length_bytes).getInt();
						String json_str = value.substring(4, 4 + length);
						JSONParser parser = new JSONParser();
						JSONObject jobj = (JSONObject) parser.parse(json_str);
						AbstractPacket packet = PacketProcessor.fromJSON(jobj);
						packets.add(packet);
						value = value.substring(0, 4 + length);
					}
					
					
					
					
				} catch (IOException e) {
					e.printStackTrace();
					try {
						p.getConn().close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					players.remove(p);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
		}
		try {
			serversock.close();
			System.out.println("Closed server socket");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to close server socket!");
			e.printStackTrace();
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

	public synchronized ServerState getServerState() {
		return state;
	}

	public synchronized void setServerState(ServerState state) {
		this.state = state;
	}

}
