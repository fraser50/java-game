package com.castrovala.fraser.orbwar.gameobject;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class TutorialField extends GameObject {
	private List<GameObject> store = new ArrayList<>();
	
	public TutorialField(Position pos, WorldProvider controller) {
		super(pos, controller, 20);
		setWidth(200);
		setHeight(200);
	}
	
	@Override
	public String getType() {
		return "tutorial";
	}
	
	@Override
	public void update() {
		double centre_x = getPosition().getX() + getWidth() / 2;
		double centre_y = getPosition().getY() + getHeight() / 2;
		double radius = getWidth() / 2;
		
		for (GameObject obj : store) {
			double distance = Util.distance(new Position(centre_x, centre_y), obj.getPosition());
			if (distance > radius) {
				float angle = Util.targetRadius(obj.getPosition(), new Position(centre_x, centre_y));
				obj.setPosition(new Position(centre_x, centre_y).add(Util.angleToVel(angle, (float)radius)));
			}
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
		g2d.setColor(Color.CYAN);
		g2d.fillOval(rel_x, rel_y, getWidth(), getHeight());
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2d.setColor(Color.RED);
		g2d.drawLine(centre_x, centre_y, centre_x, centre_y);
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				TutorialField field = (TutorialField) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "tutorial");
				jobj.put("x", field.getPosition().getX());
				
				jobj.put("y", field.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				
				
				TutorialField field = new TutorialField(null, null);
				return field;
			}
		};
			
		GameObjectProcessor.addParser("tutorial", parser);
	}

	public List<GameObject> getStore() {
		return store;
	}

}
