package com.castrovala.fraser.orbwar.util;

import com.castrovala.fraser.orbwar.gameobject.GameObject;

public class OrbitControl {
	private GameObject body;
	private float rotation;
	private float speed;
	private float rate;
	
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
		Position vel = Util.angleToVel(rotation, speed);
		Position pos = new Position(vel.getX() + body.getPosition().getX(),
									vel.getY() + body.getPosition().getY());
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

}
