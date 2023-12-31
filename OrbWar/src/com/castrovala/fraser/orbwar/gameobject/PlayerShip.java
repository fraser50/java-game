package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;

import com.castrovala.fraser.orbwar.gameobject.particle.SmokeParticle;
import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.save.GameObjParser;
import com.castrovala.fraser.orbwar.save.GameObjectProcessor;
import com.castrovala.fraser.orbwar.server.ControlUser;
import com.castrovala.fraser.orbwar.util.Anchor;
import com.castrovala.fraser.orbwar.util.CollisionHandler;
import com.castrovala.fraser.orbwar.util.Controllable;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.weapons.BulletGun;
import com.castrovala.fraser.orbwar.weapons.Forcefield;
import com.castrovala.fraser.orbwar.weapons.Weapon;
import com.castrovala.fraser.orbwar.weapons.WeaponOwner;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;

public class PlayerShip extends GameObject implements Controllable, WeaponOwner, CollisionHandler {
	private static BufferedImage renderimage;
	private Weapon primaryweaponA;
	private Weapon primaryweaponB;
	private Weapon shield;
	private double speed;
	private double maxspeed = 6d; // 6d
	private boolean handbrake;
	private ControlUser user;
	private final int healingrate = 1;
	private int timeundamaged = 0;
	private int fuel = 10000;
	
	public PlayerShip(Position pos, WorldProvider controller) {
		super(pos, controller);
		setWidth(64);
		setHeight(64);
		primaryweaponA = new BulletGun();
		primaryweaponB = new BulletGun();
		shield = new Forcefield();
	}
	
	@Override
	public void fire() {
		if (primaryweaponA != null && primaryweaponB != null) {
			Anchor a = new Anchor(new Position(9, 12), new Position(32, 32));
			Anchor b = new Anchor(new Position(45, 12), new Position(32, 32));
			
			Position posa = a.getAnchorPosition(getRotation()).add(getPosition()).subtract(new Position(8, 8));
			Position posb = b.getAnchorPosition(getRotation()).add(getPosition()).subtract(new Position(8, 8));
			primaryweaponA.fire(this, posa);
			//primaryweaponB.fire(this, posb);
		}
	}
	
	@Override
	public void update() {
		super.update();
		fuel = 100;
		
		if (primaryweaponA != null) {
			primaryweaponA.update(this);
			primaryweaponB.update(this);
		}
		
		
		if (rotation >= 360) {
			setRotation(0);
		}
		
		if (speed > maxspeed) {
			speed = maxspeed;
		}
		
		Position vel = Util.angleToVel(getRotation(), (float)speed);
		//getPosition().add(vel);
		
		float magnitude = (float) Math.sqrt(Math.pow(getVelocity().getX(), 2) + Math.pow(getVelocity().getY(), 2));
		
		if (magnitude > maxspeed) {
			getVelocity().divide(new Position(magnitude, magnitude)).multiply(maxspeed);
		} else if (magnitude > 0) {
			float newmag = magnitude - 0.01f >= 0f ? magnitude - 0.01f : 0f;
			getVelocity().divide(new Position(magnitude, magnitude)).multiply(newmag);
		}
		
		getPosition().add(getVelocity());
		
		if (handbrake) {
			speed -= 0.1d; // 0.1
			double behind = Util.fixAngle(getRotation() + 360);
			Position pos = Util.angleToVel(behind, -20);
			Position smokepos1 = getPosition().copy().add(new Position(32, 32)).add(pos);
			Position smokepos2 = smokepos1.copy();
			
			for (int i = 1; i < 30; i++) {
				double smokeangle1 = Util.fixAngle(getRotation() + 90);
				double smokeangle2 = Util.fixAngle(getRotation() - 90);
				
				Position smokemv1 = Util.angleToVel(Util.randomRange((int)smokeangle1 - 20, (int)smokeangle1 + 20), Util.randomRange(10, 40) / 10);
				Position smokemv2 = Util.angleToVel(Util.randomRange((int)smokeangle2 - 20, (int)smokeangle2 + 20), Util.randomRange(10, 40) / 10);
				
				SmokeParticle smoke1 = new SmokeParticle(smokepos1.copy(), getController(), smokemv1);
				SmokeParticle smoke2 = new SmokeParticle(smokepos2.copy(), getController(), smokemv2);
				getController().addObject(smoke1);
				getController().addObject(smoke2);
			}
			
		} else {
			speed -= 0.01d;
		}
		
		if (speed < 0) {
			speed = 0;
		}
		
		timeundamaged++;
		if (timeundamaged >= 1000) {
			timeundamaged = 925;
			setHealth(getHealth() + healingrate);
			if (getHealth() > getMaxhealth()) {
				setHealth(getMaxhealth());
			}
		}
		
		if (getHealth() == getMaxhealth()) {
			timeundamaged = 0;
		}
		
	}

	@Override
	public void left() {
		setRotation(getRotation() - 4.5f);
		
	}

	@Override
	public void right() {
		setRotation(getRotation() + 4.5f);
		
	}
	
	@Override
	public void fly() {
		
		if (fuel <= 0) return;
		
		speed += 2; // 2
		
		getVelocity().add(Util.angleToVel(getRotation(), 4f));
		
	}
	
	@Override
	public void shield() {
		//toprotect = true;
	}
	
	@Override
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		AffineTransform orig = g2d.getTransform();
		g2d.rotate(Math.toRadians((double) this.getRotation()), centre_x, centre_y);
		g2d.drawImage(getRenderimage(), rel_x, rel_y, null);
		g2d.setTransform(orig);
		
		g2d.setColor(Color.GREEN);
		int green = (int) (getHealth() * getWidth()) / getMaxhealth();
		g2d.fillRect(rel_x, rel_y - 10, green, 5);
		
		g2d.setColor(Color.RED);
		g2d.fillRect(rel_x + green, rel_y - 10, getWidth() - green, 5);
		rd.onRender(4);
		
		//Position pos = getPosition().copy();
		//pos.add(new Position(32, 32));
		//pos.add(Util.angleToVel(getRotation() - 90d, 28));
		//pos.add(Util.angleToVel(getRotation(), 16));
		//pos.setX(pos.getX() + 8);
		//pos.setY(pos.getY() + 8);
		
		Anchor a = new Anchor(new Position(9, 12), new Position(32, 32));
		Anchor b = new Anchor(new Position(45, 12), new Position(32, 32));
		
		Position posa = a.getAnchorPosition(getRotation()).add(getPosition());
		Position posb = b.getAnchorPosition(getRotation()).add(getPosition());
		
		Position extendedposa = Util.angleToVel(getRotation(), 48).add(posa);
		Position extendedposb = Util.angleToVel(getRotation(), 48).add(posb);
		
		Position scrpos1a = Util.coordToScreen(posa, rd.getRenderloc());
		Position scrpos2a = Util.coordToScreen(extendedposa, rd.getRenderloc());
		
		Position scrpos1b = Util.coordToScreen(posb, rd.getRenderloc());
		Position scrpos2b = Util.coordToScreen(extendedposb, rd.getRenderloc());
		
		g2d.setColor(Color.RED);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawLine((int)scrpos1a.getX(), (int)scrpos1a.getY(), (int)scrpos2a.getX(), (int)scrpos2a.getY());
		g2d.drawLine((int)scrpos1b.getX(), (int)scrpos1b.getY(), (int)scrpos2b.getX(), (int)scrpos2b.getY());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		
	}
	
	public static void loadResources() {
		ClassLoader cl = PlayerShip.class.getClassLoader();
		try {
			setRenderimage(ImageIO.read(cl.getResourceAsStream("resources/playership.png")));
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
	public Weapon getPrimaryweapon() {
		return primaryweaponA;
	}

	@Override
	public void setPrimaryweapon(Weapon primaryweapon) {
		this.primaryweaponA = primaryweapon;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	@Override
	public void onCollision(GameObject[] objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void death() {
		Position pos = getPosition().copy();
		pos.add(new Position(getWidth() / 2, getHeight() / 2));
		RespawnPoint point = new RespawnPoint(pos, getController(), getControl());
		getController().addObject(point);
		
		Explosion ex = new Explosion(pos.copy().add(new Position(32, 32)), getController(), 32);
		getController().addObject(ex);
	}

	public double getMaxspeed() {
		return maxspeed;
	}

	public void setMaxspeed(float maxspeed) {
		this.maxspeed = maxspeed;
	}

	public boolean isHandbrake() {
		return handbrake;
	}

	public void setHandbrake(boolean handbrake) {
		this.handbrake = handbrake;
	}
	
	@Override
	public void toggleBrake() {
		handbrake = !handbrake;
		
	}
	
	@Override
	public void suicide() {
		setHealth(0);
		
	}
	
	@Override
	public RenderStage getRenderStage() {
		return RenderStage.CONTROL;
	}

	public Weapon getShield() {
		return shield;
	}

	public void setShield(Weapon shield) {
		this.shield = shield;
	}
	
	@Override
	public ControlUser getControl() {
		return user;
	};

	@Override
	public void setControl(ControlUser user) {
		this.user = user;
		
	}

	@Override
	public String getType() {
		return "playership";
	}
	
	public static void registerGameObj() {
		GameObjParser parser = new GameObjParser() {
			
			@Override
			public JSONObject toJSON(GameObject obj) {
				PlayerShip ship = (PlayerShip) obj;
				JSONObject jobj = new JSONObject();
				jobj.put("type", "playership");
				jobj.put("x", ship.getPosition().getX());
				
				jobj.put("y", ship.getPosition().getY());
				return jobj;
			}
			
			@Override
			public GameObject fromJSON(JSONObject obj) {
				PlayerShip ship = new PlayerShip(null, null);
				return ship;
			}
		};
		
		GameObjectProcessor.addParser("playership", parser);
	}
	
	@Override
	public boolean shouldRotate() {
		return true;
	}
	
	@Override
	public void hurt(int damage) {
		super.hurt(damage);
		timeundamaged = 0;
	}
	
	@Override
	public boolean shouldSave() {
		return false;
		
	}

}
