package com.castrovala.fraser.orbwar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.castrovala.fraser.orbwar.gameobject.Asteroid;
import com.castrovala.fraser.orbwar.gameobject.Bullet;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.gameobject.RespawnLaser;
import com.castrovala.fraser.orbwar.gameobject.ShieldDrone;
import com.castrovala.fraser.orbwar.gameobject.Star;
import com.castrovala.fraser.orbwar.gameobject.Turret;
import com.castrovala.fraser.orbwar.gameobject.particle.HydrogenParticle;
import com.castrovala.fraser.orbwar.gameobject.particle.SmokeParticle;
import com.castrovala.fraser.orbwar.gui.GuiButton;
import com.castrovala.fraser.orbwar.gui.GuiClickable;
import com.castrovala.fraser.orbwar.gui.GuiElement;
import com.castrovala.fraser.orbwar.gui.GuiScreen;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.net.ObjectTransmitPacket;
import com.castrovala.fraser.orbwar.net.PositionUpdatePacket;
import com.castrovala.fraser.orbwar.server.GameServer;
import com.castrovala.fraser.orbwar.server.ServerState;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.util.GameState;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldNetController;
import com.castrovala.fraser.orbwar.util.WorldZone;
import com.castrovala.fraser.orbwar.weapons.RechargeWeapon;
import com.castrovala.fraser.orbwar.weapons.WeaponOwner;

@SuppressWarnings("serial")
public class OrbWarPanel extends JPanel implements Runnable {
	private static final int PWIDTH  = 1024;
	private static final int PHEIGHT = 1024;
	
	private Thread game;
	private volatile boolean running = false;
	private Graphics dbg;
	private Image dbImage = null;
	private volatile WorldNetController controller;
	private volatile Position mylocation = new Position(0, 0);
	public Controllable myship;
	public static final boolean quickstars = true;
	public static final boolean debug = true;
	private List<Position> starpoints = new ArrayList<>();
	private boolean init_game = false;
	private int rendertime;
	private int updatetime;
	private boolean renderedbefore;
	private GameState state = GameState.MENU;
	private GuiScreen activegui;
	private GameServer internalserver;
	
	public OrbWarPanel() {
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		setFocusable(true);
		requestFocus();
		
		Asteroid.loadResources();
		PlayerShip.loadResources();
		Bullet.loadResources();
		Star.loadResources();
		HydrogenParticle.loadResources();
		Turret.loadResources();
		RespawnLaser.loadResources();
		SmokeParticle.loadResources();
		ShieldDrone.loadResources();
		
		for (int i = 1; i<1000; i++) {
			starpoints.add(new Position(Util.randomRange(1, PWIDTH), Util.randomRange(1, PHEIGHT)));
		}
		
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER && state == GameState.MENU) {
					init_game = true;
				}
				if (myship == null) {
					return;
				}
				if (keyCode == KeyEvent.VK_LEFT) {
					myship.left();
				}
				if (keyCode == KeyEvent.VK_RIGHT) {
					myship.right();
				}
				if (keyCode == KeyEvent.VK_UP) {
					myship.fly();
				}
				if (keyCode == KeyEvent.VK_DOWN) {
					myship.fire();
				}
				if (keyCode == KeyEvent.VK_F) {
					myship.shield();
				}
				if (keyCode == KeyEvent.VK_H) {
					myship.toggleBrake();
				}
				if (keyCode == KeyEvent.VK_BACK_SPACE) {
					myship.suicide();
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
				
				if (keyCode == KeyEvent.VK_ESCAPE && state == GameState.PLAYING) {
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
					GuiScreen menuscreen = new GuiScreen();
					menuscreen.addElement(new GuiButton(new Position(1, 1), new Position(OrbWarPanel.PWIDTH - 1, 50), "S I N G L E P L A Y E R", new Runnable() {
						
						@Override
						public void run() {
							init_game = true;
							
						}
					}));
					activegui = menuscreen;
						
				}
			}
		});
		
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
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
			public void mouseClicked(MouseEvent ev) {
				if (activegui != null) {
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
					}
				}
				
			}
		});
		
		// Create menu GUI
		GuiScreen menuscreen = new GuiScreen();
		menuscreen.addElement(new GuiButton(new Position(1, 1), new Position(OrbWarPanel.PWIDTH - 1, 50), "S I N G L E P L A Y E R", new Runnable() {
			
			@Override
			public void run() {
				init_game = true;
				
			}
		}));
		activegui = menuscreen;
	}
	
	public static void main(String[] args) {
		
		PositionUpdatePacket.registerPacket();
		ObjectTransmitPacket.registerPacket();
		
		PlayerShip.registerGameObj();
		
		JFrame frame = new JFrame("OrbWar");
		OrbWarPanel panel = new OrbWarPanel();
		frame.add(panel);
		frame.setSize(PWIDTH, PHEIGHT);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.addNotify();
	}
	
	public void addNotify() {
		super.addNotify();
		startGame();
	}
	
	public void startGame() {
		if (game == null || !running) {
			game = new Thread(this);
			game.start();
		}
	}
	
	public void stopGame() {
		running = false;
	}
	
	public void run() {
		running = true;
		while (running) {
			int updatespeed = 1000 / 60;
			if (state == GameState.MENU) {
				if (init_game) {
					long start = System.currentTimeMillis();
					prepareGame();
					long end = System.currentTimeMillis();
					long timetaken = end - start;
					System.out.println("prepareGame() took " + timetaken + " ms");
					init_game = false;
					state = GameState.PLAYING;
					activegui = null;
				}
				gameUpdate();
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
			
			GameObject obj = (GameObject) myship;
			if (myship == null) {
				gameUpdate();
				gameRender();
				try {
					Thread.sleep(updatespeed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			if (mylocation.getX() - obj.getPosition().getX() >= 1000000 || mylocation.getY() - obj.getPosition().getY() >= 1000000 ||
				mylocation.getX() - obj.getPosition().getX() <= -1000000 || mylocation.getY() - obj.getPosition().getY() <= -1000000	) {
				mylocation.setX(obj.getPosition().getX() - (PWIDTH / 2) );
				mylocation.setY(obj.getPosition().getY() - (PHEIGHT / 2) );
			}
			
			gameUpdate();
			
			Position toscreen = Util.coordToScreen(obj.getPosition(), mylocation);
			long screen_x = (long) toscreen.getX();
			long screen_y = (long) toscreen.getY();
			
			while (screen_x >= PWIDTH - 50 || screen_x <= 50 || screen_y >= PHEIGHT - 50 || screen_y <= 50) {
				if (screen_x >= PWIDTH - 50) {
					mylocation.setX(mylocation.getX() + 1);
				}
				
				if (screen_x <= 50) {
					mylocation.setX(mylocation.getX() - 1);
				}
				
				if (screen_y >= PHEIGHT - 50) {
					mylocation.setY(mylocation.getY() + 1);
				}
				
				if (screen_y <= 50) {
					mylocation.setY(mylocation.getY() - 1);
				}
				
				toscreen = Util.coordToScreen(obj.getPosition(), mylocation);
				screen_x = (long) toscreen.getX();
				screen_y = (long) toscreen.getY();
			}
			
			gameRender();
			
			try {
				//Thread.sleep(20);
				Thread.sleep(updatespeed);
			} catch (InterruptedException ex){}
			
		}
		System.exit(0);
	}
	
	private void gameUpdate() {
		if (running) {
			if (state == GameState.MENU || state == GameState.PAUSED) {
				return;
			}
			
		}
		
		try {
			controller.processPackets();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		controller.updateGame();
	}
	
	public void gameRender() {
		RenderDebug rd = new RenderDebug(mylocation);
		//start
//		System.out.println("renderer called");
		if (dbImage == null) {
			dbImage = createImage(PWIDTH, PHEIGHT);
			if (dbImage == null) {
				System.out.println("dbImage is null");
				return;
			} else {
				dbg = dbImage.getGraphics();
				
				dbg.setColor(Color.BLACK);
				dbg.fillRect(0, 0, PWIDTH, PHEIGHT);
				
			}
		}
		Graphics2D g2d = (Graphics2D) dbImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
//		System.out.println("Adding all zones");
		dbg.setColor(Color.BLACK);
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);
		dbg.drawString("Fixer", 5, 5);
		int rendereditems = 0;
		for (Position star : starpoints) {
			dbg.setColor(Color.WHITE);
			int offsetx = 0;
			int offsety = 0;
			int newposx = (int) star.getX();
			int newposy = (int) star.getY();
			if (state == GameState.PLAYING) {
				offsetx = (int) (mylocation.getX() / 4);
				offsety = (int) (mylocation.getY() / 4);
				
				newposx = (int) (star.getX() + offsetx);
				newposy = (int) (star.getY() + offsety);
				
				if (newposx > OrbWarPanel.PWIDTH) {
					int xdiv = newposx / OrbWarPanel.PWIDTH;
					if (xdiv < 0) {
						xdiv = -xdiv;
					}
					newposx = (int) (newposx - (OrbWarPanel.PWIDTH / xdiv));
					newposx = OrbWarPanel.PWIDTH - newposx;
					
				} else {
					newposx = OrbWarPanel.PWIDTH - newposx;
				}
				
				if (newposx < 0) {
					//int xdiv = newposx / OrbWarPanel.PWIDTH;
					//newposx = (int) (newposx - (OrbWarPanel.PWIDTH / xdiv));
					//int xdiv = -(newposx / OrbWarPanel.PWIDTH);
					//newposx = (int) (newposx - (OrbWarPanel.PWIDTH / xdiv));
					
					//int xdiv = -(newposx / OrbWarPanel.PWIDTH);
					//newposx = (int) (newposx - (OrbWarPanel.PWIDTH / xdiv));
				}
				
				if (offsety > OrbWarPanel.PHEIGHT) {
					offsety /= OrbWarPanel.PHEIGHT;
				}
			}
			dbg.drawLine( newposx, newposy, newposx, newposy );
			rendereditems++;
		}
		
		if (activegui != null) {
			for (GuiElement e : activegui.getElements()) {
				e.render(g2d);
			}
		}
		
		if (state == GameState.MENU) {
			//g2d.setColor(Color.WHITE);
			//g2d.drawRect(200, 50, (PWIDTH - 200) - 200, (PHEIGHT - 800));
			//g2d.drawString("S I N G L E P L A Y E R", 400, 150);
			repaint();
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
				chosenzones.add(zone);
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
				if (rel_x >= 0 && rel_x <= PWIDTH && rel_y >= 0 && rel_y <= PHEIGHT) {
					stages.get(obj.getRenderStage()).add(obj);
					
					/*int centre_x = rel_x + (obj.getWidth() / 2);
					int centre_y = rel_y + (obj.getHeight() / 2);
					
					long objrenderstart = System.currentTimeMillis();
					obj.render(g2d, rel_x, rel_y, centre_x, centre_y, rd);
					long objrenderend = System.currentTimeMillis();
					long objrenderdelay = objrenderend - objrenderstart;
					if (!renderedbefore) {
						System.out.println("Rendered obj in " + objrenderdelay + "ms");
					}
					
					if (debug && obj instanceof CollisionHandler) {
						Rectangle rect = obj.getBoundingBox();
						int start_x = (int) (rect.x - mylocation.getX());
						int start_y = (int) (rect.y - mylocation.getY());
						int end_x = (int) ( (rect.x + rect.getWidth()) - mylocation.getX() );
						int end_y = (int) ( (rect.y + rect.getHeight()) - mylocation.getY() );
						dbg.setColor(Color.RED);
						dbg.drawLine(start_x, start_y, end_x, start_y);
						dbg.drawLine(start_x, end_y, end_x, end_y);
						dbg.drawLine(start_x, start_y, start_x, end_y);
						dbg.drawLine(end_x, start_y, end_x, end_y);
						rendereditems += 4;	
					}*/
							
//					dbg.fillRect(rel_x, rel_y, 100, 100);
				}
			}
		}
		if (debug) {
			//This is for debugging purposes
			for (WorldZone zone : chosenzones) {
				int start_x = (int) (zone.getStartPoint().getX() - mylocation.getX());
				int start_y = (int) (zone.getStartPoint().getY() - mylocation.getY());
				
				int end_x = (int) (zone.getEndPoint().getX() - mylocation.getX());
				int end_y = (int) (zone.getEndPoint().getY() - mylocation.getY());
				dbg.setColor(Color.CYAN);
				dbg.drawLine(start_x, start_y, end_x, start_y);
				dbg.drawLine(start_x, end_y, end_x, end_y);
				dbg.drawLine(start_x, start_y, start_x, end_y);
				dbg.drawLine(end_x, start_y, end_x, end_y);
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
				obj.render(g2d, rel_x, rel_y, centre_x, centre_y, rd);
				long objrenderend = System.currentTimeMillis();
				long objrenderdelay = objrenderend - objrenderstart;
				if (!renderedbefore) {
					System.out.println("Rendered obj in " + objrenderdelay + "ms");
				}
				
				if (debug && obj instanceof CollisionHandler) {
					Rectangle rect = obj.getBoundingBox();
					int start_x = (int) (rect.x - mylocation.getX());
					int start_y = (int) (rect.y - mylocation.getY());
					int end_x = (int) ( (rect.x + rect.getWidth()) - mylocation.getX() );
					int end_y = (int) ( (rect.y + rect.getHeight()) - mylocation.getY() );
					dbg.setColor(Color.RED);
					dbg.drawLine(start_x, start_y, end_x, start_y);
					dbg.drawLine(start_x, end_y, end_x, end_y);
					dbg.drawLine(start_x, start_y, start_x, end_y);
					dbg.drawLine(end_x, start_y, end_x, end_y);
					rendereditems += 4;	
				}
						
//				dbg.fillRect(rel_x, rel_y, 100, 100);
			}
		}
		
		dbg.setColor(Color.CYAN);
		rendereditems += 3;
		rendereditems += rd.getRendereditems();
		dbg.drawString("Items rendered: " + String.valueOf(rendereditems), 40, 40);
		dbg.drawString("Render time: " + String.valueOf(rendertime), 40, 60);
		dbg.drawString("Update time: " + String.valueOf(updatetime), 40, 80);
		
		if (myship instanceof WeaponOwner) {
			WeaponOwner owner = (WeaponOwner) myship;
			if (owner.getPrimaryweapon() != null && owner.getPrimaryweapon() instanceof RechargeWeapon) {
				RechargeWeapon weapon = (RechargeWeapon) owner.getPrimaryweapon();
				dbg.drawString("Primary: " + Util.toPercent(weapon.getCharge(), weapon.getMaxCharge()) + "%", 40, 100);
				rendereditems++;
			}
		}
		
		if (myship instanceof GameObject) {
			GameObject s = (GameObject) myship;
			dbg.drawString("Health: " + Util.toPercent(s.getHealth(), s.getMaxhealth()) + "%", 40, 120);
			dbg.drawString("X: " + s.getPosition().getX(), 40, 140);
			dbg.drawString("Y: " + s.getPosition().getY(), 40, 160);
		}
		
		if (myship instanceof PlayerShip) {
			PlayerShip ps = (PlayerShip) myship;
			dbg.drawString("Speed: " + ps.getSpeed(), 40, 180);
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
		
//		System.out.println("Loaded> " + rendereditems);
		long repaintstart = System.currentTimeMillis();
		repaint();
		long repaintend = System.currentTimeMillis();
		long repaintdelay = repaintend - repaintstart;
		
		if (!renderedbefore) {
			System.out.println("Completed repaint in " + repaintdelay + "ms");
			renderedbefore = true;
		}
		
		//end
		rd.setEnd(System.currentTimeMillis());
		rendertime = (int) rd.getTime();
		
	
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (dbImage != null) {
			g.drawImage(dbImage, 0, 0, null);
		}
	}
	
	public void prepareGame() {
		
		internalserver = new GameServer(true);
		internalserver.start();
		while (internalserver.getServerState() != ServerState.RUNNING) {
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		WorldNetController c = new WorldNetController("127.0.0.1", 5555);
		controller = c;
		
		/*WorldController c = internalserver.getGameThread().getController();
		c.getZone(new Position(0, 0));
		c.getZone(new Position(0, 1));
		
		c.getZone(new Position(0, 0));
		c.getZone(new Position(1, 0));
		*/
		
		//controller = new WorldController();
		/*for (int i = 1;i <= 1000; i++) {
			float x = Util.randomRange(0, PWIDTH);
			float y = Util.randomRange(0, PHEIGHT);
			controller.getStarpoints().add(new Position(x, y));
		}
		
		Position pos = new Position(256, 256); // 256, 256
		PlayerShip ship = new PlayerShip(pos, controller);
		Star star = new Star(new Position(512, 512), controller);
		
		Position turretpos = new Position(128, 128);
		@SuppressWarnings("unused")
		Turret t = new Turret(turretpos, controller);
		
		//RespawnPoint laser = new RespawnPoint(new Position(700, 700), controller);
		
		controller.addObject(ship);
		controller.addObject(star);
		//controller.addObject(t);
		//controller.addObject(laser);
		myship = ship;
		activegui = null;
		starpoints = controller.getStarpoints();
		*/
	}

	public GuiScreen getActivegui() {
		return activegui;
	}

	public void setActivegui(GuiScreen activegui) {
		this.activegui = activegui;
	}

}
