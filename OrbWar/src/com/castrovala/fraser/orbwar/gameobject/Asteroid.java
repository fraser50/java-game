package com.castrovala.fraser.orbwar.gameobject;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.editor.Editor;
import com.castrovala.fraser.orbwar.editor.EditorManager;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.MaterialStore;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class Asteroid extends GameObject implements CollisionHandler {
	private static BufferedImage renderimage;
	private MaterialStore mstore;

	public MaterialStore getMstore() {
		return mstore;
	}

	public void setMstore(MaterialStore mstore) {
		this.mstore = mstore;
	}

	public Asteroid(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
		
		mstore = new MaterialStore();
		
		List<String[]> materials = new ArrayList<>();
		materials.add(new String[] { "gold", "55" } );
		materials.add(new String[] { "rock", "2" } );
		materials.add(new String[] { "carbon", "4" } );
		materials.add(new String[] { "silicon", "7" } );
		materials.add(new String[] { "uranium", "100" } );
		
		int chancesgiven = 200;
		
		for (int i = 1; i < chancesgiven + 1; i++) {
			Random r = new Random();
			for (String[] m : materials) {
				int chance = Integer.parseInt(m[1]);
				if (r.nextInt(chance) == r.nextInt(chance)) {
					mstore.addMaterial(m[0], 1);
				}
			}
		}
		
	}
	
	@Override
	public void update() {
		super.update();
		
		setRotation(rotation + 0.5f);
		if (rotation >= 360) {
			setRotation(0);
		}
	}
	
	@Override
	public void clientUpdate() {
		// TODO Auto-generated method stub
		super.clientUpdate();
		
		setRotation(rotation + 0.5f);
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
		AffineTransform orig = g2d.getTransform();
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		g2d.setTransform(orig);
		rd.onRender();
		
		if (rd.isTouching()) {
			Position mouse = rd.getMousepos();
			int x = (int) mouse.getX();
			int y = (int) mouse.getY();
			
			g2d.setColor(Color.GREEN);
			
			Composite c = g2d.getComposite();
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
			
			g2d.fillRect(x - 50, y - 50, 100, mstore.getMaterials().keySet().size() * 20);
			g2d.setComposite(c);
			
			g2d.setColor(Color.WHITE);
			int i = 1;
			for (String key : mstore.getMaterials().keySet()) {
				g2d.drawString(key + " " + mstore.getMaterials().get(key), x - 50, y - 60 + (20 * i));
				i++;
			}
		}
		
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
			
			@SuppressWarnings("unchecked")
			@Override
			public JSONObject toJSON(GameObject obj) {
				Asteroid ast = (Asteroid) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "asteroid");
				jobj.put("x", ast.getPosition().getX());
				
				jobj.put("y", ast.getPosition().getY());
				
				if (ast.getMstore() != null) {
					//jobj.put("materials", ast.getMstore().toJSON());
				}
				
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				Asteroid ast = new Asteroid(null, null);
				
				if (obj.containsKey("materials")) {
					MaterialStore mstore = new MaterialStore((JSONObject)obj.get("materials"));
					ast.setMstore(mstore);
				}
				
				return ast;
			}
		};
		
		GameObjectProcessor.addParser("asteroid", parser);
	}
	
	public static void registerEditor() {
		Editor e = new Editor("Asteroid") {
			
			@Override
			public GameObject spawn(WorldProvider controller) {
				Asteroid ast = new Asteroid(new Position(0, 0), controller);
				ast.setMstore(null);
				return ast;
			}
		};
		
		EditorManager.addEditor(e);
	}

}
