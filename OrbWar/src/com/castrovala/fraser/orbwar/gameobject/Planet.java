package com.castrovala.fraser.orbwar.gameobject;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.OpenSimplexNoise;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

import net.minidev.json.JSONObject;

public class Planet extends GameObject {
	private static Map<String, BufferedImage> planetimages = new HashMap<>();

	public Planet(Position pos, WorldProvider controller) {
		super(pos, controller);
		
		setWidth(128);
		setHeight(128);
	}

	@Override
	public String getType() {
		return "planet";
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		
		if (rd.isEditor()) {
			g2d.drawOval(rel_x, rel_y, getWidth(), getHeight());
			return;
		}
		
		BufferedImage img = planetimages.get(getUuid());
		if (img == null) {
			img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			for (int x = 1; x < getWidth() + 1; x++) {
				for (int y = 1; y < getHeight() + 1; y++) {
					OpenSimplexNoise noise = new OpenSimplexNoise(UUID.fromString(getUuid()).getMostSignificantBits());
					double value = noise.eval(x / 14.5, y / 14.5, 0, 0.5);
					//System.out.println("Noise value: " + value);
					if (value > 0) {
						//System.out.println("Land");
						
						int r = 0;
						int g = 50 + (int)(value * 100);
						int b = 0;
						int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
						img.setRGB(x - 1, y - 1, rgb);
					} else {
						//System.out.println("Water");
						int r = 0;
						int g = 0;
						//int b = (int) (80 + Math.sqrt((value * 100) * (value * 100) ) );
						int b = (int) (100 - (value * 100) );
						int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
						img.setRGB(x - 1, y - 1, rgb);
					}
					
					
				}
				
			}
			
			Graphics2D g2dimg = (Graphics2D) img.getGraphics();
			g2dimg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2dimg.setComposite(AlphaComposite.Clear);
			g2dimg.setStroke(new BasicStroke(5));
			for (int i = 0; i < 361; i++) {
				Position vel = Util.angleToVel(i, (getWidth() / 2) + 2);
				g2dimg.drawLine((int)vel.getX() + (getWidth() / 2), (int)vel.getY() + (getHeight() / 2), (int)(vel.getX() * 8) + (getWidth() / 2), (int)(vel.getY() * 8) + (getHeight() / 2));
			}
			
			g2dimg.setComposite(AlphaComposite.SrcOver);
			
			planetimages.put(getUuid(), img);
		}
		
		g2d.drawImage(img, rel_x, rel_y, null);
		//g2d.drawOval(rel_x, rel_y, 64, 64);
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Planet pl = (Planet) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "planet");
				jobj.put("x", pl.getPosition().getX());
				
				jobj.put("y", pl.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Planet pl = new Planet(null, null);
				return pl;
			}
		};
			
		GameObjectProcessor.addParser("planet", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Planet") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new Planet(null, null);
			}
		};
		
		EditorManager.addEditor(e);
	}

}
