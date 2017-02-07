package com.castrovala.fraser.orbwar.gameobject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class OliverGuider extends GameObject implements CollisionHandler {
	private GameObject parent;
	private String parentuuid;

	public OliverGuider(Position pos, WorldProvider controller) {
		super(pos, controller, 200);
		setWidth(1);
		setHeight(1);
	}

	@Override
	public String getType() {
		return "guide";
	}
	
	@Override
	public void update() {
		super.update();
		hurt();
		
		PlayerShip ship = null;
		for (GameObject obj : getNearbyObjects(500)) {
			if (obj instanceof PlayerShip) {
				ship = (PlayerShip) obj;
				break;
			}
		}
		if (ship == null) {
			return;
		}
		
		Position best = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		for (int i = 1;i<3;i++) {
			Position p = getPosition().copy().add(new Position(Util.randomRange(-5, 5), Util.randomRange(-5, 5)));
			if (Util.distance(p, ship.getPosition().copy().add(new Position(32, 32))) < Util.distance(best, ship.getPosition().copy().add(new Position(32, 32)))) {
				best = p;
			}
		}
		
		setPosition(best);
		
	}

	public GameObject getParent() {
		return parent;
	}

	public void setParent(GameObject parent) {
		this.parent = parent;
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				OliverGuider guide = (OliverGuider) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "guide");
				jobj.put("x", guide.getPosition().getX());
				
				jobj.put("y", guide.getPosition().getY());
				jobj.put("parent", guide.getParent().getUuid());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				OliverGuider g = new OliverGuider(null, null);
				g.setParentuuid((String)obj.get("parent"));
				return g;
			}
		};
		
		GameObjectProcessor.addParser("guide", parser);
	}

	public String getParentuuid() {
		return parentuuid;
	}

	public void setParentuuid(String parentuuid) {
		this.parentuuid = parentuuid;
	}
	
	@Override
	public void clientUpdate() {
		if (parent == null) {
			parent = getController().getGameObject(parentuuid);
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		if (parent != null) {
			Position spos = Util.coordToScreen(parent.getPosition().copy().add(new Position(parent.getWidth() / 2, parent.getHeight() / 2)), rd.getRenderloc());
			spos.add(new Position(3, -6));
			g2d.setColor(Color.RED);
			Stroke s = g2d.getStroke();
			g2d.setStroke(new BasicStroke(5f));
			g2d.drawLine(centre_x, centre_y, (int)spos.getX(), (int)spos.getY());
			g2d.setStroke(s);
		}
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.CONTROL;
	}

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			if (obj instanceof PlayerShip) {
				obj.hurt();
			}
		}
		
	}

}
