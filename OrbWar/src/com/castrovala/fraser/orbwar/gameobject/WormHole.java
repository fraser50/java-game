package com.castrovala.fraser.orbwar.gameobject;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WormHoleData;
import com.castrovala.fraser.orbwar.util.WormHoleType;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldController;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class WormHole extends GameObject implements CollisionHandler {
	private WormHoleType wormType;
	private WormHoleData wormData;
	private int cooldown = 0;
	private static BufferedImage renderimage;
	private int animationstate = 0;
	
	public WormHole(Position pos, WorldProvider controller, WormHoleType wormType) {
		super(pos, controller);
		
		wormType = WormHoleType.WORM;
		
		this.wormType = wormType;
		
		setWidth(128);
		setHeight(128);
	}
	
	@Override
	public void afterBirth() {
		if (!(getController() instanceof WorldController))
			return;
		
		WorldController wc = (WorldController) getController();
		if (wc.getWormHoleDataMap().containsKey(getUuid()))
			return;
		
		WormHoleData data = new WormHoleData(Util.toZoneCoords(getPosition()), getUuid(), wormType);
		wc.getWormHoleDataList().add(data);
		wc.getWormHoleDataMap().put(getUuid(), data);
		
		wormData = data;
	}
	
	@Override
	public void onCollision(GameObject[] objects) {
		if (cooldown > 0)
			return;
		
		if (!(getController() instanceof WorldController))
			return;
		
		WorldController wc = (WorldController) getController();
		
		if (wc.getWormHoleDataList().size() <= 1)
			return;
		
		Random rand = new Random();
		
		List<WormHole> usedHoles = new ArrayList<>();
		for (GameObject obj : objects) {
			
			if (obj instanceof Bullet)
				continue;
			
			WormHole dest;
			while (true) {
				String uuid = wc.getWormHoleDataList().get(rand.nextInt(wc.getWormHoleDataList().size())).getWormholeID();
				dest = (WormHole) wc.getGameObject(uuid);
				if (dest.getWormType() == WormHoleType.WORM && dest.cooldown == 0 && dest != this) {
					break;
				}
				
			}
			usedHoles.add(dest);
			obj.setPosition(dest.getPosition().copy());
		}
		
		for (WormHole hole : usedHoles) {
			hole.cooldown = 100;
			cooldown = 100;
		}
		
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		if (rd.isEditor()) {
			AffineTransform orig = g2d.getTransform();
			g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
			g2d.drawImage(getRenderimage(), rel_x, rel_y, getWidth(), getHeight(), null);
			g2d.setTransform(orig);
			return;
		}
		
		int[] xpoints = new int[360];
		int[] ypoints = new int[360];
		
		for (int i = 0; i < 360; i++) {
			double sinewave = Math.sin(Math.toRadians(((animationstate * 7) + (i * 12)) / 1)) * 7; // i * 120 for a laugh (Default: i * 12)
			// MAGNITUDE = 32
			float magnitude = (float) ((getWidth() / 2) + sinewave); // 52 + sinewave
			Position pos = Util.coordToScreen(getPosition().copy().add(new Position(getWidth() / 2, getHeight() / 2)).add(Util.angleToVel(i, magnitude)), rd.getRenderloc());
			xpoints[i] = (int) pos.getX();
			ypoints[i] = (int) pos.getY();
		}
		
		animationstate++;
		
		//if (animationstate > 359) {
		//	animationstate = 0;
		//}
		
		g2d.setColor(new Color(5, 35, 75));
		Composite c = g2d.getComposite();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f));
		g2d.fillPolygon(xpoints, ypoints, 360);
		g2d.setComposite(c);
		g2d.setColor(Color.GREEN);
		g2d.drawPolygon(xpoints, ypoints, 360);
		
	}
	
	public static void setRenderimage(BufferedImage r) {
		renderimage = r;
	}

	public static BufferedImage getRenderimage() {
		return renderimage;
	}
	
	public static void loadResources() {
		ClassLoader cl = WormHole.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/wormhole.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "wormhole";
	}

	public WormHoleType getWormType() {
		return wormType;
	}

	public void setWormType(WormHoleType wormType) {
		this.wormType = wormType;
	}

	public WormHoleData getWormData() {
		return wormData;
	}

	public void setWormData(WormHoleData wormData) {
		this.wormData = wormData;
	}
	
	@Override
	public void hurt(int damage) {
		return;
	}
	
	@Override
	public void clientUpdate() {
		//setRotation((float)Util.fixAngle(getRotation() + 2));
		
		//Position vel = Util.angleToVel(getRotation(), ((getWidth() / 2) / 64) + 1.25f);
		//Spark s = new Spark(getPosition().copy().add(vel.copy().multiply(40)).add(new Position(getWidth() / 2, getHeight() / 2)), getController());
		//double vm = getRotation() % 12 == 0 ? 1 : 0.25;
		//s.setVelocity(vel.multiply(0.25));
		//s.setVelocity(Util.angleToVel(getRotation(), 0.25f));
		//getController().addObject(s);
	}
	
	@Override
	public void update() {
		if (cooldown > 0) 
			cooldown--;
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				WormHole hole = (WormHole) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "wormhole");
				jobj.put("x", hole.getPosition().getX());
				
				jobj.put("y", hole.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				WormHole hole = new WormHole(null, null, null);
				return hole;
			}
		};
		
		GameObjectProcessor.addParser("wormhole", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Wormhole") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new WormHole(null, null, WormHoleType.WORM);
			}
		};
		
		EditorManager.addEditor(e);
	}
	
	@Override
	public boolean shouldSave() {
		return false;
	}

}
