package com.castrovala.fraser.orbwar;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;

import com.castrovala.fraser.orbwar.client.ClientPlayer;
import com.castrovala.fraser.orbwar.client.ServerMessage;
import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.editor.Resizable;
import com.castrovala.fraser.orbwar.gameobject.Asteroid;
import com.castrovala.fraser.orbwar.gameobject.BigAsteroid;
import com.castrovala.fraser.orbwar.gameobject.BombBoy;
import com.castrovala.fraser.orbwar.gameobject.Bullet;
import com.castrovala.fraser.orbwar.gameobject.Explosion;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.MotherTransport;
import com.castrovala.fraser.orbwar.gameobject.OliverGuider;
import com.castrovala.fraser.orbwar.gameobject.OliverMothership;
import com.castrovala.fraser.orbwar.gameobject.Planet;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.gameobject.RespawnLaser;
import com.castrovala.fraser.orbwar.gameobject.RespawnPoint;
import com.castrovala.fraser.orbwar.gameobject.ShieldDrone;
import com.castrovala.fraser.orbwar.gameobject.ShieldGenerator;
import com.castrovala.fraser.orbwar.gameobject.Star;
import com.castrovala.fraser.orbwar.gameobject.Turret;
import com.castrovala.fraser.orbwar.gameobject.UniverseTransporter;
import com.castrovala.fraser.orbwar.gameobject.WormHole;
import com.castrovala.fraser.orbwar.gameobject.interior.StationFloor;
import com.castrovala.fraser.orbwar.gameobject.npc.EnemyDrone;
import com.castrovala.fraser.orbwar.gameobject.npc.WarShip;
import com.castrovala.fraser.orbwar.gameobject.particle.HydrogenParticle;
import com.castrovala.fraser.orbwar.gameobject.particle.SmokeParticle;
import com.castrovala.fraser.orbwar.gui.GuiButton;
import com.castrovala.fraser.orbwar.gui.GuiClickable;
import com.castrovala.fraser.orbwar.gui.GuiElement;
import com.castrovala.fraser.orbwar.gui.GuiFocusable;
import com.castrovala.fraser.orbwar.gui.GuiInputField;
import com.castrovala.fraser.orbwar.gui.GuiLabel;
import com.castrovala.fraser.orbwar.gui.GuiScreen;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.item.Inventory;
import com.castrovala.fraser.orbwar.item.InventoryType;
import com.castrovala.fraser.orbwar.item.ItemBattery;
import com.castrovala.fraser.orbwar.net.ChatEnterPacket;
import com.castrovala.fraser.orbwar.net.DebugInfoPacket;
import com.castrovala.fraser.orbwar.net.DeleteObjectPacket;
import com.castrovala.fraser.orbwar.net.DestructionPacket;
import com.castrovala.fraser.orbwar.net.EditorTransmitPacket;
import com.castrovala.fraser.orbwar.net.HealthUpdatePacket;
import com.castrovala.fraser.orbwar.net.KeyPressPacket;
import com.castrovala.fraser.orbwar.net.NameCheckPacket;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PositionUpdatePacket;
import com.castrovala.fraser.orbwar.net.ResetPacket;
import com.castrovala.fraser.orbwar.net.ScreenUpdatePacket;
import com.castrovala.fraser.orbwar.net.SetLightingPacket;
import com.castrovala.fraser.orbwar.net.ShieldUpdatePacket;
import com.castrovala.fraser.orbwar.net.ShipDataPacket;
import com.castrovala.fraser.orbwar.net.ShipRemovePacket;
import com.castrovala.fraser.orbwar.net.SizeUpdatePacket;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.GameServer;
import com.castrovala.fraser.orbwar.server.MPGameInfo;
import com.castrovala.fraser.orbwar.server.ServerState;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.util.GameState;
import com.castrovala.fraser.orbwar.util.LightCircle;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.weapons.RechargeWeapon;
import com.castrovala.fraser.orbwar.weapons.WeaponOwner;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldNetController;
import com.castrovala.fraser.orbwar.world.WorldZone;

@SuppressWarnings("serial")
public class OrbWarPanel extends Canvas implements Runnable {
	private static int PWIDTH  = 1024;
	private static int PHEIGHT = 1024;
	
	private Thread game;
	private volatile boolean running = false;
	private volatile WorldNetController controller;
	private volatile Position mylocation = new Position(0, 0);
	public Controllable myship;
	public static final boolean debug = true;
	private List<Position> starpoints = new ArrayList<>();
	private boolean init_game = false;
	private int rendertime;
	private int updatetime;
	private boolean renderedbefore = true; // Just to disable annoying message, change to false to see time to render things
	private GameState state = GameState.MENU;
	private GuiScreen activegui;
	private GameServer internalserver;
	private String activecontrol = null;
	private List<MPGameInfo> activeGames = new ArrayList<>();
	private GameObject editorObj;
	private boolean lockEditor = false;
	private volatile Position mousePos = new Position(0, 0);
	private volatile boolean clicked = false;
	private GuiFocusable focused;
	private boolean chatgui;
	private String currentmsg = "";
	private volatile int turntime = 0;
	private volatile int turnamount = 0;
	private Graphics2D g2d;
	//private long timesticked = 2;
	private BufferStrategy strategy;
	private int lightcounter = 0;
	
	private BufferedImage lightmap;
	private List<LightCircle> lightsources = new ArrayList<>();
	
	private Inventory inv;
	
	public OrbWarPanel() {
		
		for (int i = 1; i<3;i++) {
			MPGameInfo info = new MPGameInfo("Test" + i, "127.0.0.1", 5555);
			activeGames.add(info);
		}
		
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		setFocusable(true);
		requestFocus();
		
		OrbWarPanel.registerClientAssets();
		
		for (int i = 1; i<1000; i++) {
			starpoints.add(new Position(Util.randomRange(1, PWIDTH), Util.randomRange(1, PHEIGHT)));
		}
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_LEFT) {
					turnamount = -1;
				}
				
				if (keyCode == KeyEvent.VK_RIGHT) {
					turnamount = 1;
				}
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_LEFT) {
					turnamount = 0;
				}
				
				if (keyCode == KeyEvent.VK_RIGHT) {
					turnamount = 0;
				}
				
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if (activegui != null) {
					for (GuiElement el : activegui.getElements()) {
						if (!(el instanceof GuiFocusable)) {
							continue;
						}
						
						GuiFocusable focus = (GuiFocusable) el;
						if (focus == focused) {
							char key = e.getKeyChar();
							if (Character.isDefined(key)) {
								focus.onCharEnter(key);
								focus.onKeyPress(e);
							}
							break;
						}
						
					}
				}
				
				if (!chatgui && keyCode == KeyEvent.VK_C && state == GameState.PLAYING) {
					chatgui = true;
					return;
				}
				
				if (chatgui) {
					
					if (keyCode == KeyEvent.VK_ENTER) {
						ChatEnterPacket cep = new ChatEnterPacket(currentmsg);
						currentmsg = "";
						chatgui = false;
						controller.sendPacket(cep);
						return;
					}
					
					if (keyCode == KeyEvent.VK_BACK_SPACE && currentmsg.length() != 0) {
						currentmsg = currentmsg.substring(0, currentmsg.length() - 1);
					} else {
						char key = e.getKeyChar();
						if (Character.isDefined(key)) {
							currentmsg = currentmsg + key;
						}
					}
					return;
				}
				
				if (keyCode == KeyEvent.VK_ENTER && state == GameState.MENU) {
					//init_game = true;
				}
				
				if (keyCode == KeyEvent.VK_ESCAPE && state == GameState.PLAYING) {
					activecontrol = "quit";
				}
				
				if (state == GameState.MENU) {
					return;
				}
				
				if (myship == null) {
					//return;
				}
				
				if (keyCode == KeyEvent.VK_UP) {
					activecontrol = "up";
					//myship.fly();
				}
				if (keyCode == KeyEvent.VK_DOWN) {
					activecontrol = "fire";
					//myship.fire();
				}
				if (keyCode == KeyEvent.VK_F) {
					//myship.shield();
				}
				if (keyCode == KeyEvent.VK_H) {
					//myship.toggleBrake();
				}
				if (keyCode == KeyEvent.VK_BACK_SPACE) {
					//myship.suicide();
				}
				if (keyCode == KeyEvent.VK_Q) {
					if (state == GameState.PAUSED || state == GameState.PLAYING) {
						if (state == GameState.PLAYING) {
							state = GameState.PAUSED;
						} else {
							state = GameState.PLAYING;
						}
					}
				}
				if (keyCode == KeyEvent.VK_M) {
					editorObj = null;
					activecontrol = "menu";
				}
				
				if (keyCode == KeyEvent.VK_X && editorObj != null) {
					activecontrol = "lock";
				}
				
				if (keyCode == KeyEvent.VK_U) {
					activecontrol = "inv";
				}
				
				if (keyCode == KeyEvent.VK_NUMPAD4 && editorObj != null) {
					editorObj.setRotation(editorObj.getRotation() - 10);
					if (editorObj.getRotation() < 0) {
						editorObj.setRotation(360);
					}
					
					if (editorObj.getRotation() > 360) {
						editorObj.setRotation(0);
					}
				}
				
				if (keyCode == KeyEvent.VK_NUMPAD6 && editorObj != null) {
					editorObj.setRotation(editorObj.getRotation() + 10);
					if (editorObj.getRotation() < 0) {
						editorObj.setRotation(360);
					}
					
					if (editorObj.getRotation() > 360) {
						editorObj.setRotation(0);
					}
				}
					
			}
		});
		
		addMouseListener(new MouseListener() {
			
			private boolean in = false;
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (in) {
					in = false;
					hackymouseClicked(e);
				}
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				in = true;
				if (state == GameState.PLAYING) {
					clicked = true;
				}
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void hackymouseClicked(MouseEvent ev) {
				if (activegui != null) {
					boolean wipefocused = true;
					for (GuiElement e : activegui.getElements()) {
						if (e instanceof GuiClickable) {
							GuiClickable c = (GuiClickable) e;
							Rectangle mrect = new Rectangle(ev.getX(), ev.getY(), 1, 1);
							Position lb = e.getEnd().copy().subtract(e.getStart());
							Rectangle elemrect = new Rectangle((int)e.getStart().getX(), (int)e.getStart().getY(), (int)lb.getX(), (int)lb.getY());
							if (mrect.intersects(elemrect)) {
								c.onClick();
							}
						}
						
						if (e instanceof GuiFocusable) {
							
							
							GuiFocusable f = (GuiFocusable) e;
							Rectangle mrect = new Rectangle(ev.getX(), ev.getY(), 1, 1);
							Position lb = e.getEnd().copy().subtract(e.getStart());
							Rectangle elemrect = new Rectangle((int)e.getStart().getX(), (int)e.getStart().getY(), (int)lb.getX(), (int)lb.getY());
							
							if (mrect.intersects(elemrect)) {
								
								if (focused == e) {
									wipefocused = false;
									continue;
								}
								
								f.setFocus(true);
								
								if (focused != null) {
									focused.setFocus(false);
									focused = null;
								}
								focused = f;
								wipefocused = false;
							}
						}
					}
					
					if (wipefocused && focused != null) {
						focused.setFocus(false);
						focused = null;
					}
				}
				
			}
		});
		
		// Create menu GUI
		activegui = getMainMenu();
	}
	
	public static void main(String[] args) {
		
		if (args.length > 0) {
			if (args[0].equals("server")) {
				OrbWarPanel.registerGameAssets();
				System.out.println("Running headless server");
				System.out.println("");
				GameServer server = new GameServer(false);
				server.start();
				try {
					server.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Server shutdown, exiting");
				return;
				
			}
		}
		
		JFrame frame = new JFrame("OrbWar");
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		PWIDTH = (int) (screensize.getWidth() >= PWIDTH ? PWIDTH : screensize.getWidth());
		PHEIGHT = (int) (screensize.getHeight() >= PHEIGHT ? PHEIGHT : screensize.getHeight());
		
		
		OrbWarPanel panel = new OrbWarPanel();
		frame.add(panel);
		frame.setSize(PWIDTH, PHEIGHT);
		frame.setVisible(true);
		frame.setResizable(false);
		panel.setIgnoreRepaint(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		registerGameAssets();
		frame.setIgnoreRepaint(true);
		panel.addNotify();
		
		
	}
	
	public void addNotify() {
		super.addNotify();
		startGame();
	}
	
	public void startGame() {
		if (game == null || !running) {
			game = new Thread(this);
			game.setName("Client Thread");
			game.start();
		}
	}
	
	public void stopGame() {
		running = false;
	}
	
	public void run() {
		running = true;
		
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		g2d = (Graphics2D) strategy.getDrawGraphics();
		
		while (running) {
			int updatespeed = 1000 / 60; // 1000 / 60;
			if (state == GameState.MENU) {
				if (init_game) {
					long start = System.currentTimeMillis();
					try {
						prepareGame("test");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					long end = System.currentTimeMillis();
					long timetaken = end - start;
					System.out.println("prepareGame() took " + timetaken + " ms (legacy code)");
					init_game = false;
					state = GameState.PLAYING;
					activegui = null;
				}
				gameUpdate();
				
				updateMouse();
				
				
				gameRender();
				
				try {
					int newtime = updatespeed - updatetime - rendertime;
					if (newtime > 0) {
						Thread.sleep(newtime);
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			//GameObject obj = (GameObject) myship;
			if (myship == null) {
				updateMouse();
				gameUpdate();
				gameRender();
				
				try {
					long sleeptime = updatespeed - updatetime - rendertime;
					sleeptime = sleeptime >= 0 ? sleeptime : 0;
					Thread.sleep(sleeptime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			/*if (mylocation.getX() - obj.getPosition().getX() >= 1000000 || mylocation.getY() - obj.getPosition().getY() >= 1000000 ||
				mylocation.getX() - obj.getPosition().getX() <= -1000000 || mylocation.getY() - obj.getPosition().getY() <= -1000000	) {
				mylocation.setX(obj.getPosition().getX() - (PWIDTH / 2) );
				mylocation.setY(obj.getPosition().getY() - (PHEIGHT / 2) );
			}
			
			updateMouse();
			gameUpdate();
			
			gameRender();
			
			
			
			try {
				//Thread.sleep(20);
				Thread.sleep(updatespeed - updatetime - rendertime);
			} catch (InterruptedException ex){}*/
			
		}
		System.exit(0);
	}
	
	private void updateMouse() {
		PointerInfo info = MouseInfo.getPointerInfo();
		if (info != null) {
			Point p = info.getLocation();
			int x = (int) p.getX();
			int y = (int) p.getY();
			x -= this.getLocationOnScreen().getX();
			y -= this.getLocationOnScreen().getY();
			mousePos.setX(x);
			mousePos.setY(y);
		}
		
	}

	private void gameUpdate() {
		
		if (activegui != null) {
			for (GuiElement e : activegui.getElements()) {
				e.update((int)mousePos.getX(), (int)mousePos.getY());
			}
		}
		
		if (running) {
			if (state == GameState.MENU || state == GameState.PAUSED) {
				return;
			}
			
			if (activecontrol != null && activecontrol.equals("quit")) {
				activecontrol = null;
				quitGame();
				return;
			}
			
			if (editorObj != null && clicked) {
				EditorTransmitPacket etp = new EditorTransmitPacket(GameObjectProcessor.toJSON(editorObj));
				controller.sendPacket(etp);
				clicked = false;
				editorObj.setUuid(UUID.randomUUID().toString());
			}
			
			if (editorObj != null && (!lockEditor)) {
				editorObj.setPosition(mousePos.copy().subtract(new Position(editorObj.getWidth() / 2, editorObj.getHeight() / 2)).add(mylocation));
			}
			
			if (lockEditor && editorObj instanceof Resizable) {
				int width = (int) ((mousePos.getX() + mylocation.getX()) - editorObj.getPosition().getX());
				//width = width >= 0 ? width : -width;
				
				int height = (int) ((mousePos.getY() + mylocation.getY()) - editorObj.getPosition().getY());
				//height = height >= 0 ? height : -height;
				
				editorObj.setWidth(width);
				editorObj.setHeight(height);
			}
			
			if (inv != null && clicked) {
				inv.onClick(mousePos);
				clicked = false;
			}
			
			clicked = false;
			
			
		}
		
		try {
			controller.processPackets();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		turntime += turnamount;
		if (turntime <= -3) {
			turntime = 0;
			activecontrol = "left";
		}
		
		if (turntime >= 3) {
			turntime = 0;
			activecontrol = "right";
		}
		
		if (activecontrol != null) {
			if (activecontrol.equals("menu")) {
				activegui = getEditorScreen();
				activecontrol = null;
			} else if (activecontrol.equals("lock")) {
				lockEditor = !lockEditor;
				activecontrol = null;
				
			} else if (activecontrol.equals("inv") && 0==1) {
				inv = new Inventory(InventoryType.SHIP, 25);
				//inv.getSlots().get(7).setItem(new ItemBattery(1));
				activecontrol = null;
				
			} else {
				KeyPressPacket p = new KeyPressPacket(activecontrol);
				activecontrol = null;
				controller.sendPacket(p);
			}
			
		}
		
		controller.updateGame();
		controller.lighting = false;
	}
	
	public void gameRender() {
		
		RenderDebug rd = new RenderDebug(mylocation, lightsources);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, PWIDTH, PHEIGHT);
			
//		System.out.println("Adding all zones");
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, PWIDTH, PHEIGHT);
		g2d.drawString("Fixer", 5, 5);
		int rendereditems = 0;
		for (Position star : starpoints) {
			g2d.setColor(Color.WHITE);
			int offsetx = 0;
			int offsety = 0;
			int newposx = (int) star.getX();
			int newposy = (int) star.getY();
			if (state == GameState.PLAYING) {
				offsetx = (int) ( -(mylocation.getX() / 4) );
				offsety = (int) ( -(mylocation.getY() / 4) );
				
				newposx = (int) (star.getX() + offsetx);
				newposy = (int) (star.getY() + offsety);
				
				while (newposx < 0) {
					newposx += PWIDTH;
				}
				
				while (newposx > PWIDTH) {
					newposx -= PWIDTH;
				}
				
				while (newposy < 0) {
					newposy += PHEIGHT;
				}
				
				while (newposy > PHEIGHT) {
					newposy -= PHEIGHT;
				}
			}
			g2d.drawLine( newposx, newposy, newposx, newposy );
			rendereditems++;
		}
		
		if (activegui != null && state == GameState.MENU) {
			for (GuiElement e : activegui.getElements()) {
				e.render(g2d, (int)mousePos.getX(), (int)mousePos.getY());
			}
		}
		
		if (state == GameState.MENU) {
			if (!strategy.contentsLost()) {
				strategy.show();
			}
			
			return;
		}
		
		HashMap<RenderStage, List<GameObject>> stages = new HashMap<>();
		stages.put(RenderStage.BACKGROUND, new ArrayList<GameObject>());
		stages.put(RenderStage.PARTICLES, new ArrayList<GameObject>());
		stages.put(RenderStage.SPACEOBJECTS, new ArrayList<GameObject>());
		stages.put(RenderStage.SHIPS, new ArrayList<GameObject>());
		stages.put(RenderStage.CONTROL, new ArrayList<GameObject>());
		
		long start = System.currentTimeMillis();
		List<WorldZone> chosenzones = new ArrayList<>();
		for (int x = WorldZone.len_x * -1;x<=PWIDTH; x+=WorldZone.len_x) {
			for (int y = WorldZone.len_y * -1;y<=PHEIGHT; y+=WorldZone.len_y) {
				long px = (long) mylocation.getX() + x;
				long py = (long) mylocation.getY() + y;
				Position pos = new Position(px, py);
				WorldZone zone = controller.getZone(Util.toZoneCoords(pos));
				
				if (!chosenzones.contains(zone)) {
					chosenzones.add(zone);
				}
				
			}
		}
		long end = System.currentTimeMillis();
		long timetaken = end - start;
		if (!renderedbefore) {
			System.out.println("Completed first zone choosing in " + timetaken + "ms");
		}
		
		for (WorldZone zone : chosenzones) {
			if (zone == null) {
				g2d.setColor(Color.RED);
				continue;
			}
			
			for (GameObject obj : zone.getGameobjects().toArray(new GameObject[zone.getGameobjects().size()])) {
				int rel_x = (int)(obj.getPosition().getX() - mylocation.getX());
				int rel_y = (int)(obj.getPosition().getY() - mylocation.getY());
				if ( (rel_x >= 0 && rel_x <= PWIDTH && rel_y >= 0 && rel_y <= PHEIGHT ) || (rel_x + obj.getWidth() >= 0 && rel_x + obj.getWidth() <= PWIDTH && rel_y + obj.getHeight() >= 0 && rel_y + obj.getHeight() <= PHEIGHT) || true  ) {
					stages.get(obj.getRenderStage()).add(obj);
							
//					dbg.fillRect(rel_x, rel_y, 100, 100);
				}
			}
		}
		
		if (debug) {
			//This is for debugging purposes
			for (WorldZone zone : chosenzones) {
				int start_x = (int) (zone.getStartPoint().getX() - mylocation.getX());
				int start_y = (int) (zone.getStartPoint().getY() - mylocation.getY());
				g2d.setColor(Color.CYAN);
				g2d.drawRect(start_x, start_y, WorldZone.len_x, WorldZone.len_y);
				rendereditems += 4;
			
			}	
		}
		
		for (RenderStage s : RenderStage.values()) {
			for (GameObject obj : stages.get(s)) {
				int rel_x = (int)(obj.getPosition().getX() - mylocation.getX());
				int rel_y = (int)(obj.getPosition().getY() - mylocation.getY());
				
				int centre_x = rel_x + (obj.getWidth() / 2);
				int centre_y = rel_y + (obj.getHeight() / 2);
				
				long objrenderstart = System.currentTimeMillis();
				
				int start_x = (int) (obj.getBoundingBox().getX() - mylocation.getX());
				int start_y = (int) (obj.getBoundingBox().getY() - mylocation.getY());
				Rectangle objrect = new Rectangle(start_x, start_y, obj.getWidth(), obj.getHeight());
				Rectangle mouserect = new Rectangle((int)mousePos.getX(), (int)mousePos.getY(), 1, 1);
				
				if (mouserect.intersects(objrect)) {
					rd.setTouching(true);
					rd.setMousepos(mousePos);
				}
				
				obj.render(g2d, rel_x, rel_y, centre_x, centre_y, rd);
				
				rd.setTouching(false);
				
				if (controller.getClients().containsKey(obj.getUuid())) {
					ClientPlayer p = controller.getClients().get(obj.getUuid());
					g2d.setColor(Color.WHITE);
					
					if (p.isAdmin()) {
						g2d.setColor(Color.CYAN);
					} else {
						g2d.setColor(Color.GRAY);
					}
					
					g2d.fillOval(rel_x + ( (obj.getWidth() / 2) - (p.getName().length() * 3) ) - 20, rel_y - 31, 10, 10);
					
					g2d.setColor(Color.WHITE);
					g2d.drawString(p.getName(), rel_x + ( (obj.getWidth() / 2) - (p.getName().length() * 3) ), rel_y - 20);
				}
				
				long objrenderend = System.currentTimeMillis();
				long objrenderdelay = objrenderend - objrenderstart;
				if (!renderedbefore) {
					System.out.println("Rendered obj in " + objrenderdelay + "ms");
				}
				
				if (debug && obj instanceof CollisionHandler) {
					Rectangle rect = obj.getBoundingBox();
					start_x = (int) (rect.x - mylocation.getX());
					start_y = (int) (rect.y - mylocation.getY());
					g2d.setColor(Color.RED);
					g2d.drawRect(start_x, start_y, obj.getWidth(), obj.getHeight());
					rendereditems++;	
				}
						
//				dbg.fillRect(rel_x, rel_y, 100, 100);
			}
		}
		
		// LIGHTING
		
		if (controller.lighting) {
			renderLighting();
		}
		
		if (inv != null) {
			inv.render(g2d);
		}
		
		g2d.setColor(Color.CYAN);
		rendereditems += 3;
		rendereditems += rd.getRendereditems();
		g2d.drawString("Items rendered: " + String.valueOf(rendereditems), 40, 40);
		g2d.drawString("Render time: " + String.valueOf(rendertime), 40, 60);
		g2d.drawString("Locked: " + lockEditor, 40, 80);
		g2d.drawString("Lighting: " + controller.lighting, 40, 100);
		//g2d.drawString("Update time: " + String.valueOf(new Random().nextInt(64) + 1), 40, 80); // updatetime
		
		if (myship instanceof WeaponOwner) {
			WeaponOwner owner = (WeaponOwner) myship;
			if (owner.getPrimaryweapon() != null && owner.getPrimaryweapon() instanceof RechargeWeapon) {
				RechargeWeapon weapon = (RechargeWeapon) owner.getPrimaryweapon();
				g2d.drawString("Primary: " + Util.toPercent(weapon.getCharge(), weapon.getMaxCharge()) + "%", 40, 100);
				rendereditems++;
			}
		}
		
		if (myship instanceof GameObject) { // Doesn't work since multiplayer was added, might fix in the future
			GameObject s = (GameObject) myship;
			g2d.drawString("Health: " + Util.toPercent(s.getHealth(), s.getMaxhealth()) + "%", 40, 120);
			g2d.drawString("X: " + s.getPosition().getX(), 40, 140);
			g2d.drawString("Y: " + s.getPosition().getY(), 40, 160);
		}
		
		if (myship instanceof PlayerShip) {
			PlayerShip ps = (PlayerShip) myship;
			g2d.drawString("Speed: " + ps.getSpeed(), 40, 180);
			g2d.setColor(Color.CYAN);
			g2d.drawString("Handbrake:", 40, 200);
			if (ps.isHandbrake()) {
				g2d.setColor(Color.GREEN);
				g2d.drawString("ON", 115, 201);
			} else {
				g2d.setColor(Color.RED);
				g2d.drawString("OFF", 115, 201);
			}
		}
		
		if (editorObj != null) {
			if (editorObj.getPosition() == null) return;
			int rel_x = (int)(editorObj.getPosition().getX() - mylocation.getX());
			int rel_y = (int)(editorObj.getPosition().getY() - mylocation.getY());
			
			int centre_x = rel_x + (editorObj.getWidth() / 2);
			int centre_y = rel_y + (editorObj.getHeight() / 2);
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			
			rd.setEditor(true);
			editorObj.render(g2d, rel_x, rel_y, centre_x, centre_y, rd);
			rd.setEditor(false);
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
		
		if (activegui != null) {
			for (GuiElement e : activegui.getElements()) {
				e.render(g2d, (int)mousePos.getX(), (int) mousePos.getY());
			}
		}
		
		if (controller.getMessages().size() > 20) {
			controller.getMessages().remove(controller.getMessages().get(0));
		}
		
		int currmsg = 0;
		for (ServerMessage msg : controller.getMessages()) {
			int x = 20;
			int y = 250 + (20 * currmsg);
			g2d.setColor(Color.CYAN);
			g2d.drawString(msg.getValue(), x, y);
			currmsg++;
		}
		
		if (chatgui) {
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.drawString(currentmsg + "_", 20, 250 + (20 * 21));
		}
		
		long repaintstart = System.currentTimeMillis();
		if (!strategy.contentsLost()) {
			strategy.show();
		}
		
		long repaintend = System.currentTimeMillis();
		long repaintdelay = repaintend - repaintstart;
		
		if (!renderedbefore) {
			System.out.println("Completed repaint in " + repaintdelay + "ms");
			renderedbefore = true;
		}
		
		//end
		rd.setEnd(System.currentTimeMillis());
		rendertime = (int) rd.getTime();
		//System.out.println(rendertime);
		
	
	}
	
	public void renderLighting() {
		if (controller.lighting) {
			if (lightmap == null) {
				lightmap = new BufferedImage(PWIDTH, PHEIGHT, BufferedImage.TYPE_INT_ARGB);
			}
			
			lightcounter++;
			if (lightcounter >= 0) {
				lightcounter = 0;
				Graphics2D lg2d = (Graphics2D) lightmap.getGraphics();
				lg2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				lg2d.setColor(new Color(0, 0, 0, 255)); // Alpha: 127
				lg2d.fillRect(0, 0, PWIDTH, PHEIGHT);
				lg2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
				for (LightCircle c : lightsources) {
					lg2d.setColor(c.getColour());
					Util.fillCircle(lg2d, c.getX(), c.getY(), c.getRadius());
				}
				lg2d.dispose();
			}
			//Composite c = g2d.getComposite();
			//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
			g2d.drawImage(lightmap, 0, 0, PWIDTH, PHEIGHT, null);
			//g2d.setComposite(c);
		} else {
			lightmap = null;
		}
		
		lightsources.clear();
	}
	
	public void prepareGame(String world) throws IOException {
		new File(System.getProperty("user.home") + "/Documents/orbwarlevels/").mkdirs();
		internalserver = new GameServer(true, new File(System.getProperty("user.home") + "/Documents/orbwarlevels/" + world));
		internalserver.start();
		
		while (internalserver.getServerState() != ServerState.RUNNING) {
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		joinServer("127.0.0.1", 5555, "SP");
		controller.readytoplay = true;
	}

	public GuiScreen getActivegui() {
		return activegui;
	}

	public void setActivegui(GuiScreen activegui) {
		this.activegui = activegui;
	}
	
	public GuiScreen getMainMenu() {
		GuiScreen menuscreen = new GuiScreen();
		menuscreen.addElement(new GuiButton(new Position(1, 1), new Position(OrbWarPanel.PWIDTH - 1, 50), "S I N G L E P L A Y E R", new Runnable() {
			
			@Override
			public void run() {
				activegui = getSingleplayerMenu();
				
			}
		}, Color.CYAN).setText(Color.BLACK));
		
		/*menuscreen.addElement(new GuiButton(new Position(1, 60), new Position(OrbWarPanel.PWIDTH - 1, 110), "M U L T I P L A Y E R", new Runnable() {
			
			@Override
			public void run() {
				activegui = getMultiplayerScreen();
				
			}
		}, Color.LIGHT_GRAY).setText(Color.BLACK));*/
		
		menuscreen.addElement(new GuiButton(new Position(1, 120), new Position(OrbWarPanel.PWIDTH - 1, 170), "C O N N E C T", new Runnable() {
			
			@Override
			public void run() {
				activegui = getConnectScreen();
				
			}
		}, Color.LIGHT_GRAY).setText(Color.BLACK));
		
		return menuscreen;
	}
	
	public GuiScreen getMultiplayerScreen() {
		int current = 0;
		GuiScreen screen = new GuiScreen();
		for (MPGameInfo info : activeGames) {
			screen.addElement(new GuiButton(new Position(4, 2 + (current * 24)), new Position(OrbWarPanel.PWIDTH - 2, 20 + (current * 24)), info.getName(), new Runnable() {
				
				@Override
				public void run() {
					System.out.println("Test");
					
				}
			}, Color.LIGHT_GRAY).setText(Color.BLACK));
			current++;
		}
		
		screen.addElement(new GuiButton(new Position(4, PHEIGHT - 100), new Position(PWIDTH - 2, PHEIGHT - 30), "Back", new Runnable() {
			
			@Override
			public void run() {
				activegui = getMainMenu();
				
			}
		}, Color.DARK_GRAY));
		
		return screen;
	}
	
	public GuiScreen getEditorScreen() {
		int current = 0;
		GuiScreen screen = new GuiScreen();
		for (final Editor e : EditorManager.getEditors()) {
			screen.addElement(new GuiButton(new Position(4, 2 + (current * 24)), new Position(OrbWarPanel.PWIDTH - 2, 20 + (current * 24)), e.getName(), new Runnable() {
				
				@Override
				public void run() {
					editorObj = e.spawn(controller);
					activegui = null;
					clicked = false;
					lockEditor = false;
					
				}
			}, Color.LIGHT_GRAY).setText(Color.BLACK));
			current++;
		}
		
		screen.addElement(new GuiButton(new Position(4, PHEIGHT - 100), new Position(PWIDTH - 2, PHEIGHT - 30), "Back", new Runnable() {
			
			@Override
			public void run() {
				activegui = null;
				
			}
		}, Color.DARK_GRAY));
		
		return screen;
	}
	
	public GuiScreen getConnectScreen() {
		GuiScreen screen = new GuiScreen();
		final GuiInputField ipfield = new GuiInputField(new Position(0, 0), new Position(PWIDTH, 80));
		final GuiInputField portfield = new GuiInputField(new Position(0, 90), new Position(PWIDTH, 170));
		final GuiInputField namefield = new GuiInputField(new Position(0, 180), new Position(PWIDTH, 260));
		
		ipfield.setShadowtext("IP Address");
		portfield.setShadowtext("Port");
		namefield.setShadowtext("Username");
		
		portfield.setText("5555");
		
		final GuiLabel label = new GuiLabel(new Position(4, 280), new Position(4, 280));
		label.setColour(Color.RED);
		
		GuiButton connectbutton = new GuiButton(new Position(4, PHEIGHT - 200), new Position(PWIDTH - 2, PHEIGHT - 120), "Connect", new Runnable() {
			
			@Override
			public void run() {
				int port;
				String host;
				
				try {
					port = Integer.parseInt(portfield.getText());
					
				} catch (NumberFormatException e) {
					label.setNotice("You didn't enter a number for the port!");
					return;
				}
				
				host = ipfield.getText();
				
				try {
					
					
					if(joinServer(host, port, namefield.getText())) {
						controller.readytoplay = true;
						activegui = null;
						state = GameState.PLAYING;
					} else {
						label.setNotice(controller.namereason);
						controller.didgetncp = false;
					}
					
					
				} catch (IOException e) {
					label.setNotice("There was an error connecting to that server.");
				}
				
			}
		});
		
		GuiButton backbutton = new GuiButton(new Position(4, PHEIGHT - 100), new Position(PWIDTH - 2, PHEIGHT - 30), "Back", new Runnable() {
			
			@Override
			public void run() {
				activegui = getMainMenu();
				
			}
		});
		
		connectbutton.setFill(Color.GREEN);
		backbutton.setFill(Color.CYAN);
		
		connectbutton.setText(Color.BLACK);
		backbutton.setText(Color.BLACK);
		
		
		screen.addElement(ipfield);
		screen.addElement(portfield);
		screen.addElement(namefield);
		
		screen.addElement(connectbutton);
		screen.addElement(backbutton);
		
		screen.addElement(label);
		return screen;
	}
	
	public GuiScreen getSingleplayerMenu() {
		GuiScreen screen = new GuiScreen();
		
		final GuiInputField worldfield = new GuiInputField(new Position(0, 0), new Position(PWIDTH, 80));
		//final GuiInputField portfield = new GuiInputField(new Position(0, 90), new Position(PWIDTH, 170));
		//final GuiInputField namefield = new GuiInputField(new Position(0, 180), new Position(PWIDTH, 260));
		
		GuiButton playbutton = new GuiButton(new Position(4, PHEIGHT - 200), new Position(PWIDTH - 2, PHEIGHT - 120), "Play", new Runnable() {

			@Override
			public void run() {
				long start = System.currentTimeMillis();
				try {
					prepareGame(worldfield.getText());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();
				long timetaken = end - start;
				System.out.println("prepareGame() took " + timetaken + " ms");
				init_game = false;
				state = GameState.PLAYING;
				activegui = null;
			}
			
		});
		
		GuiButton backbutton = new GuiButton(new Position(4, PHEIGHT - 100), new Position(PWIDTH - 2, PHEIGHT - 30), "Back", new Runnable() {
			
			@Override
			public void run() {
				activegui = getMainMenu();
				
			}
		});
		
		playbutton.setFill(Color.GREEN);
		backbutton.setFill(Color.CYAN);
		
		playbutton.setText(Color.BLACK);
		backbutton.setText(Color.BLACK);
		
		worldfield.setShadowtext("Enter a world name");
		
		screen.addElement(worldfield);
		screen.addElement(playbutton);
		screen.addElement(backbutton);
		
		return screen;
		
	}
	
	public boolean joinServer(String host, int port, String name) throws IOException {
		
		if (controller != null) {
			if (!controller.host.equals(host) && controller.port != port) {
				controller.getChannel().close();
				WorldNetController c = new WorldNetController(host, port, mylocation);
				controller = c;
			}
		} else {
			WorldNetController c = new WorldNetController(host, port, mylocation);
			controller = c;
		}
		
		controller.sendPacket(new NameCheckPacket(true, name, PWIDTH, PHEIGHT));
		while (!controller.didgetncp) {
			controller.processPackets();
		}
		
		return controller.namegood;
		
	}
	
	public void quitGame() {
		System.out.println("Terminating game");
		try {
			controller.getChannel().close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		controller = null;
		state = GameState.MENU;
		System.out.println("Trying to stop internal server");
		internalserver.stopServer();
		System.out.println("Server stop called");
		try {
			internalserver.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Internal Server died");
		internalserver = null;
		mylocation = new Position(0, 0);
		activegui = getMainMenu();
		editorObj = null;
		mousePos = new Position(0, 0);
		
	}
	
	private static void registerGameAssets() {
		ObjectTransmitPacket.registerPacket();
		PositionUpdatePacket.registerPacket();
		HealthUpdatePacket.registerPacket();
		KeyPressPacket.registerPacket();
		DeleteObjectPacket.registerPacket();
		EditorTransmitPacket.registerPacket();
		ScreenUpdatePacket.registerPacket();
		ShipDataPacket.registerPacket();
		ShipRemovePacket.registerPacket();
		ChatEnterPacket.registerPacket();
		ShieldUpdatePacket.registerPacket();
		NameCheckPacket.registerPacket();
		SizeUpdatePacket.registerPacket();
		DestructionPacket.registerPacket();
		ResetPacket.registerPacket();
		DebugInfoPacket.registerPacket();
		SetLightingPacket.registerPacket();
		
		PlayerShip.registerGameObj();
		Asteroid.registerGameObj();
		Bullet.registerGameObj();
		RespawnPoint.registerGameObj();
		RespawnLaser.registerGameObj();
		Turret.registerGameObj();
		Explosion.registerGameObj();
		OliverMothership.registerGameObj();
		ShieldGenerator.registerGameObj();
		Planet.registerGameObj();
		WormHole.registerGameObj();
		Star.registerGameObj();
		EnemyDrone.registerGameObj();
		OliverGuider.registerGameObj();
		WarShip.registerGameObj();
		BombBoy.registerGameObj();
		MotherTransport.registerGameObj();
		BigAsteroid.registerGameObj();
		UniverseTransporter.registerGameObj();
		StationFloor.registerGameObj();
	}
	
	public static void registerClientAssets() {
		Asteroid.loadResources();
		PlayerShip.loadResources();
		Bullet.loadResources();
		Star.loadResources();
		HydrogenParticle.loadResources();
		Turret.loadResources();
		RespawnLaser.loadResources();
		SmokeParticle.loadResources();
		ShieldDrone.loadResources();
		OliverMothership.loadResources();
		ShieldGenerator.loadResources();
		WormHole.loadResources();
		
		Asteroid.registerEditor();
		Turret.registerEditor();
		OliverMothership.registerEditor();
		Planet.registerEditor();
		WormHole.registerEditor();
		Star.registerEditor();
		EnemyDrone.registerEditor();
		BigAsteroid.registerEditor();
		UniverseTransporter.registerEditor();
		StationFloor.registerEditor();
		
		// Item texture registration
		ItemBattery.loadResources();
	}
	
	// Used to prevent game from crashing while it is loading if you click
	@Override
	public Point getLocationOnScreen() {
		try {
			return super.getLocationOnScreen();
		} catch (IllegalComponentStateException e) {
			return new Point(0, 0);
		}
	}

}
