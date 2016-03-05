package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.WorldProvider;

public class ShieldDrone extends GameObject implements CollisionHandler {
	private OrbitControl control;
	private static BufferedImage renderimage;
	private long life = 0;

	public ShieldDrone(Position pos, WorldProvider controller, OrbitControl control) {
		super(pos, controller, 100);
		this.control = control;
		setWidth(16);
		setHeight(16);
	}
	
	@Override
	public void update() {
		super.update();
		if (control.getBody().isDeleted()) {
			delete();
		}
		Position pos = control.updateOrbit();
		double x = pos.getX();
		double y = pos.getY();
		getPosition().setX(x + 32);
		getPosition().setY(y + 32);
	}

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(renderimage, rel_x, rel_y, null);
		rd.onRender();
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/respawnlaser.png")));
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
		return RenderStage.SHIPS;
	}
	
	@Override
	public String getType() {
		return "shield";
	}

}
