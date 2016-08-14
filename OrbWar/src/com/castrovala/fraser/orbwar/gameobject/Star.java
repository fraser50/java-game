package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.gameobject.particle.HydrogenParticle;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

import net.minidev.json.JSONObject;

public class Star extends GameObject {
	private static BufferedImage renderimage;
	
	private float r = 255;
	private float g = 255;
	private float b = 0;
	
	public static final int min = 140;
	
	private float amount = -0.5f;
	
	public Star(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(256);
		setHeight(256);
	}
	
	@Override
	public void update() {
		super.update();
		
		setWidth(256);
		setHeight(256);
		
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.setColor(new Color((int)r, (int)g, (int)b));
		g2d.fillOval(rel_x, rel_y, getWidth(), getHeight());
		rd.onRender();
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/star.png")));
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
	public RenderStage getRenderStage() {
		return RenderStage.SPACEOBJECTS;
	}

	@Override
	public String getType() {
		return "star";
	}
	
	@Override
	public void clientUpdate() {
		Random rand = new Random();
		if (rand.nextBoolean()) {
			float rotation = (float) Util.randomRange(0, 360);
			float speed = (float) Util.randomRange(1, 2) * 2;
			Position p = getPosition().copy();
			p.add(new Position(getWidth() / 2, getHeight() / 2));
			p.add(Util.angleToVel(rotation, getWidth() / 2));
			HydrogenParticle h = new HydrogenParticle(p, getController(), Util.angleToVel(rotation, speed));
			getController().addObject(h);
		}
		
		g += amount;
		
		g = g < min ? min : g;
		g = g > 255 ? 255 : g;
		
		if (g == min) {
			amount = 0.5f;
		}
		
		if (g == 255) {
			amount = -0.5f;
		}
		
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Star st = (Star) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "star");
				jobj.put("x", st.getPosition().getX());
				
				jobj.put("y", st.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Star st = new Star(null, null);
				return st;
			}
		};
			
		GameObjectProcessor.addParser("star", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Star") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new Star(null, null);
			}
		};
		
		EditorManager.addEditor(e);
	}

}
