package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class Explosion extends GameObject {
	private float size = 0;
	
	public Explosion(Position pos, WorldProvider controller, float size) {
		super(pos, controller, 20);
		this.size = size;
	}

	@Override
	public String getType() {
		return "boom";
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		Random rand = new Random(UUID.fromString(getUuid()).getMostSignificantBits());
		List<Position> points = new ArrayList<>();
		for (int i = 0;i<360;i+=2) {
			if (rand.nextBoolean() != rand.nextBoolean()) {
				float expansion = rand.nextInt(40);
				float far = expansion + (size + 18.5f);
				float healthratio = (float) ((float)getHealth()/(float)getMaxhealth());
				Position pos = Util.angleToVel(i, healthratio * far);
				points.add(pos);
			}
		}
		
		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];
		int count = 0;
		for (Position pos : points) {
			xPoints[count] = (int) ((int) rel_x + pos.getX());
			yPoints[count] = (int) ((int) rel_y + pos.getY());
			count++;
		}
		
		g2d.setColor(Color.YELLOW);
		//g2d.drawLine(xPoints[0], yPoints[0], xPoints[0], yPoints[0]);
		g2d.fillPolygon(xPoints, yPoints, xPoints.length);
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Explosion ex = (Explosion) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "boom");
				jobj.put("x", ex.getPosition().getX());
				
				jobj.put("y", ex.getPosition().getY());
				jobj.put("size", ex.getSize());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Explosion ex = new Explosion(null, null, ((Number)obj.get("size")).floatValue());
				return ex;
			}
		};
			
		GameObjectProcessor.addParser("boom", parser);
	}
	
	@Override
	public void update() {
		super.update();
		hurt();
		setChanged(false);
	}
	
	@Override
	public void clientUpdate() {
		hurt();
		
		if (getHealth() <= 0) {
			delete();
		}
	}
	
	@Override
	public boolean shouldBroadcastDeath() {
		return false;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.CONTROL;
	}

}
