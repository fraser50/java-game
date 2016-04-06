package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.WorldProvider;

import net.minidev.json.JSONObject;

public class Asteroid extends GameObject implements CollisionHandler {
	private static BufferedImage renderimage;

	public Asteroid(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
		
	}
	
	@Override
	public void update() {
		super.update();
		
		setRotation(rotation + 1);
		if (rotation >= 360) {
			setRotation(0);
		}
	}

	@Override
	public void onCollision(GameObject[] objects) {
		for (GameObject obj : objects) {
			if (obj instanceof Bullet) {
				delete();
				return;
			}
		}
		
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		g2d.rotate(Math.toRadians((double)-this.getRotation()), centre_x, centre_y);
		rd.onRender();
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/asteroid.png")));
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
		//return RenderStage.SPACEOBJECTS;
		return super.getRenderStage();
	}

	@Override
	public String getType() {
		return "asteroid";
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				Asteroid ast = (Asteroid) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "asteroid");
				jobj.put("x", ast.getPosition().getX());
				
				jobj.put("y", ast.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Asteroid ast = new Asteroid(null, null);
				return ast;
			}
		};
		
		GameObjectProcessor.addParser("asteroid", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Asteroid") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				return new Asteroid(new Position(0, 0), controller);
			}
		};
		
		EditorManager.addEditor(e);
	}

}
