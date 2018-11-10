package com.castrovala.fraser.orbwar.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

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
import com.castrovala.fraser.orbwar.world.WorldController;

public class GameServer extends Thread {
	public static int MAX_PACKET_SIZE = 10240; // Packet limit of 10 KiB
	private boolean localserver;
	private boolean isTicking = true;
	private GameThread gamelogic;
	private volatile boolean active = true;
	private ServerSocketChannel serversock;
	private volatile List<NetworkPlayer> players = new ArrayList<>();
	private volatile List<NetworkPlayer> readyplayers = new ArrayList<>();
	private ServerState state = ServerState.STARTING;
	private File savefile = null;
	private boolean nosecurity = false;
	
	public File getSavefile() {
		return savefile;
	}

	public void setSavefile(File savefile) {
		this.savefile = savefile;
	}

	public GameServer(boolean localserver, File savefile) {
		this.localserver = localserver;
		this.savefile = savefile;
		this.setName("Server Thread");
	}
	
	public GameServer(boolean localserver) {
		this(localserver, null);
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
				Thread.sleep(20);
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
							
							byte[] data;
							
							try {
								data = PacketProcessor.toBytes(pa);
							} catch (NoPacketParserException e) {continue;};

							ByteBuffer buf = ByteBuffer.allocate(data.length + 4);

							ByteBuffer len_buf = ByteBuffer.allocate(4);
							len_buf.putInt(data.length);
							byte[] len_bytes = len_buf.array();

							buf.put(len_bytes);

							buf.put(data);

							buf.flip();
							
							//System.out.println("msg size: " + msgsize + " bytes");
							while (buf.array().length != buf.position()) {
								channel.write(buf);
								//System.out.println("Amount written: " + amountwritten);
								
							}
							
							p.getPacketQueue().remove(pa);

						}
					}
					
					List<AbstractPacket> packets = new ArrayList<>();
					
					if (p.getReceivedLen() == null) {
						p.setReceivedLen(ByteBuffer.allocate(4));
					}
					
					if (p.getReceivedLen().hasRemaining()) {
						channel.read(p.getReceivedLen());
						if (p.getReceivedLen().hasRemaining()) {
							continue;
						}
						p.getReceivedLen().position(0);
					}
					
					int length = p.getReceivedLen().getInt();
					p.getReceivedLen().position(0);
					if (length >= GameServer.MAX_PACKET_SIZE || length < 1) {
						p.setReceivedLen(null);
						continue;
					}
					
					ByteBuffer buff = p.getReceived();
					if (buff == null) {
						buff = ByteBuffer.allocate(p.getReceivedLen().getInt());
						p.setReceived(buff);
					}
					
					if (buff.hasRemaining()) {
						channel.read(buff);
						if (buff.hasRemaining()) {
							continue;
						}
					}
					
					
					try {
						AbstractPacket packet = PacketProcessor.fromBytes(buff.array());
						
						if (packet == null) {
							System.out.println("Packet is null, no parser exists!");
						}
						
						packets.add(packet);
					} catch (Exception e) {
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
							
							NameCheckPacket response = new NameCheckPacket(false, "A login error occurred", 0, 0);
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
								response.setName("Incorrect Size! (between 1 and 10)");
							}
							
							for (NetworkPlayer pl : getPlayers().toArray(new NetworkPlayer[getPlayers().size()])) {
								if (pl.getName().equalsIgnoreCase(ncp.getName())) {
									response.setLogin(false);
									response.setName("That name is taken!");
								}
							}
							
							if (ncp.getName() == null || ncp.getName().equals("")) {
								response.setLogin(false);
								response.setName("Don't leave your name blank!");
							}
							
							if (p.getScreenWidth() > 1024 || p.getScreenHeight() > 1024) {
								response.setLogin(false);
								response.setName("Invalid game size! (Don't try to cheat! :O )");
							}
							
							
							if (!response.isLogin()) {
								System.out.println("Refusing player with reason '" + response.getName() + "'");
								System.out.println(ncp.getName());
							}
							p.sendPacket(response);
							
							if (ncp.isLogin() && response.isLogin()) {
								p.setName(ncp.getName());
								p.setScreenWidth(ncp.getWidth());
								p.setScreenHeight(ncp.getHeight());
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
								
								switch (kpp.getKey()) {
								case "up":
									p.forward = true;
									break;
								case "fire":
									p.fire = true;
									break;
								case "left":
									p.left = true;
									break;
								case "right":
									p.right = true;
									break;
								default:
									break;
									// Something went wrong
								
								}
							}
							gotcontrol = true;
							
						}
						
						if (pa instanceof EditorTransmitPacket) {
							
							
							if (p.isAdmin() || nosecurity) {
								EditorTransmitPacket etp = (EditorTransmitPacket) pa;
								GameObject obj = GameObjectProcessor.fromJSON(etp.getObj());
								synchronized (p.getUniverse()) {
									obj.setController(p.getUniverse());
									p.getUniverse().addObject(obj);
									obj.afterBirth();
								} 
							}
							
						}
						
						if (pa instanceof ChatEnterPacket) {
							ChatEnterPacket cep = (ChatEnterPacket) pa;
							if (cep.getMessage().length() > 0) {
								String message = "[" + p.getName() + "] " + cep.getMessage();
								ChatEnterPacket cepp = new ChatEnterPacket(message);
								
								for (NetworkPlayer pl : getPlayers()) {
									
									if (cep.getMessage().replace(" ", "").length() == 0) {
										break;
									}
									
									if (cep.getMessage().equalsIgnoreCase("/secon") && p.isAdmin()) {
										nosecurity = false;
										p.sendPacket(new ChatEnterPacket("Server security has been turned on!"));
										break;
									}
									
									if (cep.getMessage().equalsIgnoreCase("/secoff") && p.isAdmin()) {
										p.sendPacket(new ChatEnterPacket("Server security has been turned off!"));
										nosecurity = true;
										break;
									}
									
									pl.sendPacket(cepp);
								}
							}
							
							
						}
					}
					
					buff = null;
					p.setReceivedLen(null);
					p.setReceived(buff);
					
				} catch (Exception e) {
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
		try {
			gamelogic.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Server thread terminated...");
		
		saveGame();
		
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
		p.setUniverseUnsafe(getGameThread().getManager().getUniverses().get(0));
		readyplayers.add(p);
		try {
			p.setAdmin(p.getConn().getRemoteAddress().toString().startsWith("/127.0.0.1:"));
			System.out.println("Showing IP");
			System.out.println(p.getConn().getRemoteAddress().toString().startsWith("/127.0.0.1:"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		synchronized (p.getUniverse()) {
			PlayerShip ship = new PlayerShip(new Position(300, 300), p.getUniverse());
			ship.setControl(p);
			p.setControl(ship);
			
			
			p.getUniverse().addObject(ship);
			System.out.println("Added Player Ship");
			
			for (GameObject obj : p.getUniverse().allObjects()) {
				if (obj == ship) {
					continue;
				}
				
				if (!(obj instanceof Controllable)) {
					continue;
				}
				
				Controllable c = (Controllable) obj;
				
				if (c.getControl() == null)
					continue;
				
				NetworkPlayer tp = (NetworkPlayer) c.getControl();
				
				ShipDataPacket supp = new ShipDataPacket(tp.getName(), obj.getUuid(), tp.isAdmin());
				p.sendPacket(supp);
			}
			
			ShipDataPacket sdp = new ShipDataPacket(null, null, false);
			for (NetworkPlayer pl : getPlayers()) {
				if (pl == p) {
					continue;
				}
				
				if (p.getControl() != null) {
					sdp.setName(p.getName());
					sdp.setShipid(((GameObject)p.getControl()).getUuid());
					sdp.setAdmin(p.isAdmin());
					pl.sendPacket(sdp);
				}
				
			}
			
		}
	}
	
	private void saveGame() {
		if (savefile != null) {
			WorldController c = getGameThread().getManager().getUniverses().get(0);
			if (!savefile.exists()) {
				savefile.mkdir();
				
			}
			
			c.saveZones(savefile);
		}
	}

}
