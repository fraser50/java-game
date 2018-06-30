package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.net.DestructionPacket;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.NetworkPlayer;
import com.castrovala.fraser.orbwar.util.AstCircle;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldController;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class BigAsteroid extends GameObject implements CollisionHandler {
	private BufferedImage texture;
	List<AstCircle> matter = new ArrayList<>();
	List<AstCircle> missing = new ArrayList<>();

	public BigAsteroid(Position pos, WorldProvider controller) {
		super(pos, controller);
		
	}
	
	@Override
	public void afterBirth() {
		buildMaterial();
		
		int bestx = 1;
		int besty = 1;
		for (AstCircle m : matter) {
			if (m.getX() + m.getRadius() > bestx) bestx = m.getX() + m.getRadius() + 8;
			if (m.getY() + m.getRadius() > besty) besty = m.getY() + m.getRadius() + 8;
		}
		
		setWidth(bestx);
		setHeight(besty);
	}
	
	public void buildMaterial() {
		matter.clear();
		Random rand = new Random(UUID.fromString(getUuid()).getMostSignificantBits());
		matter.add(new AstCircle(256, 256, 200 + rand.nextInt(41))); // 200+ 256, 256
	}
	
	public int getColour(int x, int y) {
		
		Random rand = new Random(x * y);
		
		int r = 50 + rand.nextInt(13);
		int g = 50 + rand.nextInt(7);
		int b = 60 + rand.nextInt(15);
		
		if (rand.nextFloat() <= 0.1) {
			r = 0;
			b = 0;
		}
		
		int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
		return rgb;
	}
	
	public void buildTexture() {
		//System.out.println(missing.size());
		int bestx = 1;
		int besty = 1;
		
		for (AstCircle m : matter) {
			if (m.getX() + m.getRadius() > bestx) bestx = m.getX() + m.getRadius();
			if (m.getY() + m.getRadius() > besty) besty = m.getY() + m.getRadius();
		}
		
		texture = new BufferedImage(bestx + 1, besty + 1, BufferedImage.TYPE_INT_ARGB);
		Position pos1 = new Position(0, 0);
		Position pos2 = new Position(0, 0);
		
		Rectangle r1 = new Rectangle(1, 1);
		Rectangle r2 = new Rectangle(1, 1);
		
		for (AstCircle m : matter) {
			double p = Math.pow(m.getRadius(), 2);
			for (int x = m.getX() - m.getRadius(); x <= m.getX() + m.getRadius(); x++) {
				for (int y = m.getY() - m.getRadius(); y <= m.getY() + m.getRadius(); y++) {
					pos1.setX(x);
					pos1.setY(y);
					pos2.setX(m.getX());
					pos2.setY(m.getY());
					r1.setLocation(x, y);
					if (Util.distanceSquared(pos1, pos2) <= p) {
						boolean suitable = true;
						for (AstCircle d : missing) {
							pos2.setX(d.getX());
							pos2.setY(d.getY());
							r2.setLocation(d.getX() - d.getRadius(), d.getY() - d.getRadius());
							r2.setSize(d.getRadius() * 2, d.getRadius() * 2);
							
							//if (!r1.intersects(r2)) continue;
							
							if (Util.distanceSquared(pos1, pos2) <= 625) { // Math.pow(d.getRadius(), 2)
								suitable = false;
								break;
							}
						}
						
						if (suitable) {
							texture.setRGB(x, y, getColour(x, y));
						}
						
					}
				}
			}
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		if (rd.isEditor()) {
			g2d.setColor(Color.GREEN);
			g2d.fillRect(rel_x, rel_y, 32, 32);
			return;
		}
		
		if (matter.size() == 0) {
			buildMaterial();
		}
		
		if (texture == null) buildTexture();
		AffineTransform orig = g2d.getTransform();
		g2d.rotate(Math.toRadians((double) this.getRotation()), rel_x + 256, rel_y + 256);
		g2d.drawImage(texture, rel_x, rel_y, texture.getWidth(), texture.getHeight(), null);
		g2d.setTransform(orig);
		texture = null;
	}

	@Override
	public String getType() {
		return "big";
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				BigAsteroid ast = (BigAsteroid) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "big");
				jobj.put("x", ast.getPosition().getX());
				
				jobj.put("y", ast.getPosition().getY());
				jobj.put("width", ast.getWidth());
				jobj.put("height", ast.getHeight());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				int width = ((Long) obj.get("width")).intValue();
				int height = ((Long) obj.get("height")).intValue();
				BigAsteroid ast = new BigAsteroid(null, null);
				ast.setWidth(width);
				ast.setHeight(height);
				return ast;
			}
		};
			
		GameObjectProcessor.addParser("big", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Big Asteroid") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new BigAsteroid(null, null);
			}
		};
		
		EditorManager.addEditor(e);
	}
	
	@Override
	public void hurt(int damage) {
		
	}
	

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			
			if (obj instanceof BigAsteroid) {
				obj.delete();
				delete();
				return;
			}
			
			if (obj instanceof Bullet) {
				Position mycentre = new Position(256, 256);
				Position bulletpos = obj.getPosition().copy().add(new Position(obj.getWidth() / 2, obj.getHeight() / 2));
				double magnitude = getPosition().copy().add(mycentre).distance(bulletpos);
				double originalangle = Util.targetRadius(bulletpos, getPosition().copy().add(mycentre));
				double fixedangle = originalangle - getRotation();
				Position relpos = Util.angleToVel(fixedangle, (float)magnitude).add(mycentre);
				//Position relpos = impactpos.add(getPosition());
				//System.out.println("X: " + relpos.getX() + " Y: " + relpos.getY());
				
				boolean proceed = false;
				for (AstCircle m : matter) {
					if (Util.distanceSquared(relpos, new Position(m.getX(), m.getY())) <= Math.pow(m.getRadius(), 2)) {
						proceed = true;
						for (AstCircle d : missing) {
							if (Util.distanceSquared(relpos, new Position(d.getX(), d.getY())) <= Math.pow(d.getRadius(), 2)) {
								proceed = false;
								break;
							}
						}
						
						break;
					}
				}
				
				if (!proceed) continue;
				obj.delete();
				
				AstCircle explosion = new AstCircle((int)relpos.getX(), (int)relpos.getY(), 25);
				missing.add(explosion);
				DestructionPacket p = new DestructionPacket(getUuid(), explosion.getX(), explosion.getY(), explosion.getRadius());
				WorldController c = (WorldController) getController();
				
				for (NetworkPlayer pl : c.getServer().getPlayers()) {
					pl.sendPacket(p);
				}
			}
		}
	}
	
	public List<AstCircle> getMissing() {
		return missing;
	}
	
	public void computeRotation() {
		double time = System.currentTimeMillis() / 100d;
		double goodrange = time - (((long)(time / 360)) * 360);
		setRotation((float)goodrange);
		setChanged(false);
	}
	
	@Override
	public void update() {
		computeRotation();
	}
	
	@Override
	public void clientUpdate() {
		computeRotation();
	}

}
