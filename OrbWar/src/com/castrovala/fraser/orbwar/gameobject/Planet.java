package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.planet.PlanetType;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.OpenSimplexNoise;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

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
			
			PlanetType type = PlanetType.values()[(new Random(UUID.fromString(getUuid()).getMostSignificantBits())).nextInt(PlanetType.values().length)];
			
			int r = 0;
			int g = 0;
			int b = 0;
			
			OpenSimplexNoise noise = new OpenSimplexNoise(UUID.fromString(getUuid()).getMostSignificantBits());
			
			
			for (int x = 1; x < getWidth() + 1; x++) {
				for (int y = 1; y < getHeight() + 1; y++) {
					
					Position centrepos = new Position(centre_x, centre_y);
					Position chosenpos = getPosition().copy().add(new Position(x, y));
					
					double distance = Util.distance(centrepos, chosenpos);
					
					if (distance > getWidth() / 2) {
						continue;
					}
					
					double value = noise.eval(x / 16.5, y / 16.5);
					//System.out.println("Noise value: " + value);
					if (value > 0.2) {
						//System.out.println("Land");
						
						//int r = 0;
						//int g = 50 + (int)(value * 100);
						//int b = 0;
						
						switch (type) {
							case EARTH:
								r = 0;
								g = 50 + (int)(value * 100);
								b = 0;
								break;
							case ALIEN:
								r = 50 + (int)(value * 100);
								g = 60;
								b = 60;
								break;
							case DESERT:
								r = 128;
								g = 128;
								b = (int)(value * 100);
						default:
							break;
						}
						
						int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
						img.setRGB(x - 1, y - 1, rgb);
					} else { //int b = (int) (80 + Math.sqrt((value * 100) * (value * 100) ) );
						//System.out.println("Water");
						r = 0;
						g = 0;
						
						b = (int) (100 - (value * 100) );
						
						switch (type) {
							case EARTH:
								r = 0;
								g = 0;
								b = (int) (100 - (value * 100) );
								break;
							case ALIEN:
								r = 255;
								g = (int) (100 - (value * 100) );
								b = 40;
								break;
							case DESERT:
								r = 255;
								g = 255;
								b = 250 - (int)(value * 100);
						default:
							break;
						}
						
						int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
						img.setRGB(x - 1, y - 1, rgb);
						
						
						
					}
					/*if (value <= 0.5f && type == PlanetType.EARTH) {
						int rgb = (new Color(1f, 1f, 1f, 0.5f)).getRGB();
						img.setRGB(x - 1, y - 1, rgb);
					}*/
					
					
				}
				
			}
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
