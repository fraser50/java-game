package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.WorldProvider;

import net.minidev.json.JSONObject;

public class Bullet extends GameObject implements CollisionHandler {
	private static BufferedImage renderimage;
	private GameObject parent;
	
	public Bullet(Position pos, WorldProvider controller, GameObject parent) {
		super(pos, controller);
		setWidth (16);
		setHeight(16);
		setMaxhealth(1000);
		setHealth(1000);
		this.parent = parent;
	}

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			if (obj == parent || (obj instanceof ShieldDrone && parent instanceof PlayerShip) ) {
				// continue;
			} else {
				obj.hurt();
				delete();
			}
			
		}
		
		
		
	}
	
	@Override
	public void update() {
		super.update();
		
		getPosition().setX(getPosition().getX() + getVelocity().getX());
		getPosition().setY(getPosition().getY() + getVelocity().getY());
		if (getHealth() <= 0) {
			delete();
		} else {
			hurt();
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
				jobj.put("x", bullet.getPosition().getX());
				
				jobj.put("y", bullet.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				
				
				Bullet bullet = new Bullet(null, null, null);
				return bullet;
			}
		};
			
		GameObjectProcessor.addParser("bullet", parser);
	}

}
