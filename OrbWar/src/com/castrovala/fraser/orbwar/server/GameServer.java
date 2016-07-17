package com.castrovala.fraser.orbwar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.net.AbstractPacket;
import com.castrovala.fraser.orbwar.net.ChatEnterPacket;
import com.castrovala.fraser.orbwar.net.EditorTransmitPacket;
import com.castrovala.fraser.orbwar.net.KeyPressPacket;
import com.castrovala.fraser.orbwar.net.NameCheckPacket;
import com.castrovala.fraser.orbwar.net.PacketProcessor;
import com.castrovala.fraser.orbwar.net.ShipDataPacket;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.world.Position;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class GameServer extends Thread {
	private boolean localserver;
	private boolean isTicking = true;
	private GameThread gamelogic;
	private volatile boolean active = true;
	private ServerSocketChannel serversock;
	private volatile List<NetworkPlayer> players = new ArrayList<>();
	private volatile List<NetworkPlayer> readyplayers = new ArrayList<>();
	private ServerState state = ServerState.STARTING;
	
	public GameServer(boolean localserver) {
		this.localserver = localserver;
		this.setName("Server Thread");
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
				Thread.sleep(15);
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
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (NetworkPlayer p : players.toArray(new NetworkPlayer[players.size()])) {
				try {
					SocketChannel channel = p.getConn();
					//System.out.println("Acquiring packet queue lock...");
					synchronized (p.getPacketQueue()) {
						for (AbstractPacket pa : p.getPacketQueue()
								.toArray(new AbstractPacket[p.getPacketQueue().size()])) {

							if (pa == null) {
								System.out.println("Packet is null");
								continue;
							}

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
							}

							channel.write(buf);
							p.getPacketQueue().remove(pa);

						}
					}
					
					List<AbstractPacket> packets = new ArrayList<>();
					
					if (p.getRecievedLen() == null) {
						p.setRecievedLen(ByteBuffer.allocate(4));
					}
					
					if (p.getRecievedLen().hasRemaining()) {
						channel.read(p.getRecievedLen());
						if (p.getRecievedLen().hasRemaining()) {
							continue;
						}
						p.getRecievedLen().position(0);
					}
					
					ByteBuffer buff = p.getReceived();
					if (buff == null) {
						buff = ByteBuffer.allocate(p.getRecievedLen().getInt());
						p.setReceived(buff);
					}
					
					if (buff.hasRemaining()) {
						channel.read(buff);
						if (buff.hasRemaining()) {
							continue;
						}
					}
					
					String value = new String(buff.array());
					//System.out.println("value: " + value);
					
					JSONParser parser = new JSONParser(NORM_PRIORITY);
					JSONObject jobj;
					try {
						jobj = (JSONObject) parser.parse(value);
						AbstractPacket packet = PacketProcessor.fromJSON(jobj);
						
						if (packet == null) {
							System.out.println("Packet is null, no parser exists!");
						}
						
						packets.add(packet);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//long end = System.currentTimeMillis();
					//long delay = end - start;
					
					boolean gotcontrol = false;
					for (AbstractPacket pa : packets) {
						//System.out.println("Server found packet: " + pa.getClass().getName());
						
						if (pa instanceof NameCheckPacket && !readyplayers.contains(p)) {
							NameCheckPacket ncp = (NameCheckPacket) pa;
							
							NameCheckPacket response = new NameCheckPacket(false, "An login error occurred");
							for (char c : ncp.getName().toCharArray()) {
								if (Character.isLetterOrDigit(c) || c == '_') {
									response.setLogin(true);
								} else {
									response.setLogin(false);
									response.setName("Invalid characters");
									break;
								}
							}
							
							if (ncp.getName().length() >= 1 && ncp.getName().length() > 10) {
								response.setLogin(false);
								response.setName("Incorrect Size! (between 1 and 10");
							}
							
							for (NetworkPlayer pl : getPlayers().toArray(new NetworkPlayer[getPlayers().size()])) {
								if (pl.getName().equalsIgnoreCase(ncp.getName())) {
									response.setLogin(false);
									response.setName("That name is taken!");
								}
							}
							
							p.sendPacket(response);
							
							if (ncp.isLogin() && response.isLogin()) {
								System.out.println("Setting name");
								p.setName(ncp.getName());
								finishLogin(p);
							}
							
							if (!readyplayers.contains(p)) {
								continue;
							}
							
						}
						
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
						
						if (pa instanceof ChatEnterPacket) {
							ChatEnterPacket cep = (ChatEnterPacket) pa;
							if (cep.getMessage().length() > 0) {
								String message = "[" + p.getName() + "] " + cep.getMessage();
								ChatEnterPacket cepp = new ChatEnterPacket(message);
								
								if (cep.getMessage().startsWith("/name")) {
									if (cep.getMessage().length() <= 6) {
										message = "You need to enter a name to use this!";
										cepp.setMessage(message);
										p.sendPacket(cepp);
										message = "";
									} else {
										String name = cep.getMessage().substring(6);
										if (name.trim().length() == 0) {
											message = "That name is not suitable";
											p.sendPacket(new ChatEnterPacket(message));
										} else {
											message = "'" + p.getName() + "' changed their name to '" + name + "'";
											cepp.setMessage(message);
											p.setName(name);
											synchronized (gamelogic.getController()) {
												if (p.getControl() != null) {
													ShipDataPacket sdp = new ShipDataPacket(name,
															((GameObject) p.getControl()).getUuid());
													for (NetworkPlayer np : getPlayers()) {
														np.sendPacket(sdp);
													} 
												}
											}
										}
										
									}
								}
								
								for (NetworkPlayer pl : getPlayers()) {
									
									if (message.replace(" ", "").length() == 0) {
										break;
									}
									
									pl.sendPacket(cepp);
								}
							}
						}
					}
					
					buff = null;
					p.setRecievedLen(null);
					p.setReceived(buff);
					
				} catch (IOException | IllegalArgumentException e) {
					e.printStackTrace();
					try {
						p.getConn().close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					players.remove(p);
					readyplayers.remove(p);
					if (p.getControl() != null) {
						((GameObject)p.getControl()).delete();
					}
				}
				
			}
			
			
		}
			
		for (NetworkPlayer player : getPlayers()) {
			try {
				player.getConn().close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		return readyplayers;
	}

	public synchronized ServerState getServerState() {
		return state;
	}

	public synchronized void setServerState(ServerState state) {
		this.state = state;
	}
	
	private void finishLogin(NetworkPlayer p) {
		System.out.println("finishlogin called for " + p.getName());
		
		readyplayers.add(p);
		
		synchronized (gamelogic.getController()) {
			PlayerShip ship = new PlayerShip(new Position(300, 300), gamelogic.getController());
			ship.setControl(p);
			p.setControl(ship);
			
			
			gamelogic.getController().addObject(ship);
			System.out.println("Added Player Ship");
			
			for (GameObject obj : gamelogic.getController().allObjects()) {
				if (obj == ship) {
					continue;
				}
				
				if (!(obj instanceof Controllable)) {
					continue;
				}
				
				Controllable c = (Controllable) obj;
				
				NetworkPlayer tp = (NetworkPlayer) c.getControl();
				
				ShipDataPacket supp = new ShipDataPacket(tp.getName(), obj.getUuid());
				p.sendPacket(supp);
			}
			
			ShipDataPacket sdp = new ShipDataPacket(null, null);
			for (NetworkPlayer pl : getPlayers()) {
				if (pl == p) {
					continue;
				}
				
				if (p.getControl() != null) {
					sdp.setName(p.getName());
					sdp.setShipid(((GameObject)p.getControl()).getUuid());
					pl.sendPacket(sdp);
				}
				
				/*if (pl.getControl() != null) {
					sdp.setName(pl.getName());
					sdp.setShipid(((GameObject)pl.getControl()).getUuid());
					p.sendPacket(sdp);
				}*/
			}
			
		}
	}

}
