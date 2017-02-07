package com.castrovala.fraser.orbwar.gameobject.npc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.gameobject.PlayerShip;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class EnemyDrone extends GameObject {
	private GameObject target;
	private double speed = 2;
	@SuppressWarnings("unused")
	private int strategy = 0; // 0 = circle, 1 = flee

	public EnemyDrone(Position pos, WorldProvider controller) {
		super(pos, controller);
	}

	@Override
	public String getType() {
		return "enemydronet1";
	}
	
	@Override
	public void update() {
		if (target == null) {
			for (GameObject obj : getNearbyObjects(100)) {
				if (obj instanceof PlayerShip) {
					target = obj;
					return;
				}
			}
			return;
		}
		
		if (target.isDeleted() || target.isCleaned()) {
			target = null;
			return;
		}
		
		//System.out.println("Has target");
		getPosition().add(Util.angleToVel(getRotation(), (float) speed));
		
		double distancebetween = this.distance(target);
		double distanceaim = 10;
		
		List<Entry<Position, Float>> choices = new ArrayList<>();
		Position mightpos;
		
		float rot = 0;
		mightpos = getPosition().copy().add(Util.angleToVel((float)Util.fixAngle(getRotation() + rot), (float)speed));
		choices.add(new AbstractMap.SimpleEntry<Position, Float>(mightpos, rot));
		
		rot = 0.8f;
		mightpos = getPosition().copy().add(Util.angleToVel((float)Util.fixAngle(getRotation() + rot), (float)speed));
		choices.add(new AbstractMap.SimpleEntry<Position, Float>(mightpos, rot));
		
		rot = -0.8f;
		mightpos = getPosition().copy().add(Util.angleToVel((float)Util.fixAngle(getRotation() + rot), (float)speed));
		choices.add(new AbstractMap.SimpleEntry<Position, Float>(mightpos, rot));
		
		Position bestchoice = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		float bestrot = 0;
		
		for (Entry<Position, Float> e : choices) {
			Position p = e.getKey();
			rot = e.getValue();
			double dist = Util.distance(p, target.getPosition());
			double bestdist = Util.distance(bestchoice, target.getPosition());
			System.out.println("Best dist:" + bestdist);
			System.out.println("distaim:" + distanceaim);
			System.out.println("dist:" + dist);
			System.out.println("Test rot: " + rot);
			double diff = dist / distanceaim;
			if ( dist < bestdist) {
				bestchoice = p.copy();
				bestrot = rot;
				System.out.println("Good rot: " + rot);
			}
			
			//bestrot = 1;
			
			
		}
		setRotation((float) Util.fixAngle(getRotation() + bestrot));
		
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				EnemyDrone ed = (EnemyDrone) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "enemydronet1");
				jobj.put("x", ed.getPosition().getX());
				
				jobj.put("y", ed.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				EnemyDrone ed = new EnemyDrone(null, null);
				return ed;
			}
		};
		
		GameObjectProcessor.addParser("enemydronet1", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Enemy (WIP)") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new EnemyDrone(null, controller);
			}
		};
		
		EditorManager.addEditor(e);
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
	
	@Override
	public boolean shouldRotate() {
		return true;
	}

}
