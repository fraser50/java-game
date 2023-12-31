package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.planet.PlanetType;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.OpenSimplexNoise;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class Planet extends GameObject implements CollisionHandler {
	private BufferedImage image;
	private int counter = 0;

	public Planet(Position pos, WorldProvider controller) {
		super(pos, controller);
		
		setWidth(128);
		setHeight(128);
		setMaxhealth(1000);
		setHealth(1);
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
		
		BufferedImage img = image;
		if (img == null) {
			img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			PlanetType type = PlanetType.values()[(new Random(UUID.fromString(getUuid()).getMostSignificantBits())).nextInt(PlanetType.values().length)];
			
			int r = 0;
			int g = 0;
			int b = 0;
			
			OpenSimplexNoise noise = new OpenSimplexNoise(UUID.fromString(getUuid()).getMostSignificantBits());
			
			Position centrepos = getPosition().copy().add(new Position(getWidth() / 2, getHeight() / 2));
			for (int x = 1; x < getWidth() + 1; x++) {
				for (int y = 1; y < getHeight() + 1; y++) {
					
					
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
						
						switch (type) { // Land
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
								
							case DEAD:
								r = (int) (255 - (value * 100));
								g = (int) (240 - (value * 100));
								b = (int) (230 - (value * 100));
								break;
								
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
						
						switch (type) { // Water
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
								b = 100 - (int)((value * 100));
								break;
								
							case DEAD:
								r = 150;
								g = 150;
								b = (int) (170 + (value * 100));
								break;
								
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
			image = img;
		}
		
		g2d.drawImage(img, rel_x, rel_y, null);
		g2d.setColor(Color.GREEN);
		int green = (int) (getHealth() * getWidth()) / getMaxhealth();
		g2d.fillRect(rel_x, rel_y - 10, green, 5);
		
		g2d.setColor(Color.RED);
		g2d.fillRect(rel_x + green, rel_y - 10, getWidth() - green, 5);
		rd.onRender(4);
		//g2d.drawOval(rel_x, rel_y, 64, 64);
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public void death() {
		Explosion ex = new Explosion(getPosition().copy().add(new Position(64, 64)), getController(), 64);
		getController().addObject(ex);
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

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}

}
