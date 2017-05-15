package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class MotherTransport extends GameObject {
	OrbitControl orbit;

	public MotherTransport(Position pos, WorldProvider controller, OrbitControl orbit) {
		super(pos, controller);
		this.orbit = orbit;
		
		setWidth(16);
		setHeight(16);
		
	}

	@Override
	public String getType() {
		return "mothertp";
	}
	
	@Override
	public void update() {
		setPosition(orbit.updateOrbit());
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(RespawnLaser.getRenderimage(), rel_x, rel_y, null);
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				JSONObject jobj = new JSONObject();
				jobj.put("type", "mothertp");
				jobj.put("x", obj.getPosition().getX());
				jobj.put("y", obj.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				return new MotherTransport(null, null, null);
			}
		};
		
		GameObjectProcessor.addParser("mothertp", parser);
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.CONTROL;
	}

}
