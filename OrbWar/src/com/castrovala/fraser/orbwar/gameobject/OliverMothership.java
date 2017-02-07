package com.castrovala.fraser.orbwar.gameobject;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.gameobject.npc.WarShip;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.net.ShieldUpdatePacket;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.NetworkPlayer;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldController;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class OliverMothership extends GameObject implements CollisionHandler {
	private boolean shield = true;
	private boolean cstate = false;
	private static BufferedImage renderimage;
	private int offlinetimer = 0;
	private List<ShieldGenerator> drones = new ArrayList<>();
	private boolean firstrun = true;
	private final float distanceaway = 200;
	private int timer = 0;
	
	private int missiletimer = 0;

	public OliverMothership(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(256);
		setHeight(256);
	}

	@Override
	public String getType() {
		return "ollie";
	}
	
	@Override
	public void hurt(int damage) {
		if (!shield) {
			super.hurt(damage);
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		if (shield) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
			g2d.setColor(Color.CYAN);
			g2d.fillOval(rel_x - 40, rel_y - 40, getWidth() + 80, getHeight() + 80);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
		
		g2d.setColor(Color.GREEN);
		int green = (int) (getHealth() * getWidth()) / getMaxhealth();
		g2d.fillRect(rel_x, rel_y - 60, green, 5);
		
		g2d.setColor(Color.RED);
		g2d.fillRect(rel_x + green, rel_y - 60, getWidth() - green, 5);
		rd.onRender(4);
	}
	
	@Override
	public void update() {
		super.update();
		
		timer++;
		if (timer >= 500) {
			OliverGuider g = new OliverGuider(getPosition().copy().add(new Position(128, 128)), getController());
			g.setParent(this);
			getController().addObject(g);
			timer = 0;
		}
		
		if (firstrun) {
			addGenerators();
			for (ShieldGenerator g : drones) {
				g.getControl().setSpeed(distanceaway);
			}
			firstrun = false;
		}
		
		for (ShieldGenerator g : drones) {
			g.getControl().setSpeed(g.getControl().getSpeed() + 1);
			if (g.getControl().getSpeed() > distanceaway) {
				g.getControl().setSpeed(distanceaway);
			}
		}
		
		if (getDrones().size() == 0) {
			setShield(false);
		}
		
		if (!shield) {
			offlinetimer++;
		}
		
		if (offlinetimer >= 200) {
			setShield(true);
			offlinetimer = 0;
			addGenerators();
		}
		
		if (cstate) {
			cstate = false;
			ShieldUpdatePacket p = new ShieldUpdatePacket(getUuid(), shield);
			WorldController c = (WorldController) getController();
			for (NetworkPlayer pl : c.getServer().getPlayers()) {
				pl.sendPacket(p);
			}
		}
		
		missiletimer++;
		if (missiletimer >= 50) {
			Random r = ThreadLocalRandom.current();
			float rotation = r.nextInt(361);
			Position adder = Util.angleToVel(rotation, 200f + r.nextInt(100));
			WarShip war = new WarShip(getPosition().copy().add(adder), getController());
			getController().addObject(war);
			missiletimer = 0;
		}
		
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				OliverMothership boss = (OliverMothership) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "ollie");
				jobj.put("x", boss.getPosition().getX());
				
				jobj.put("y", boss.getPosition().getY());
				jobj.put("shield", boss.isShield());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				OliverMothership boss = new OliverMothership(null, null);
				boss.setShield((boolean) obj.get("shield"));
				return boss;
			}
		};
		
		GameObjectProcessor.addParser("ollie", parser);
	}
	
	public void setShield(boolean shield) {
		this.shield = shield;
		cstate = true;
		
	}

	public static void registerEditor() {
		Editor e = new Editor("Mothership") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				OliverMothership oms = new OliverMothership(new Position(0, 0), controller);
				oms.setShield(true);
				return oms;
			}
		};
		
		EditorManager.addEditor(e);
	}

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	public static void loadResources() {
		ClassLoader cl = OliverMothership.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/ollie.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setRenderimage(BufferedImage r) {
		renderimage = r;
	}
	
	public static BufferedImage getRenderimage() {
		return renderimage;
	}
	
	public boolean isShield() {
		return shield;
	}
	
	@Override
	public void death() {
		Explosion boom = new Explosion(getPosition().copy().add(new Position(128, 128)), getController());
		getController().addObject(boom);
	}
	
	private void addGenerators() {
		for (int i = 0; i < 360; i+=90) {
			ShieldGenerator gen = new ShieldGenerator(getPosition().copy(), getController());
			OrbitControl oc = new OrbitControl(this, 1, 0.25f);
			oc.setRotation(i);
			oc.setTarget(gen);
			gen.setControl(oc);
			gen.setBossid(getUuid());
			getController().addObject(gen);
			drones.add(gen);
		}
	}
	
	public List<ShieldGenerator> getDrones() {
		return drones;
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.SPACEOBJECTS;
	}
}
