package com.castrovala.fraser.orbwar.gameobject.npc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.BombBoy;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class WarShip extends GameObject implements CollisionHandler {
	private int counter = 0;
	
	private int detonator = 200;

	public WarShip(Position pos, WorldProvider controller) {
		super(pos, controller, 20);
		setWidth(64);
		setHeight(64);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "war";
	}
	
	@Override
	public void update() {
		super.update();
		
		boolean worthit = false;
		for (GameObject obj : getNearbyObjects(10000)) {
			if (obj instanceof PlayerShip) {
				worthit = true;
			}
		}
		
		if (worthit) {
			counter++;
		}
		
		detonator--;
		if (counter >= 20) {
			getController().addObject(new BombBoy(getPosition().copy(), getController()));
			counter = 0;
		}
		
		if (detonator <= 0) {
			delete();
		}
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				WarShip war = (WarShip) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "war");
				jobj.put("x", war.getPosition().getX());
				
				jobj.put("y", war.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				WarShip war = new WarShip(null, null);
				return war;
			}
		};
			
		GameObjectProcessor.addParser("war", parser);
	}

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		AffineTransform orig = g2d.getTransform();
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(PlayerShip.getRenderimage(), rel_x, rel_y, null);
		g2d.setTransform(orig);
		
		g2d.setColor(Color.GREEN);
		int green = (int) (getHealth() * getWidth()) / getMaxhealth();
		g2d.fillRect(rel_x, rel_y - 10, green, 5);
		
		g2d.setColor(Color.RED);
		g2d.fillRect(rel_x + green, rel_y - 10, getWidth() - green, 5);
		rd.onRender(4);
	}

}
