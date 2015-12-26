package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gameobject.particle.HydrogenParticle;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;

public class Star extends GameObject {
	private OrbitControl orbit;
	private static BufferedImage renderimage;
	
	public Star(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
	}
	
	@Override
	public void update() {
		super.update();
		if (orbit != null) {
			Position pos = orbit.updateOrbit();
			getPosition().setX(pos.getX());
			getPosition().setY(pos.getY());
			//Bullet b = new Bullet(getPosition().copy(), getController());
			//getController().addObject(b);
		}
		
		Random rand = new Random();
		if (rand.nextBoolean()) {
			float rotation = (float) Util.randomRange(0, 360);
			float speed = (float) Util.randomRange(1, 2) / 8;
			Position p = getPosition().copy();
			p.add(new Position(getWidth() / 2, getHeight() / 2));
			p.add(Util.angleToVel(rotation, 32));
			HydrogenParticle h = new HydrogenParticle(p, getController(), Util.angleToVel(rotation, speed));
			getController().addObject(h);
		}
		
	}

	public OrbitControl getOrbit() {
		return orbit;
	}

	public void setOrbit(OrbitControl orbit) {
		this.orbit = orbit;
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(renderimage, rel_x, rel_y, null);
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

}
