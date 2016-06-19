package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.WorldProvider;

import net.minidev.json.JSONObject;

public class ShieldGenerator extends GameObject implements CollisionHandler {
	private OrbitControl control;
	private static BufferedImage renderimage;

	public ShieldGenerator(Position pos, WorldProvider controller) {
		super(pos, controller, 20);
		setWidth(64);
		setHeight(64);
		
	}

	@Override
	public String getType() {
		return "shieldgen";
	}

	public OrbitControl getControl() {
		return control;
	}

	public void setControl(OrbitControl control) {
		this.control = control;
	}
	
	@Override
	public void update() {
		super.update();
		
		if (control != null) {
			Position pos = control.updateOrbit();
			getPosition().setX(pos.getX());
			getPosition().setY(pos.getY());
		}
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				ShieldGenerator gen = (ShieldGenerator) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "shieldgen");
				jobj.put("x", gen.getPosition().getX());
				
				jobj.put("y", gen.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				return new ShieldGenerator(null, null);
			}
		};
		
		GameObjectProcessor.addParser("shieldgen", parser);
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		rd.onRender();
		
		g2d.setColor(Color.GREEN);
		int green = (int) (getHealth() * getWidth()) / getMaxhealth();
		g2d.fillRect(rel_x, rel_y - 20, green, 5);
		
		g2d.setColor(Color.RED);
		g2d.fillRect(rel_x + green, rel_y - 20, getWidth() - green, 5);
		rd.onRender(4);
	}
	
	public static void loadResources() {
		ClassLoader cl = ShieldGenerator.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/shieldgen.png")));
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
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void death() {
		if (control != null) {
			OliverMothership ms = (OliverMothership) control.getBody();
			ms.getDrones().remove(this);
		}
	}

}
