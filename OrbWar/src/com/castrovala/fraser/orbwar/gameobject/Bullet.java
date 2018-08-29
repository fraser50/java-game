package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.LightCircle;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class Bullet extends GameObject implements CollisionHandler {
	private static BufferedImage renderimage;
	private GameObject parent;
	private Position initialpos;
	private long timeborn;
	
	public Bullet(Position pos, WorldProvider controller, GameObject parent) {
		super(pos, controller);
		setWidth (16);
		setHeight(16);
		setMaxhealth(150);
		setHealth(150);
		this.parent = parent;
		
		if (getPosition() != null) {
			setInitialpos(getPosition().copy());
		}
		
		timeborn = System.currentTimeMillis();
	}

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			
			if (obj == parent || (obj instanceof ShieldDrone && parent instanceof PlayerShip)) {
				// continue;
			} else {
				
				if (obj instanceof WormHole) {
					obj.setWidth(obj.getWidth() + 100);
					obj.setHeight(obj.getHeight() + 100);
				}
				
				if (obj instanceof BigAsteroid) {
					continue;
				}
				
				obj.hurt(5); // 1
				delete();
			}
			
		}
		
	}
	
	@Override
	public void update() {
		super.update();
		
		long timepassed = System.currentTimeMillis() - timeborn;
		int loopsince = (int) (timepassed / (1000 / 60)); // 1000 / 60
		
		double dx = loopsince * getVelocity().getX();
		double dy = loopsince * getVelocity().getY();
		
		getPosition().setX(getInitialpos().getX() + dx);
		getPosition().setY(getInitialpos().getY() + dy);
		
		if (getHealth() <= 0) {
			delete();
		} else {
			hurt();
			setChanged(false);
		}
		
		getPosition().setEdited(false);
	}
	
	@Override
	public void clientUpdate() {
		super.update();
		
		long timepassed = System.currentTimeMillis() - timeborn;
		int loopsince = (int) (timepassed / (1000 / 60));
		
		double dx = loopsince * getVelocity().getX();
		double dy = loopsince * getVelocity().getY();
		
		getPosition().setX(getInitialpos().getX() + dx);
		getPosition().setY(getInitialpos().getY() + dy);
		
		if (getHealth() <= 0) {
			delete();
		} else {
			hurt();
			setChanged(false);
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(renderimage, rel_x, rel_y, null);
		rd.onRender();
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/bullet.png")));
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

	@Override
	public String getType() {
		return "bullet";
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Bullet bullet = (Bullet) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "bullet");
				jobj.put("x", bullet.getInitialpos().getX());
				
				jobj.put("y", bullet.getInitialpos().getY());
				
				jobj.put("velx", bullet.getVelocity().getX());
				jobj.put("vely", bullet.getVelocity().getY());
				
				jobj.put("ix", bullet.getInitialpos().getX());
				jobj.put("iy", bullet.getInitialpos().getY());
				
				jobj.put("birth", bullet.getTimeborn());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Bullet bullet = new Bullet(null, null, null);
				bullet.getVelocity().setX(((Number) obj.get("velx")).doubleValue());
				bullet.getVelocity().setY(((Number) obj.get("vely")).doubleValue());
				bullet.setTimeborn(((Number) obj.get("birth")).longValue());
				bullet.setInitialpos(new Position(((Number) obj.get("ix")).doubleValue(), ((Number) obj.get("iy")).doubleValue()));
				return bullet;
			}
		};
			
		GameObjectProcessor.addParser("bullet", parser);
	}

	public Position getInitialpos() {
		return initialpos;
	}

	public void setInitialpos(Position initialpos) {
		this.initialpos = initialpos;
	}

	public long getTimeborn() {
		return timeborn;
	}

	public void setTimeborn(long timeborn) {
		this.timeborn = timeborn;
	}
	
	@Override
	public boolean shouldSave() {
		return false;
	}

}
