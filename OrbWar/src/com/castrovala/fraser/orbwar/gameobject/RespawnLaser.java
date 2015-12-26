package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.util.OrbitControl;
import com.castrovala.fraser.orbwar.util.Position;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.util.WorldProvider;

public class RespawnLaser extends GameObject {
	
	private static BufferedImage renderimage;
	
	public OrbitControl control;
	public boolean firing;

	public RespawnLaser(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(16);
		setHeight(16);
	}
	
	@Override
	public void update() {
		super.update();
		
		if (control != null) {
			Position pos = control.updateOrbit();
			getPosition().setX(pos.getX());
			getPosition().setY(pos.getY());
		}
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		g2d.drawImage(renderimage, rel_x, rel_y, null);
		rd.onRender();
		
		if (firing) {
			Position spos = rd.getRenderloc();
			Position bodypos = control.getBody().getPosition();
			g2d.setColor(Color.WHITE);
			
			// Position start = Util.coordToScreen(new Position(centre_x, centre_y), spos);
			Position end = Util.coordToScreen(new Position(bodypos.getX(), bodypos.getY()), spos);
			
			g2d.drawLine((int)centre_x, (int)centre_y, (int)end.getX(), (int)end.getY());
		}
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
		return RenderStage.SPACEOBJECTS;
	}

}
