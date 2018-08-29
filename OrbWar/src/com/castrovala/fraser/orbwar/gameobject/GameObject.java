package com.castrovala.fraser.orbwar.gameobject;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.castrovala.fraser.orbwar.gui.RenderStage;
import com.castrovala.fraser.orbwar.util.RenderDebug;
import com.castrovala.fraser.orbwar.util.Util;
import com.castrovala.fraser.orbwar.world.Position;
import com.castrovala.fraser.orbwar.world.WorldProvider;
import com.castrovala.fraser.orbwar.world.WorldZone;

public abstract class GameObject {
	private Position pos;
	
	private boolean delete;
	protected float rotation;
	private Position velocity;
	private WorldProvider controller;
	private int width;
	private int height;
	private int health = 20;
	private int maxhealth = 20;
	private boolean cleaned;
	private List<GameObject> nearby = new ArrayList<>();
	private String uuid;
	private volatile boolean changed = false;
	private volatile boolean sizechanged = false;
	
	public GameObject(Position pos, WorldProvider controller) {
		uuid = UUID.randomUUID().toString();
		this.pos = pos;
		this.rotation = 0f;
		this.velocity = new Position(0, 0);
		this.controller = controller;
		delete = false;
	}
	
	public GameObject(Position pos, WorldProvider controller, int maxhealth) {
		this(pos, controller);
		this.setMaxhealth(maxhealth);
		health = maxhealth;
	}
	
	public void update() {
		if (getHealth() <= 0) {
			delete();
			death();
		}
	}
	
	public void clientUpdate() {}
	
	public void zoneUnload(WorldZone zone) {
		
	}
	
	public Position getPosition() {
		return pos;
	}
	
	public void setPosition(Position pos) {
		this.pos = pos;
		
		if (pos != null)
			pos.setEdited(true);
	}
	
	public void delete() {
		delete = true;
	}
	
	public boolean isDeleted() {
		return delete;
	}
	
	public float getRotation() {
		return rotation;
	}
	
	public void setRotation(float rotation) {
		this.rotation = (float) Util.fixAngle(rotation);
		changed = true;
	}

	public Position getVelocity() {
		return velocity;
	}
	
	public void setVelocity(Position velocity) {
		this.velocity = velocity;
	}

	public synchronized WorldProvider getController() {
		return controller;
	}

	public void setController(WorldProvider controller) {
		this.controller = controller;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		sizechanged = true;
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		sizechanged = true;
		this.height = height;
	}
	
	public Rectangle getBoundingBox() {
		Position p = getPosition().copy();
		p.setX(p.getX() - (getWidth () / 64) );
		p.setY(p.getY() - (getHeight() / 64) );
		int startx = (int) p.getX();
		int starty = (int) p.getY();
		return new Rectangle(startx, starty, getWidth(), getHeight());
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
		changed = true;
	}
	
	public void hurt(int damage) {
		health -= damage;
		changed = true;
	}
	
	public void hurt() {
		hurt(1);
	}

	public int getMaxhealth() {
		return maxhealth;
	}

	public void setMaxhealth(int maxhealth) {
		this.maxhealth = maxhealth;
	}
	
	public void render(Graphics2D g2d, int rel_x, int rel_y, int centre_x, int centre_y, RenderDebug rd) {
		
	}
	
	public static void loadResources() {
		
	}
	
	public GameObject[] getNearbyObjects(float radius) {
		controller.getScanners().put(this, (float) Math.pow(radius, 2));
		
		return nearby.toArray(new GameObject[nearby.size()]);
	}

	public boolean isCleaned() {
		return cleaned;
	}

	public void setCleaned(boolean cleaned) {
		this.cleaned = cleaned;
	}

	public List<GameObject> getNearby() {
		return nearby;
	}
	
	public double distanceSquared(GameObject obj) {
		return Util.distanceSquared(getPosition(), obj.getPosition());
	}
	
	public double distance(GameObject obj) {
		return Util.distance(getPosition(), obj.getPosition());
	}
	
	public void death() {}
	
	public RenderStage getRenderStage() {
		return RenderStage.BACKGROUND;
	}

	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	//public abstract String getType();
	public abstract String getType();

	public synchronized boolean isChanged() {
		return changed;
	}

	public synchronized void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public boolean shouldRotate() {
		return false;
	}
	
	public WorldZone getZone() {
		return getController().getZone(Util.toZoneCoords(getPosition()));
	}
	
	public void afterBirth() {}

	public boolean isSizechanged() {
		return sizechanged;
	}

	public void setSizechanged(boolean sizechanged) {
		this.sizechanged = sizechanged;
	};
	
	public boolean shouldSave() {
		return true;
	}
	
	public boolean shouldBroadcastDeath() {
		return true;
	}
	
}
