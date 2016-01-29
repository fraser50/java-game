package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;

public class Asteroid extends GameObject implements CollisionHandler {
	OrbitControl orbit;
	private List<Position> points = new ArrayList<>();
	private static BufferedImage renderimage;

	public Asteroid(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
		for (int i = 1; i<10; i++) {
			float rotation = (float) Util.randomRange(0, 360);
			float speed = (float) Util.randomRange(5, 32);
			Position p = Util.angleToVel(rotation, speed);
			points.add(p);
		}
		
	}
	
	@Override
	public void update() {
		super.update();
		
		setRotation(rotation + 1);
		if (rotation >= 360) {
			setRotation(0);
		}
//		getPosition().setX(getPosition().getX() + 1);
		if (orbit != null) {
			Position pos = orbit.updateOrbit();
			getPosition().setX(pos.getX());
			getPosition().setY(pos.getY());
		}
	}

	public OrbitControl getOrbit() {
		return orbit;
	}

	public void setOrbit(OrbitControl orbit) {
		this.orbit = orbit;
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

	public List<Position> getPoints() {
		return points;
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		g2d.rotate(Math.toRadians( ((double)this.getRotation() * -1)), centre_x, centre_y);
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
		return RenderStage.SPACEOBJECTS;
	}

	@Override
	public String getType() {
		return "asteroid";
	}

}
