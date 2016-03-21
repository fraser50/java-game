package com.castrovala.fraser.orbwar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import com.castrovala.fraser.orbwar.Constants;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.EditorTransmitPacket;
import com.castrovala.fraser.orbwar.net.KeyPressPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.Position;

public class GameServer extends Thread {
	private boolean localserver;
	private boolean isTicking = true;
	private GameThread gamelogic;
	private volatile boolean active = true;
	private ServerSocketChannel serversock;
	private volatile List<NetworkPlayer> players = new ArrayList<>();
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
			
			//System.out.println("Looking for new connections...");
			try {
				SocketChannel channel = serversock.accept();
				
				if (channel != null) {
					NetworkPlayer p = new NetworkPlayer(this, channel);
					channel.configureBlocking(false);
					System.out.println("Pending Connection");
					channel.finishConnect();
					System.out.println("Player Connected");
					players.add(p);
					synchronized (gamelogic.getController()) {
						PlayerShip ship = new PlayerShip(new Position(200, 200), gamelogic.getController());
						ship.setControl(p);
						p.setControl(ship);
						gamelogic.getController().addObject(ship);
						System.out.println("Added Player Ship");
					}
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("No longer checking for connections");
			
			for (NetworkPlayer p : getPlayers().toArray(new NetworkPlayer[getPlayers().size()])) {
				//System.out.println("Iterating over players");
				try {
					SocketChannel channel = p.getConn();
					int count = 0;
					//System.out.println("Acquiring packet queue lock...");
					synchronized (p.getPacketQueue()) {
						//System.out.println("Lock acquired");
						//System.out.println(p.getPacketQueue().size() + " packets in queue");
						for (AbstractPacket pa : p.getPacketQueue()
								.toArray(new AbstractPacket[p.getPacketQueue().size()])) {

						//for (AbstractPacket pa : p.getPacketQueue()) {
							//System.out.println("Preparing to send packet");
							if (pa == null) {
								System.out.println("Packet is null");
								continue;
							}

							//System.out.println("Packet to transmit class: " + pa.getClass().getName());
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
								//System.out.println("Finished padding in " + padding_delay + "ms");
							}

							//buf.flip();
							buf.position(0);
							long end = System.currentTimeMillis();
							long delay = end - start;
							if (delay >= 10) {
							}

							channel.write(buf);
							count++;
							p.getPacketQueue().remove(pa);
							if (count >= 600000000) {
								//break;
							}

						}
					}
					
					List<AbstractPacket> packets = new ArrayList<>();
					long start = System.currentTimeMillis();
					 // 65536
					//System.out.println("Server waiting for data");
					ByteBuffer buff = p.getReceived();
					if (buff == null) {
						//System.out.println("Server buffer is empty");
						buff = ByteBuffer.allocate(Constants.packetsize);
						p.setReceived(buff);
					}
					
					if (buff.hasRemaining()) {
						channel.read(buff);
						continue;
					}
					
					String value = new String(buff.array());
					//System.out.println("value: " + value);
					while (value.trim() != "") {
						//System.out.println("Server parsing: " + value.length());
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
					
					boolean gotcontrol = false;
					for (AbstractPacket pa : packets) {
						//System.out.println("Server found packet: " + pa.getClass().getName());
						if (pa instanceof KeyPressPacket) {
							if (gotcontrol) {
								continue;
							}
							
							KeyPressPacket kpp = (KeyPressPacket) pa;
							if (p.getControl() != null) {
								GameObject ship = (GameObject) p.getControl();
								synchronized (ship) {
									switch (kpp.getKey()) {
										case "up":
											p.getControl().fly();
											break;
										case "fire":
											p.getControl().fire();
											break;
										case "left":
											p.getControl().left();
											break;
										case "right":
											p.getControl().right();
											break;
										default:
											// Something went wrong
									}
								}
							}
							gotcontrol = true;
							
						}
						
						if (pa instanceof EditorTransmitPacket) {
							EditorTransmitPacket etp = (EditorTransmitPacket) pa;
							GameObject obj = GameObjectProcessor.fromJSON(etp.getObj());
							
							synchronized (gamelogic.getController()) {
								obj.setController(gamelogic.getController());
								obj.setUuid(UUID.randomUUID().toString());
								gamelogic.getController().addObject(obj);
							}
							
						}
					}
					
					buff = ByteBuffer.allocate( Constants.packetsize );
					p.setReceived(buff);
					
				} catch (IOException e) {
					e.printStackTrace();
					try {
						p.getConn().close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					players.remove(p);
					if (p.getControl() != null) {
						((GameObject)p.getControl()).delete();
					}
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
