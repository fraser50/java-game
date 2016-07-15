package com.castrovala.fraser.orbwar.util;

import com.castrovala.fraser.orbwar.gameobject.GameObject;
import com.castrovala.fraser.orbwar.world.Position;

public class OrbitControl {
	private GameObject body;
	private float rotation;
	private float speed;
	private float rate;
	private GameObject target;
	
	public OrbitControl(GameObject body, float speed, float rate) {
		this.body = body;
		this.speed = speed;
		this.rate = rate;
		rotation = 0;
	}
	
	public Position updateOrbit() {
		rotation += rate;
		if (rotation >= 360) {
			rotation = 0;
		}
		
		double targetw = 0;
		double targeth = 0;
		if (target != null) {
			targetw = target.getWidth();
			targeth = target.getHeight();
		}
		
		Position vel = Util.angleToVel(rotation, speed);
		Position pos = new Position(vel.getX() + (targetw / 2) + body.getPosition().getX() + (body.getWidth() / 4),
									vel.getY() + (targeth / 2) + body.getPosition().getY() + (body.getHeight() / 4));
		return pos;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public GameObject getBody() {
		return body;
	}

	public void setBody(GameObject body) {
		this.body = body;
	}

	public GameObject getTarget() {
		return target;
	}

	public void setTarget(GameObject target) {
		this.target = target;
	}

}
