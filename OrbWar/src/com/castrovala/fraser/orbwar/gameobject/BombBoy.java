package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;

import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

import net.minidev.json.JSONObject;

public class BombBoy extends GameObject implements CollisionHandler {
	private GameObject target;

	public BombBoy(Position pos, WorldProvider controller) {
		super(pos, controller, 500);
		setWidth(16);
		setHeight(16);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "bomb";
	}

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			if (obj == target) {
				obj.hurt(1);
				delete();
			}
		}
		
	}
	
	@Override
	public void update() {
		super.update();
		hurt();
		if (target == null) {
			for (GameObject obj : getNearbyObjects(500)) {
				if (obj instanceof PlayerShip) {
					target = obj;
				}
			}
			return;
		}
		
		getPosition().setX(target.getPosition().getX() > getPosition().getX() ? getPosition().getX() + 2 : getPosition().getX());
		getPosition().setX(target.getPosition().getX() < getPosition().getX() ? getPosition().getX() - 2 : getPosition().getX());
		
		getPosition().setY(target.getPosition().getY() > getPosition().getY() ? getPosition().getY() + 2 : getPosition().getY());
		getPosition().setY(target.getPosition().getY() < getPosition().getY() ? getPosition().getY() - 2 : getPosition().getY());
		
		if (target.isDeleted()) {
			delete();
		}
		
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				BombBoy bomb = (BombBoy) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "bomb");
				jobj.put("x", bomb.getPosition().getX());
				
				jobj.put("y", bomb.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				BombBoy bomb = new BombBoy(null, null);
				return bomb;
			}
		};
			
		GameObjectProcessor.addParser("bomb", parser);
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(RespawnLaser.getRenderimage(), rel_x, rel_y, null);
		rd.onRender();
	}

}
